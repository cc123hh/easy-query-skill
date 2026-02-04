# Easy-Query Performance Optimization Guide

This document summarizes Easy-Query performance optimization techniques and common pitfalls, helping developers write efficient database queries.

## Performance Optimization Checklist

### 1. Query Optimization

#### Use Implicit Grouping Instead of Multiple Subqueries

**Problem Scenario**: When there are multiple query conditions on the same collection

```java
// ❌ Poor performance: Generates multiple EXISTS subqueries
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> {
        c.users().any(u -> u.age().gt(18));
        c.users().any(u -> u.age().lt(60));
        c.users().count().gt(10);
    })
    .toList();

-- Generated SQL (3 subqueries)
SELECT * FROM t_company
WHERE EXISTS (SELECT 1 FROM t_user WHERE company_id = t.id AND age > 18)
  AND EXISTS (SELECT 1 FROM t_user WHERE company_id = t.id AND age < 60)
  AND (SELECT COUNT(*) FROM t_user WHERE company_id = t.id) > 10
```

**Optimization Solution**: Use implicit grouping

```java
// ✅ Good performance: Merged into single GROUP BY
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(c -> c.users())
    .where(c -> {
        c.users().any(u -> u.age().gt(18));
        c.users().any(u -> u.age().lt(60));
        c.users().count().gt(10);
    })
    .toList();

-- Generated SQL (1 JOIN)
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

**Configuration Method**:
```java
// Method 1: Enable during query
.subQueryToGroupJoin(c -> c.users())

// Method 2: Configure in annotation
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;
```

#### Avoid Selecting All Fields

```java
// ❌ Query all fields
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

-- SELECT id, title, content, url, star, publish_time, score, status, ...

// ✅ Query only needed fields
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .title().set(b.title())
    )
    .toList();

-- SELECT id, title FROM t_blog
```

#### Use Pagination to Avoid Large Data Queries

```java
// ❌ Query all data at once
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

// ✅ Use pagination
EasyPageResult<BlogEntity> page = easyEntityQuery.queryable(BlogEntity.class)
    .toPageResult(1, 20);  // 20 records per page
```

### 2. Update Optimization

#### Use Differential Update

**Scenario**: Only update modified fields

```java
// ❌ Full field update
BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
    .whereById("123").firstNotNull();
blog.setTitle("New Title");  // Only changed one field
easyEntityQuery.updatable(blog).executeRows();

-- UPDATE t_blog SET title = ?, content = ?, url = ?, ... WHERE id = ?
-- All fields are updated

// ✅ Differential update
TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
try {
    trackManager.begin();
    BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
        .asTracking()
        .whereById("123").firstNotNull();
    blog.setTitle("New Title");
    easyEntityQuery.updatable(blog).executeRows();
} finally {
    trackManager.release();
}

-- UPDATE t_blog SET title = ? WHERE id = ?
-- Only update modified fields
```

#### Use Batch Update

```java
// ❌ Loop single record update
for (String id : ids) {
    easyEntityQuery.updatable(BlogEntity.class)
        .setColumns(b -> b.status().set(1))
        .where(b -> b.id().eq(id))
        .executeRows();
}

// ✅ Batch update
easyEntityQuery.updatable(BlogEntity.class)
    .setColumns(b -> b.status().set(1))
    .where(b -> b.id().in(ids))
    .executeRows();
```

### 3. Relationship Query Optimization

#### Small Data Volume: Use Implicit Subquery

```java
// Applicable scenario: Related data < 100 records
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();

-- Generates EXISTS subquery, good performance
```

#### Large Data Volume: Use Implicit Grouping

```java
// Applicable scenario: Related data > 1000 records
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(c -> c.users())
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();

-- Generates GROUP BY + LEFT JOIN, avoids multiple subqueries
```

#### Avoid Loop Relationship Queries (N+1 Problem)

```java
// ❌ N+1 problem
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // Queries once per iteration
}
// 1 query for companies + N queries for users

// ✅ Use implicit query
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();
// 1 query completes
```

### 4. Index Optimization Recommendations

#### Create Indexes for Frequently Queried Fields

```sql
-- WHERE condition fields
CREATE INDEX idx_topic_id ON t_blog(topic_id);

-- JOIN condition fields
CREATE INDEX idx_company_id ON t_sys_user(company_id);

-- Sorting fields
CREATE INDEX idx_create_time ON t_blog(create_time);

-- Composite index (leftmost prefix)
CREATE INDEX idx_status_create_time ON t_blog(status, create_time);
```

#### Avoid Index Invalidation

```java
// ❌ Index invalidation: Using function on indexed field
.where(b -> b.title().concat("xxx").eq("xxx"))

// ✅ Index effective: Direct comparison
.where(b -> b.title().eq("xxx"))

// ❌ Index invalidation: Implicit type conversion
.where(b -> b.status().eq("123"))  // status is INT, "123" is STRING

// ✅ Index effective: Type matching
.where(b -> b.status().eq(123))

// ❌ Index invalidation: Prefix fuzzy query
.where(b -> b.title().like("%keyword"))

// ✅ Index effective: Suffix fuzzy query
.where(b -> b.title().like("keyword%"))
```

## Common Performance Pitfalls

### Pitfall 1: Not Enabling subQueryToGroupJoin

**Problem**: In large data volume scenarios, multiple subqueries cause poor performance

**Solution**:
```java
// Enable globally in @Navigate annotation
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;

// Or enable temporarily during query
.subQueryToGroupJoin(c -> c.users())
```

### Pitfall 2: Using getter to Access Related Data After Query

```java
// ❌ N+1 problem
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // Lazy loading, queries once per loop
}

// ✅ Specify needed related data during query
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.age().gt(18)))
    .toList();
```

### Pitfall 3: selectAll() Queries Large Objects

```java
// ❌ Query all fields (including large text)
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class).toList();

// ✅ Query only needed fields
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .title().set(b.title())
        // Don't query content and other large fields
    )
    .toList();
```

### Pitfall 4: Executing Queries in Loop

```java
// ❌ Poor performance
for (String id : ids) {
    BlogEntity blog = easyEntityQuery.queryable(BlogEntity.class)
        .where(b -> b.id().eq(id))
        .firstNotNull();
}

// ✅ Use IN query
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .where(b -> b.id().in(ids))
    .toList();
```

### Pitfall 5: Not Using @Column(ignoreProperties)

```java
// ❌ Queried large fields
@Table("t_blog")
@EntityProxy
public class BlogEntity {
    private String content;  // Large text, queried every time
}

// ✅ Ignore large fields
@Table(value = "t_blog", ignoreProperties = {"content"})
@EntityProxy
public class BlogEntity {
    private String content;  // Not queried by default
}

// Manually query when needed
List<BlogEntity> blogs = easyEntityQuery.queryable(BlogEntity.class)
    .select(b -> new BlogEntityProxy()
        .id().set(b.id())
        .content().set(b.content())  // Only query when needed
    )
    .toList();
```

## Performance Monitoring

### Enable SQL Logging

```yaml
# application.yml
easy-query:
  print-sql: true
  sql-time: true  # Print execution time
```

### Slow Query Analysis

```java
// Queries with execution time > 1s are marked as slow queries
EasyPageResult<BlogEntity> page = easyEntityQuery.queryable(BlogEntity.class)
    .toPageResult(1, 20);

// Check logs
-- Time: 1234 ms
-- SLOW QUERY DETECTED!
SELECT * FROM t_blog LIMIT 20
```

## Performance Optimization Summary

| Scenario | Optimization Solution | Performance Improvement |
|----------|----------------------|------------------------|
| Multiple subqueries | Enable subQueryToGroupJoin | ⬆️⬆️ |
| Query all fields | Use select() to specify fields | ⬆️ |
| N+1 queries | Use implicit queries | ⬆️⬆️⬆️ |
| Loop queries | Use IN batch queries | ⬆️⬆️ |
| Full field update | Use differential update | ⬆️ |
| Loop updates | Use batch updates | ⬆️⬆️ |
| Large field query | Use ignoreProperties | ⬆️ |

Optimization Principles:
1. **Reduce database interactions**
2. **Reduce data transfer volume**
3. **Fully utilize indexes**
4. **Monitor and optimize slow queries**
