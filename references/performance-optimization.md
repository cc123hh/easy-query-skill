# Easy-Query 性能优化指南

本文档总结 Easy-Query 的性能优化技巧和常见坑点，帮助开发者写出高效的数据库查询。

## 性能优化清单

### 1. 查询优化

#### 使用隐式分组替代多子查询

**问题场景**：对同一集合有多个查询条件时

```java
// ❌ 性能差：生成多个 EXISTS 子查询
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> {
        c.users().any(u -> u.age().gt(18));
        c.users().any(u -> u.age().lt(60));
        c.users().count().gt(10);
    })
    .toList();

-- 生成 SQL（3 个子查询）
SELECT * FROM t_company
WHERE EXISTS (SELECT 1 FROM t_user WHERE company_id = t.id AND age > 18)
  AND EXISTS (SELECT 1 FROM t_user WHERE company_id = t.id AND age < 60)
  AND (SELECT COUNT(*) FROM t_user WHERE company_id = t.id) > 10
```

**优化方案**：使用隐式分组

```java
// ✅ 性能好：合并为单个 GROUP BY
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(c -> c.users())
    .where(c -> {
        c.users().any(u -> u.age().gt(18));
        c.users().any(u -> u.age().lt(60));
        c.users().count().gt(10);
    })
    .toList();

-- 生成 SQL（1 个 JOIN）
SELECT t.*
FROM t_company t
LEFT JOIN (
    SELECT company_id,
           COUNT(*) AS cnt,
           SUM(CASE WHEN age > 18 THEN 1 END) AS age_gt_18,
           SUM(CASE WHEN age < 60 THEN 1 END) AS age_lt_60
    FROM t_user
    GROUP BY company_id
) t1 ON t.id = t1.company_id
WHERE t1.age_gt_18 > 0 AND t1.age_lt_60 > 0 AND t1.cnt > 10
```

**配置方式**：
```java
// 方式 1：查询时启用
.subQueryToGroupJoin(c -> c.users())

// 方式 2：注解中配置
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;
```

#### 避免选择所有字段

```java
// ❌ 查询所有字段
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

-- SELECT id, title, content, url, star, publish_time, score, status, ...

// ✅ 只查询需要的字段
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .title().set(b.title())
    )
    .toList();

-- SELECT id, title FROM t_blog
```

#### 使用分页避免大数据量查询

```java
// ❌ 一次查询所有数据
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

// ✅ 使用分页
EasyPageResult<BlogEntity> page = easyEntityQuery.queryable(BlogEntity.class)
    .toPageResult(1, 20);  // 每页 20 条
```

### 2. 更新优化

#### 使用差异化更新

**场景**：只更新修改过的字段

```java
// ❌ 全字段更新
BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
    .whereById("123").firstNotNull();
blog.setTitle("新标题");  // 只改了一个字段
easyEntityQuery.updatable(blog).executeRows();

-- UPDATE t_blog SET title = ?, content = ?, url = ?, ... WHERE id = ?
-- 所有字段都会更新

// ✅ 差异化更新
TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
try {
    trackManager.begin();
    BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
        .asTracking()
        .whereById("123").firstNotNull();
    blog.setTitle("新标题");
    easyEntityQuery.updatable(blog).executeRows();
} finally {
    trackManager.release();
}

-- UPDATE t_blog SET title = ? WHERE id = ?
-- 只更新修改过的字段
```

#### 使用批量更新

```java
// ❌ 循环单条更新
for (String id : ids) {
    easyEntityQuery.updatable(BlogEntity.class)
        .setColumns(b -> b.status().set(1))
        .where(b -> b.id().eq(id))
        .executeRows();
}

// ✅ 批量更新
easyEntityQuery.updatable(BlogEntity.class)
    .setColumns(b -> b.status().set(1))
    .where(b -> b.id().in(ids))
    .executeRows();
```

### 3. 关系查询优化

#### 小数据量：使用隐式子查询

```java
// 适用场景：关联数据 < 100 条
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();

-- 生成 EXISTS 子查询，性能良好
```

#### 大数据量：使用隐式分组

```java
// 适用场景：关联数据 > 1000 条
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(c -> c.users())
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();

-- 生成 GROUP BY + LEFT JOIN，避免多个子查询
```

#### 避免循环关联查询（N+1 问题）

```java
// ❌ N+1 问题
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // 每次都查询一次
}
// 1 次查询公司 + N 次查询用户

// ✅ 使用隐式查询
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();
// 1 次查询完成
```

### 4. 索引优化建议

#### 为常用查询字段创建索引

```sql
-- WHERE 条件字段
CREATE INDEX idx_topic_id ON t_blog(topic_id);

-- JOIN 条件字段
CREATE INDEX idx_company_id ON t_sys_user(company_id);

-- 排序字段
CREATE INDEX idx_create_time ON t_blog(create_time);

-- 组合索引（最左前缀）
CREATE INDEX idx_status_create_time ON t_blog(status, create_time);
```

#### 避免索引失效

```java
// ❌ 索引失效：在索引字段上使用函数
.where(b -> b.title().concat("xxx").eq("xxx"))

// ✅ 索引生效：直接比较
.where(b -> b.title().eq("xxx"))

// ❌ 索引失效：隐式类型转换
.where(b -> b.status().eq("123"))  // status 是 INT，"123" 是 STRING

// ✅ 索引生效：类型匹配
.where(b -> b.status().eq(123))

// ❌ 索引失效：前缀模糊查询
.where(b -> b.title().like("%关键词"))

// ✅ 索引生效：后缀模糊查询
.where(b -> b.title().like("关键词%"))
```

## 常见性能坑点

### 坑点 1：未启用 subQueryToGroupJoin

**问题**：大数据量场景下，多个子查询导致性能低下

**解决**：
```java
// 在 @Navigate 注解中全局启用
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;

// 或在查询时临时启用
.subQueryToGroupJoin(c -> c.users())
```

### 坑点 2：查询后使用 getter 访问关联数据

```java
// ❌ N+1 问题
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // 懒加载，每次循环都查询
}

// ✅ 在查询时指定需要的关联数据
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();
```

### 坑点 3：selectAll() 查询大对象

```java
// ❌ 查询所有字段（包括大文本）
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

// ✅ 只查询需要的字段
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .title().set(b.title())
        // 不查询 content 等大字段
    )
    .toList();
```

### 坑点 4：在循环中执行查询

```java
// ❌ 性能差
for (String id : ids) {
    BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
        .where(b -> b.id().eq(id))
        .firstNotNull();
}

// ✅ 使用 IN 查询
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.id().in(ids))
    .toList();
```

### 坑点 5：未使用 @Column(ignoreProperties)

```java
// ❌ 查询了大字段
@Table("t_blog")
@EntityProxy
public class BlogEntity {
    private String content;  // 大文本，每次都查询
}

// ✅ 忽略大字段
@Table(value = "t_blog", ignoreProperties = {"content"})
@EntityProxy
public class BlogEntity {
    private String content;  // 默认不查询
}

// 需要时手动查询
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .content().set(b.content())  // 只在需要时查询
    )
    .toList();
```

## 性能监控

### 开启 SQL 日志

```yaml
# application.yml
easy-query:
  print-sql: true
  sql-time: true  # 打印执行时间
```

### 慢查询分析

```java
// 执行时间 > 1s 的查询会被标记为慢查询
EasyPageResult<BlogEntity> page = easyEntityQuery.queryable(BlogEntity.class)
    .toPageResult(1, 20);

// 检查日志
-- Time: 1234 ms
-- SLOW QUERY DETECTED!
SELECT * FROM t_blog LIMIT 20
```

## 性能优化总结

| 场景 | 优化方案 | 性能提升 |
|------|---------|---------|
| 多子查询 | 启用 subQueryToGroupJoin | ⬆️⬆️ |
| 查询所有字段 | select() 指定字段 | ⬆️ |
| N+1 查询 | 使用隐式查询 | ⬆️⬆️⬆️ |
| 循环查询 | 使用 IN 批量查询 | ⬆️⬆️ |
| 全字段更新 | 使用差异化更新 | ⬆️ |
| 循环更新 | 使用批量更新 | ⬆️⬆️ |
| 大字段查询 | 使用 ignoreProperties | ⬆️ |

优化原则：
1. **减少数据库交互次数**
2. **减少数据传输量**
3. **充分利用索引**
4. **监控慢查询并优化**
