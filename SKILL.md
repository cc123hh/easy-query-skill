---
name: easy-query-expert
description: This skill should be used when the user asks to "use Easy-Query", "write an Easy-Query query", "@EntityProxy annotation", "Easy-Query join query", "Easy-Query tracking update", "Easy-Query pagination", or mentions "proxy class generation", "implicit join", "implicit subquery", "entity navigation". Provides comprehensive guidance for the Easy-Query ORM framework including type-safe Lambda queries, implicit relationships, tracking updates, and proxy pattern usage.
version: 1.0.0
---

# Easy-Query ORM Expert

Easy-Query 是一个基于 APT 代理模式的 Java ORM 框架，提供类型安全的 Lambda 查询语法。此 Skill 涵盖核心概念、常见操作、关系映射和高级特性。

## 核心概念

### 代理模式

Easy-Query 使用 APT（Annotation Processing Tool）在编译时生成代理类，实现类型安全的查询语法。

**实体代理** (`@EntityProxy`)：
```java
@Table("t_blog")
@EntityProxy
public class BlogEntity implements ProxyEntityAvailable<BlogEntity, BlogEntityProxy> {
    private String title;
    private BigDecimal score;
}
```

**VO 代理** (`@EntityFileProxy`)：
```java
@EntityFileProxy
public class BlogVO {
    private String title;
    private BigDecimal avgScore;
}
```

### 代理类生成要求

使用 `@EntityProxy` 或 `@EntityFileProxy` 注解后，**必须先编译项目**生成代理类：
```bash
mvn clean compile
```

生成的代理类位于：`target/generated-sources/annotations/`

### Lambda 字段引用

查询时必须使用代理对象的 Lambda 表达式访问字段：
```java
// ✅ 正确：使用 Lambda 表达式
easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.title().like("Spring%"))
    .toList();

// ❌ 错误：直接使用 getter 方法
easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.getTitle().like("Spring%")) // 编译错误
```

## 快速参考

### CRUD 操作速查

| 操作 | 方法 | 示例 |
|------|------|------|
| 查询单条 | `firstOrNull()` | `.where(b -> b.id().eq("123")).firstOrNull()` |
| 查询列表 | `toList()` | `.where(b -> b.score().gt(3.0)).toList()` |
| 插入 | `insertable()` | `easyEntityQuery.insertable(entity).executeRows()` |
| 表达式更新 | `updatable().setColumns()` | `.setColumns(b -> b.title().set("新标题"))` |
| 实体更新 | `updatable(entity)` | `easyEntityQuery.updatable(entity).executeRows()` |
| 删除 | `deletable()` | `.where(b -> b.score().lt(1.0)).executeRows()` |

### Join 操作速查

| 类型 | 方法 | 场景 |
|------|------|------|
| Left Join | `.leftJoin(Class, (a,b) -> condition)` | 保留左表所有数据 |
| Inner Join | `.innerJoin(Class, (a,b) -> condition)` | 只返回匹配数据 |
| Right Join | `.rightJoin(Class, (a,b) -> condition)` | 保留右表所有数据 |

### 关系类型速查

| 注解值 | 关系类型 | 示例 |
|--------|---------|------|
| `OneToOne` | 一对一 | 用户 ↔ 个人资料 |
| `OneToMany` | 一对多 | 主题 → 多篇博客 |
| `ManyToOne` | 多对一 | 博客 → 所属主题 |
| `ManyToMany` | 多对多 | 学生 ↔ 课程 |

## 常见操作模式

### 基础查询

```java
// 单条查询
BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.id().eq("123"))
    .firstOrNull();

// 多条件查询
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> {
        b.title().like("Easy%");
        b.score().gt(new BigDecimal("3.0"));
    })
    .orderBy(b -> b.publishTime().desc())
    .toList();
```

### 多表 Join

```java
// Left Join
easyEntityQuery.queryable(Topic.class)
    .leftJoin(BlogEntity.class, (t, b) -> t.id().eq(b.id()))
    .where((t, b) -> {
        t.id().eq("123");
        b.title().isNotNull();
    })
    .toList();
```

### 差异化更新

```java
TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
try {
    trackManager.begin();
    BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
        .asTracking()
        .whereById("123").firstNotNull();
    blog.setViewCount(blog.getViewCount() + 1);
    easyEntityQuery.updatable(blog).executeRows();
} finally {
    trackManager.release();
}
```

### 分页查询

```java
EasyPageResult<BlogEntity> pageResult = easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.status().eq(1))
    .orderBy(b -> b.publishTime().desc())
    .toPageResult(1, 20);

long total = pageResult.getTotalCount();
List<BlogEntity> list = pageResult.getList();
```

### VO 查询映射

```java
// 定义 VO
@EntityFileProxy
public class BlogVO {
    private String title;
    private BigDecimal avgScore;
}

// 使用 VO 接收结果
List<BlogVO> vos = easyEntityQuery.queryable(BlogEntity.class)
    .groupBy(b -> b.category())
    .select(g -> new BlogVOProxy()
        .title().set(g.key())
        .avgScore().set(g.groupTable().score().avg())
    )
    .toList();
```

## 关系映射配置

使用 `@Navigate` 注解定义实体间关系：

```java
// 一对多
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id",
          targetProperty = "topicId")
private List<BlogEntity> blogs;

// 多对一
@Navigate(value = RelationTypeEnum.ManyToOne,
          selfProperty = "topicId",
          targetProperty = "id")
private Topic topic;

// 一对一
@Navigate(value = RelationTypeEnum.OneToOne,
          selfProperty = "id",
          targetProperty = "userId")
private UserProfile profile;
```

## 高级特性概览

### 隐式 Join（Implicit Join）

自动处理 OneToOne/ManyToOne 关系，无需显式 join：
```java
// 自动生成 LEFT JOIN
easyEntityQuery.queryable(SysUser.class)
    .where(u -> u.company().name().like("阿里"))
    .toList();
```

### 隐式子查询（Implicit Subquery）

自动处理 OneToMany/ManyToMany 关系：
```java
// 自动生成 EXISTS 子查询
easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.name().like("小明")))
    .toList();
```

### 聚合查询

```java
easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.score().gt(new BigDecimal("3.0")))
    .groupBy(b -> GroupKeys.of(b.category()))
    .select(g -> Select.DRAFT.of(
        g.key1(),
        g.groupTable().score().avg(),
        g.groupTable().id().count()
    ))
    .toList();
```

## 常见问题速查

| 问题 | 解决方案 |
|------|---------|
| 找不到 XXXProxy 类 | 执行 `mvn clean compile` 生成代理类 |
| @Column 映射不生效 | VO 中需要使用相同的列名映射 |
| 循环引用序列化问题 | 查询时使用 select 指定字段或添加 JSON 忽略注解 |
| Join 查询性能慢 | 使用 `subQueryToGroupJoin = true` 优化 |

## 完整资源

### 详细文档

- **`references/advanced-features.md`** - 五大隐式特性详解（Implicit Join/Subquery/Grouping/Partition/CASE WHEN）
- **`references/relationship-mapping.md`** - @Navigate 注解完整配置和最佳实践
- **`references/performance-optimization.md`** - 性能优化技巧和常见坑点

### 工作示例

- **`examples/BlogEntity.java`** - 完整的实体类示例
- **`examples/QueryExamples.java`** - 各种查询操作示例
- **`examples/JoinExamples.java`** - 多表 Join 示例
- **`examples/TrackingUpdateExample.java`** - 差异化更新完整示例

## 核心注解位置

- `@EntityProxy` / `@EntityFileProxy`: `sql-core/src/main/java/com/easy/query/core/annotation/`
- `@Table` / `@Column`: `sql-core/src/main/java/com/easy/query/core/annotation/`
- `@Navigate`: `sql-core/src/main/java/com/easy/query/core/annotation/`
- `ProxyEntityAvailable`: `sql-platform/sql-api-proxy/src/main/java/com/easy/query/core/proxy/`
