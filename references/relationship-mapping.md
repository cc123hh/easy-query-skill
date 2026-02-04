# @Navigate 关系映射完整配置

`@Navigate` 注解用于定义实体间的关联关系，是 Easy-Query 隐式查询特性的基础。

## 注解属性详解

### 基本属性

```java
@Navigate(
    value = RelationTypeEnum.OneToMany,    // 关系类型（必填）
    selfProperty = "id",                    // 当前实体关联属性（必填）
    targetProperty = "topicId",             // 目标实体关联属性（必填）
    mappingClass = TopicBlog.class,         // 多对多中间表
    subQueryToGroupJoin = false,            // 子查询转 group join
    required = false                        // 是否必须存在
)
private List<BlogEntity> blogs;
```

### value - 关系类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `OneToOne` | 一对一 | 用户 ↔ 个人资料 |
| `OneToMany` | 一对多 | 主题 → 博客列表 |
| `ManyToOne` | 多对一 | 博客 → 所属主题 |
| `ManyToMany` | 多对多 | 学生 ↔ 课程 |

### selfProperty - 当前实体属性

当前实体中用于关联的属性名：

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id")  // 使用当前实体的 id 字段
private List<BlogEntity> blogs;
```

### targetProperty - 目标实体属性

目标实体中用于关联的属性名：

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id",
          targetProperty = "topicId")  // 关联到 BlogEntity.topicId
private List<BlogEntity> blogs;
```

### mappingClass - 多对多中间表

仅用于 `ManyToMany` 关系，指定中间表实体类：

```java
// Student 实体
@Navigate(
    value = RelationTypeEnum.ManyToMany,
    selfProperty = "id",
    targetProperty = "studentId",
    mappingClass = StudentCourse.class  // 中间表
)
private List<Course> courses;

// Course 实体
@Navigate(
    value = RelationTypeEnum.ManyToMany,
    selfProperty = "id",
    targetProperty = "courseId",
    mappingClass = StudentCourse.class
)
private List<Student> students;

// 中间表实体
@Table("t_student_course")
@EntityProxy
public class StudentCourse {
    @Column(primaryKey = true)
    private String id;
    private String studentId;
    private String courseId;
}
```

### subQueryToGroupJoin - 子查询转分组

当设置为 `true` 时，多个子查询会合并为单个 GROUP BY 查询：

```java
@Navigate(value = RelationTypeEnum.OneToMany,
          selfProperty = "id",
          targetProperty = "companyId",
          subQueryToGroupJoin = true)  // 启用优化
private List<SysUser> users;
```

**适用场景**：
- 同一集合有多个查询条件
- 大数据量场景（>1000 条记录）

**性能对比**：
```sql
-- 未启用（多个子查询）
SELECT * FROM t_company t
WHERE EXISTS (SELECT 1 FROM t_sys_user WHERE company_id = t.id AND age > 18)
  AND EXISTS (SELECT 1 FROM t_sys_user WHERE company_id = t.id AND age < 60)

-- 启用（单个 GROUP BY）
SELECT * FROM t_company t
LEFT JOIN (
    SELECT company_id, COUNT(*) AS cnt
    FROM t_sys_user
    WHERE age > 18 AND age < 60
    GROUP BY company_id
) t1 ON t.id = t1.company_id
WHERE t1.cnt > 0
```

### required - 是否必须存在

控制关联关系是否必须存在，影响生成的 JOIN 类型：

```java
@Navigate(value = RelationTypeEnum.ManyToOne,
          selfProperty = "topicId",
          targetProperty = "id",
          required = true)  // 必须存在 → INNER JOIN
private Topic topic;
```

| required 值 | JOIN 类型 | 说明 |
|------------|----------|------|
| `false`（默认） | LEFT JOIN | 允许关联对象为空 |
| `true` | INNER JOIN | 关联对象必须存在 |

## 关系类型配置示例

### 一对一（OneToOne）

```java
// 用户实体
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

// 用户资料实体
@Table("t_user_profile")
@EntityProxy
public class UserProfile {
    @Column(primaryKey = true)
    private String id;
    private String userId;  // 关联到 SysUser.id
    private String phone;
    private String address;

    @Navigate(value = RelationTypeEnum.OneToOne,
              selfProperty = "userId",
              targetProperty = "id")
    private SysUser user;
}
```

### 一对多（OneToMany）

```java
// 主题实体
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

// 博客实体
@Table("t_blog")
@EntityProxy
public class BlogEntity {
    @Column(primaryKey = true)
    private String id;
    private String topicId;  // 关联到 Topic.id
    private String title;

    @Navigate(value = RelationTypeEnum.ManyToOne,
              selfProperty = "topicId",
              targetProperty = "id")
    private Topic topic;
}
```

### 多对多（ManyToMany）

```java
// 学生实体
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

// 课程实体
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

// 中间表实体
@Table("t_student_course")
@EntityProxy
public class StudentCourse {
    @Column(primaryKey = true)
    private String id;
    private String studentId;  // 关联到 Student.id
    private String courseId;   // 关联到 Course.id
}
```

## 高级配置

### 排序配置

在 `@Navigate` 中配置关联结果的默认排序：

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

### 级联查询配置

配置关联对象是否自动加载：

```java
@Navigate(
    value = RelationTypeEnum.ManyToOne,
    selfProperty = "topicId",
    targetProperty = "id",
    extraSqlArg = "lazy"  // 延迟加载
)
private Topic topic;
```

## 常见问题

### 循环引用问题

双向关联可能导致序列化循环：

```java
// Topic.java
@Navigate(OneToMany)
private List<BlogEntity> blogs;

// BlogEntity.java
@Navigate(ManyToOne)
private Topic topic;
```

**解决方案**：
1. 查询时使用 `.select()` 指定返回字段
2. 在 JSON 序列化注解中忽略循环引用
3. 使用 `@JsonIgnore` 注解

### 性能优化建议

1. **一对一/多对一**：使用隐式 Join，性能良好
2. **一对多/多对多**：
   - 小数据量（<100）：直接使用隐式子查询
   - 大数据量（>1000）：启用 `subQueryToGroupJoin`
3. **多对多查询**：考虑在中间表添加额外字段

### 关联查询最佳实践

```java
// ✅ 推荐：使用隐式查询
easyEntityQuery.queryable(Company.class)
    .where(c -> c.users().any(u -> u.name().like("张三")))
    .toList();

// ⚠️ 可用：显式 JOIN（复杂场景）
easyEntityQuery.queryable(Company.class)
    .leftJoin(SysUser.class, (c, u) -> c.id().eq(u.companyId()))
    .where((c, u) -> u.name().like("张三"))
    .toList();

// ❌ 避免：应用层循环（N+1 问题）
List<Company> companies = easyEntityQuery.queryable(Company.class).toList();
for (Company c : companies) {
    List<SysUser> users = c.getUsers();  // 每次循环都查询一次
}
```
