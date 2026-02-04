# Easy-Query Five Implicit Features Explained

Easy-Query's core competitive advantage lies in its five implicit features, which can automatically handle complex relationship queries and significantly simplify development work.

## 1. Implicit Join

Automatically handles OneToOne and ManyToOne relationships without explicitly writing JOIN statements.

### Usage Scenarios

When querying objects access related objects through navigation properties, automatically generates LEFT JOIN:

```java
// Automatically generates LEFT JOIN
List<SysUser> users = easyEntityQuery.queryable(SysUser.class)
    .where(user -> {
        user.company().name().like("Alibaba");
        user.company().registerMoney().gt(new BigDecimal("1000000"));
    })
    .orderBy(user -> {
        user.company().registerMoney().desc();
        user.birthday().asc();
    })
    .toList();

-- Generated SQL
SELECT t.*
FROM t_sys_user t
LEFT JOIN t_company t1 ON t.company_id = t1.id
WHERE t1.name LIKE '%Alibaba%'
  AND t1.register_money > 1000000
ORDER BY t1.register_money DESC, t.birthday ASC
```

### Advantages

- Simplifies query syntax, no need to manually write JOIN conditions
- Type-safe, compile-time checking
- Supports multi-level navigation: `user.company().department().name()`

### Notes

- Implicit Join only supports OneToOne and ManyToOne relationships
- OneToMany and ManyToMany require implicit subquery

## 2. Implicit Subquery

Automatically handles OneToMany and ManyToMany relationships, generating EXISTS or IN subqueries.

### Usage Scenarios

```java
// Automatically generates EXISTS subquery
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(company -> {
        company.users().any(u -> u.name().like("Xiaoming"));
        company.users().where(u -> u.name().like("Xiaoming"))
            .max(u -> u.birthday()).gt(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
    })
    .toList();

-- Generated SQL
SELECT t.*
FROM t_company t
WHERE EXISTS (
    SELECT 1
    FROM t_sys_user t1
    WHERE t1.company_id = t.id
      AND t1.name LIKE '%Xiaoming%'
)
AND (
    SELECT MAX(t1.birthday)
    FROM t_sys_user t1
    WHERE t1.company_id = t.id
      AND t1.name LIKE '%Xiaoming%'
) > '2000-01-01 00:00:00'
```

### Supported Methods

- `.any()` - Exists any record matching condition (EXISTS)
- `.all()` - All records satisfy condition
- `.where(condition).max/min/avg/sum/count()` - Aggregate functions

### Advantages

- Automatically generates optimized subqueries
- Supports complex aggregate conditions
- Avoids N+1 query problems

## 3. Implicit Grouping

Merges multiple OneToMany/ManyToMany subqueries into a single group query, improving performance.

### Usage Scenarios

```java
// Multiple subqueries will be merged into one GROUP BY query
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .subQueryToGroupJoin(company -> company.users())
    .where(company -> {
        company.users().any(u -> u.name().like("Xiaoming"));
        company.users().where(u -> u.name().like("Xiaoming"))
            .max(u -> u.birthday()).gt(LocalDateTime.now());
    })
    .toList();

-- Generated SQL (optimized)
SELECT t.*
FROM t_company t
LEFT JOIN (
    SELECT company_id,
           COUNT(*) AS user_count,
           MAX(birthday) AS max_birthday
    FROM t_sys_user
    WHERE name LIKE '%Xiaoming%'
    GROUP BY company_id
) t1 ON t.id = t1.company_id
WHERE t1.user_count > 0
  AND t1.max_birthday > '2024-01-01 00:00:00'
```

### Trigger Conditions

Explicitly enable via `.subQueryToGroupJoin()` method, or configure in `@Navigate` annotation:
```java
@Navigate(value = RelationTypeEnum.OneToMany,
          subQueryToGroupJoin = true)
private List<SysUser> users;
```

### Advantages

- Reduces number of subqueries
- Lowers database load
- Improves performance in large data volume scenarios

### Applicable Scenarios

- Multiple query conditions on the same collection property
- Large data volume scenarios (>1000 related records)

## 4. Implicit Partition Grouping

Supports operations like First/Nth/ElementAt on collections, automatically generating partition window functions.

### Usage Scenarios

```java
// Get the user with the maximum birthday in each company
List<Company> companies = easyEntityQuery.queryable(Company.class)
    .where(company -> {
        company.users().orderBy(u -> u.birthday().desc()).first().name().eq("Xiaoming");
        company.users().orderBy(u -> u.birthday().desc()).element(0)
            .birthday().lt(LocalDateTime.now());
    })
    .toList();

-- Generated SQL (using window function)
SELECT t.*
FROM t_company t
LEFT JOIN (
    SELECT t1.*,
           ROW_NUMBER() OVER (PARTITION BY t1.company_id ORDER BY t1.birthday DESC) AS rn
    FROM t_sys_user t1
) t2 ON t.id = t2.company_id AND t2.rn = 1
WHERE t2.name = 'Xiaoming'
  AND t2.birthday < '2024-01-01 00:00:00'
```

### Supported Methods

- `.first()` - First element (ROW_NUMBER() = 1)
- `.element(index)` - Element at specified index
- `.last()` - Last element

### Database Compatibility

| Database | Window Function Support | Notes |
|----------|------------------------|-------|
| MySQL 8.0+ | ✅ | Full support |
| PostgreSQL | ✅ | Full support |
| Oracle | ✅ | Full support |
| SQL Server | ✅ | Full support |
| MySQL 5.7 | ❌ | Simulated with subquery |

### Advantages

- Simplifies "get first/Nth per group" queries
- Automatically uses optimal window functions
- Avoids application-level loop processing

## 5. Implicit CASE WHEN Expression

Implements conditional aggregation through `.filter()` method, automatically generating CASE WHEN expressions.

### Usage Scenarios

```java
// Count users and average age by region
List<Draft3<String, Long, BigDecimal>> result = easyEntityQuery.queryable(SysUser.class)
    .where(user -> user.birthday().lt(LocalDateTime.now()))
    .select(user -> Select.DRAFT.of(
        user.address(),
        user.id().count().filter(() -> user.address().eq("Hangzhou")),
        user.id().count().filter(() -> user.address().eq("Beijing")),
        user.age().avg().filter(() -> user.address().eq("Beijing"))
    ))
    .toList();

-- Generated SQL
SELECT
    t.address,
    COUNT(CASE WHEN t.address = 'Hangzhou' THEN 1 END) AS value2,
    COUNT(CASE WHEN t.address = 'Beijing' THEN 1 END) AS value3,
    AVG(CASE WHEN t.address = 'Beijing' THEN t.age END) AS value4
FROM t_sys_user t
WHERE t.birthday < '2024-01-01 00:00:00'
```

### Syntax Pattern

```java
// Aggregate function + filter()
property.count().filter(() -> condition)
property.sum().filter(() -> condition)
property.avg().filter(() -> condition)
property.max().filter(() -> condition)
property.min().filter(() -> condition)
```

### Application Scenarios

- Data statistics and reports
- Conditional group statistics
- Dynamic metric calculation

### Advantages

- Simplifies CASE WHEN expression writing
- Type-safe conditional expressions
- Supports all aggregate functions

## Performance Comparison

| Scenario | Traditional SQL | Implicit Feature | Performance Improvement |
|----------|----------------|------------------|------------------------|
| Relation query | Manual LEFT JOIN | Implicit Join | Development efficiency ⬆️ |
| Collection filter | EXISTS subquery | Implicit subquery | SQL optimization ⬆️ |
| Multi-condition collection query | Multiple subqueries | Implicit grouping | Query performance ⬆️⬆️ |
| Group first record | Window function | Implicit partition grouping | Code simplification ⬆️ |
| Conditional aggregation | CASE WHEN | Implicit CASE WHEN | Readability ⬆️ |

## Best Practices

1. **Prioritize implicit features**: Reduce manual SQL writing, improve development efficiency
2. **Use implicit grouping for large data**: Avoid multiple subqueries
3. **Understand generated SQL**: Check SQL correctness through logs
4. **Configure @Navigate reasonably**: Choose whether to enable subQueryToGroupJoin based on scenario
