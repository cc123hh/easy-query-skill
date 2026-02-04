# @Navigate Relationship Mapping Complete Configuration

The `@Navigate` annotation is used to define relationships between entities, serving as the foundation for Easy-Query's implicit query features.

## Annotation Properties Explained

### Basic Properties

```java
@Navigate(
    value = RelationTypeEnum.OneToMany,    // Relationship type (required)
    selfProperty = "id",                    // Current entity relation property (required)
    targetProperty = "topicId",             // Target entity relation property (required)
    mappingClass = TopicBlog.class,         // Many-to-many intermediate table
    subQueryToGroupJoin = false,            // Subquery to group join
    required = false                        // Whether must exist
)
private List<BlogEntity> blogs;
```

### value - Relationship Type

| Type | Description | Example |
|------|-------------|---------|
| `OneToOne` | One-to-One | User ↔ Profile |
| `OneToMany` | One-to-Many | Topic → Blog list |
| `ManyToOne` | Many-to-One | Blog →所属 Topic |
| `ManyToMany` | Many-to-Many | Student ↔ Course |

### selfProperty - Current Entity Property

The property name in the current entity used for association:

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id")  // Use current entity's id field
private List<BlogEntity> blogs;
```

### targetProperty - Target Entity Property

The property name in the target entity used for association:

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id",
          targetProperty = "topicId")  // Associate to BlogEntity.topicId
private List<BlogEntity> blogs;
```

### mappingClass - Many-to-Many Intermediate Table

Only used for `ManyToMany` relationships, specifying the intermediate table entity class:

```java
// Student entity
@Navigate(
    value = RelationTypeEnum.ManyToMany,
    selfProperty = "id",
    targetProperty = "studentId",
    mappingClass = StudentCourse.class  // Intermediate table
)
private List<Course> courses;

// Course entity
@Navigate(
    value = RelationTypeEnum.ManyToMany,
    selfProperty = "id",
    targetProperty = "courseId",
    mappingClass = StudentCourse.class
)
private List<Student> students;

// Intermediate table entity
@Table("t_student_course")
@EntityProxy
public class StudentCourse {
    @Column(primaryKey = true)
    private String id;
    private String studentId;
    private String courseId;
}
```

### subQueryToGroupJoin - Subquery to Grouping

When set to `true`, multiple subqueries are merged into a single GROUP BY query:

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id",
          targetProperty = "companyId",
          subQueryToGroupJoin = true)  // Enable optimization
private List<SysUser> users;
```

**Applicable Scenarios**:
- Multiple query conditions on the same collection
- Large data volume scenarios (>1000 records)

**Performance Comparison**:
```sql
-- Not enabled (multiple subqueries)
SELECT * FROM t_company t
WHERE EXISTS (SELECT 1 FROM t_sys_user WHERE company_id = t.id AND age > 18)
  AND EXISTS (SELECT 1 FROM t_sys_user WHERE company_id = t.id AND age < 60)

-- Enabled (single GROUP BY)
SELECT * FROM t_company t
LEFT JOIN (
    SELECT company_id, COUNT(*) AS cnt
    FROM t_sys_user
    WHERE age > 18 AND age < 60
    GROUP BY company_id
) t1 ON t.id = t1.company_id
WHERE t1.cnt > 0
```

### required - Must Exist

Controls whether the relationship must exist, affecting the generated JOIN type:

```java
@Navigate(value = RelationTypeEnum.ManyToOne,
          selfProperty = "topicId",
          targetProperty = "id",
          required = true)  // Must exist → INNER JOIN
private Topic topic;
```

| required Value | JOIN Type | Description |
|---------------|-----------|-------------|
| `false` (default) | LEFT JOIN | Allow related object to be null |
| `true` | INNER JOIN | Related object must exist |

## Relationship Type Configuration Examples

### One-to-One (OneToOne)

```java
// User entity
@Table("t_sys_user")
@EntityProxy
public class SysUser {
    @Column(primaryKey = true)
    private String id;
    private String username;

    @Navigate(value = RelationTypeEnum.OneToOne,
              selfProperty = "id",
              targetProperty = "userId")
    private UserProfile profile;
}

// User profile entity
@Table("t_user_profile")
@EntityProxy
public class UserProfile {
    @Column(primaryKey = true)
    private String id;
    private String userId;  // Associates to SysUser.id
    private String phone;
    private String address;

    @Navigate(value = RelationTypeEnum.OneToOne,
              selfProperty = "userId",
              targetProperty = "id")
    private SysUser user;
}
```

### One-to-Many (OneToMany)

```java
// Topic entity
@Table("t_topic")
@EntityProxy
public class Topic {
    @Column(primaryKey = true)
    private String id;
    private String title;

    @Navigate(value = RelationTypeEnum.OneToMany,
              selfProperty = "id",
              targetProperty = "topicId")
    private List<BlogEntity> blogs;
}

// Blog entity
@Table("t_blog")
@EntityProxy
public class BlogEntity {
    @Column(primaryKey = true)
    private String id;
    private String topicId;  // Associates to Topic.id
    private String title;

    @Navigate(value = RelationTypeEnum.ManyToOne,
              selfProperty = "topicId",
              targetProperty = "id")
    private Topic topic;
}
```

### Many-to-Many (ManyToMany)

```java
// Student entity
@Table("t_student")
@EntityProxy
public class Student {
    @Column(primaryKey = true)
    private String id;
    private String name;

    @Navigate(
        value = RelationTypeEnum.ManyToMany,
        selfProperty = "id",
        targetProperty = "studentId",
        mappingClass = StudentCourse.class
    )
    private List<Course> courses;
}

// Course entity
@Table("t_course")
@EntityProxy
public class Course {
    @Column(primaryKey = true)
    private String id;
    private String name;

    @Navigate(
        value = RelationTypeEnum.ManyToMany,
        selfProperty = "id",
        targetProperty = "courseId",
        mappingClass = StudentCourse.class
    )
    private List<Student> students;
}

// Intermediate table entity
@Table("t_student_course")
@EntityProxy
public class StudentCourse {
    @Column(primaryKey = true)
    private String id;
    private String studentId;  // Associates to Student.id
    private String courseId;   // Associates to Course.id
}
```

## Advanced Configuration

### Sorting Configuration

Configure default sorting for related results in `@Navigate`:

```java
@Navigate(
    value = RelationTypeEnum.OneToMany,
    selfProperty = "id",
    targetProperty = "topicId",
    orderByProps = {
        @OrderByProperty(property = "createTime", asc = false),
        @OrderByProperty(property = "id", asc = true)
    }
)
private List<BlogEntity> blogs;
```

### Cascade Query Configuration

Configure whether related objects are automatically loaded:

```java
@Navigate(
    value = RelationTypeEnum.ManyToOne,
    selfProperty = "topicId",
    targetProperty = "id",
    extraSqlArg = "lazy"  // Lazy loading
)
private Topic topic;
```

## Common Issues

### Circular Reference Problem

Bidirectional relationships may cause serialization loops:

```java
// Topic.java
@Navigate(OneToMany)
private List<BlogEntity> blogs;

// BlogEntity.java
@Navigate(ManyToOne)
private Topic topic;
```

**Solutions**:
1. Use `.select()` to specify return fields when querying
2. Ignore circular references in JSON serialization annotations
3. Use `@JsonIgnore` annotation

### Performance Optimization Recommendations

1. **One-to-One/Many-to-One**: Use implicit Join, good performance
2. **One-to-Many/Many-to-Many**:
   - Small data volume (<100): Directly use implicit subquery
   - Large data volume (>1000): Enable `subQueryToGroupJoin`
3. **Many-to-Many queries**: Consider adding extra fields to intermediate table

### Relationship Query Best Practices

```java
// ✅ Recommended: Use implicit query
easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.name().like("Zhang San")))
    .toList();

// ⚠️ Available: Explicit JOIN (complex scenarios)
easyEntityQuery.queryable(Company.class)
    .leftJoin(SysUser.class, (c, u) -> c.id().eq(u.companyId()))
    .where((c, u) -> u.name().like("Zhang San"))
    .toList();

// ❌ Avoid: Application layer loop (N+1 problem)
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // Queries once per loop
}
```
