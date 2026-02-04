# Easy-Query 五大隐式特性详解

Easy-Query 的核心竞争力在于其五大隐式特性，这些特性可以自动处理复杂的关系查询，大幅简化开发工作。

## 1. 隐式 Join（Implicit Join）

自动处理 OneToOne 和 ManyToOne 关系，无需显式编写 JOIN 语句。

### 使用场景

当查询对象通过导航属性访问关联对象时，自动生成 LEFT JOIN：

```java
// 自动生成 LEFT JOIN
List<SysUser> users = easyEntityQuery.queryable(SysUser.class)
    .where(user -> {
        user.company().name().like("阿里");
        user.company().registerMoney().gt(new BigDecimal("1000000"));
    })
    .orderBy(user -> {
        user.company().registerMoney().desc();
        user.birthday().asc();
    })
    .toList();

-- 生成的 SQL
SELECT t.*
FROM t_sys_user t
LEFT JOIN t_company t1 ON t.company_id = t1.id
WHERE t1.name LIKE '%阿里%'
  AND t1.register_money > 1000000
ORDER BY t1.register_money DESC, t.birthday ASC
```

### 优势

- 简化查询语法，无需手写 JOIN 条件
- 类型安全，编译时检查
- 支持多级导航：`user.company().department().name()`

### 注意事项

- 隐式 Join 只支持 OneToOne 和 ManyToOne 关系
- OneToMany 和 ManyToMany 需要使用隐式子查询

## 2. 隐式子查询（Implicit Subquery）

自动处理 OneToMany 和 ManyToMany 关系，生成 EXISTS 或 IN 子查询。

### 使用场景

```java
// 自动生成 EXISTS 子查询
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(company -> {
        company.users().any(u -> u.name().like("小明"));
        company.users().where(u -> u.name().like("小明"))
            .max(u -> u.birthday()).gt(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
    })
    .toList();

-- 生成的 SQL
SELECT t.*
FROM t_company t
WHERE EXISTS (
    SELECT 1
    FROM t_sys_user t1
    WHERE t1.company_id = t.id
      AND t1.name LIKE '%小明%'
)
AND (
    SELECT MAX(t1.birthday)
    FROM t_sys_user t1
    WHERE t1.company_id = t.id
      AND t1.name LIKE '%小明%'
) > '2000-01-01 00:00:00'
```

### 支持的方法

- `.any()` - 存在任意满足条件的记录（EXISTS）
- `.all()` - 所有记录都满足条件
- `.where(condition).max/min/avg/sum/count()` - 聚合函数

### 优势

- 自动生成优化的子查询
- 支持复杂的聚合条件
- 避免 N+1 查询问题

## 3. 隐式分组（Implicit Grouping）

将多个 OneToMany/ManyToMany 子查询合并为单个分组查询，提升性能。

### 使用场景

```java
// 多个子查询会被合并为一个 GROUP BY 查询
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(company -> company.users())
    .where(company -> {
        company.users().any(u -> u.name().like("小明"));
        company.users().where(u -> u.name().like("小明"))
            .max(u -> u.birthday()).gt(LocalDateTime.now());
    })
    .toList();

-- 生成的 SQL（优化后）
SELECT t.*
FROM t_company t
LEFT JOIN (
    SELECT company_id,
           COUNT(*) AS user_count,
           MAX(birthday) AS max_birthday
    FROM t_sys_user
    WHERE name LIKE '%小明%'
    GROUP BY company_id
) t1 ON t.id = t1.company_id
WHERE t1.user_count > 0
  AND t1.max_birthday > '2024-01-01 00:00:00'
```

### 触发条件

通过 `.subQueryToGroupJoin()` 方法显式启用，或在 `@Navigate` 注解中配置：
```java
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;
```

### 优势

- 减少子查询数量
- 降低数据库负载
- 提升大数据量场景性能

### 适用场景

- 同一个集合属性有多个查询条件
- 大数据量场景（>1000 条关联记录）

## 4. 隐式分区分组（Implicit Partition Grouping）

支持对集合进行 First/Nth/ElementAt 等操作，自动生成分区窗口函数。

### 使用场景

```java
// 获取每个公司生日最大的用户
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(company -> {
        company.users().orderBy(u -> u.birthday().desc()).first().name().eq("小明");
        company.users().orderBy(u -> u.birthday().desc()).element(0)
            .birthday().lt(LocalDateTime.now());
    })
    .toList();

-- 生成的 SQL（使用窗口函数）
SELECT t.*
FROM t_company t
LEFT JOIN (
    SELECT t1.*,
           ROW_NUMBER() OVER (PARTITION BY t1.company_id ORDER BY t1.birthday DESC) AS rn
    FROM t_sys_user t1
) t2 ON t.id = t2.company_id AND t2.rn = 1
WHERE t2.name = '小明'
  AND t2.birthday < '2024-01-01 00:00:00'
```

### 支持的方法

- `.first()` - 第一个元素（ROW_NUMBER() = 1）
- `.element(index)` - 指定索引的元素
- `.last()` - 最后一个元素

### 数据库兼容性

| 数据库 | 窗口函数支持 | 备注 |
|--------|------------|------|
| MySQL 8.0+ | ✅ | 完整支持 |
| PostgreSQL | ✅ | 完整支持 |
| Oracle | ✅ | 完整支持 |
| SQL Server | ✅ | 完整支持 |
| MySQL 5.7 | ❌ | 使用子查询模拟 |

### 优势

- 简化"取每组第一条/第N条"的查询
- 自动使用最优的窗口函数
- 避免应用层循环处理

## 5. 隐式 CASE WHEN 表达式

通过 `.filter()` 方法实现条件聚合，自动生成 CASE WHEN 表达式。

### 使用场景

```java
// 统计各地区用户数量和平均年龄
List<Draft3<String, Long, BigDecimal>> result = easyEntityQuery.queryable(SysUser.class)
    .where(user -> user.birthday().lt(LocalDateTime.now()))
    .select(user -> Select.DRAFT.of(
        user.address(),
        user.id().count().filter(() -> user.address().eq("杭州")),
        user.id().count().filter(() -> user.address().eq("北京")),
        user.age().avg().filter(() -> user.address().eq("北京"))
    ))
    .toList();

-- 生成的 SQL
SELECT
    t.address,
    COUNT(CASE WHEN t.address = '杭州' THEN 1 END) AS value2,
    COUNT(CASE WHEN t.address = '北京' THEN 1 END) AS value3,
    AVG(CASE WHEN t.address = '北京' THEN t.age END) AS value4
FROM t_sys_user t
WHERE t.birthday < '2024-01-01 00:00:00'
```

### 语法模式

```java
// 聚合函数 + filter()
property.count().filter(() -> condition)
property.sum().filter(() -> condition)
property.avg().filter(() -> condition)
property.max().filter(() -> condition)
property.min().filter(() -> condition)
```

### 应用场景

- 数据统计和报表
- 条件分组统计
- 动态指标计算

### 优势

- 简化 CASE WHEN 表达式编写
- 类型安全的条件表达式
- 支持所有聚合函数

## 性能对比

| 场景 | 传统 SQL | 隐式特性 | 性能提升 |
|------|---------|---------|---------|
| 关联查询 | 手写 LEFT JOIN | 隐式 Join | 开发效率 ⬆️ |
| 集合过滤 | EXISTS 子查询 | 隐式子查询 | SQL 优化 ⬆️ |
| 多条件集合查询 | 多个子查询 | 隐式分组 | 查询性能 ⬆️⬆️ |
| 分组取首条 | 窗口函数 | 隐式分区分组 | 代码简化 ⬆️ |
| 条件聚合 | CASE WHEN | 隐式 CASE WHEN | 可读性 ⬆️ |

## 最佳实践

1. **优先使用隐式特性**：减少手写 SQL，提高开发效率
2. **大数据量使用隐式分组**：避免多个子查询
3. **理解生成的 SQL**：通过日志检查 SQL 正确性
4. **合理配置 @Navigate**：根据场景选择是否启用 subQueryToGroupJoin
