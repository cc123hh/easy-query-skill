# Easy-Query Expert Skill

Easy-Query ORM 框架专家 Skill，提供类型安全的 Lambda 查询、隐式关联、差异化更新等功能的专业指导。

[English](README_EN.md) | 简体中文

## Skill 结构

```
easy-query-expert/
├── SKILL.md                                    # 主文件（技能元数据 + 核心内容）
├── README.md                                   # 本文件
├── references/                                 # 详细参考文档
│   ├── advanced-features.md                   # 五大隐式特性详解
│   ├── relationship-mapping.md                # @Navigate 关系映射完整配置
│   └── performance-optimization.md            # 性能优化指南
└── examples/                                   # 代码示例
    ├── BlogEntity.java                        # 完整实体类示例
    ├── QueryExamples.java                     # 各种查询操作示例
    ├── JoinExamples.java                      # 多表 Join 示例
    └── TrackingUpdateExample.java             # 差异化更新示例
```

## 适用场景

当用户询问以下问题时，此 Skill 会被自动触发：

- "如何使用 Easy-Query 进行数据库查询"
- "@EntityProxy 注解怎么用"
- "Easy-Query 多表 Join 联查"
- "差异化更新（Tracking Update）"
- "Easy-Query 分页查询"
- "代理类编译或生成问题"
- "隐式 Join、隐式子查询等高级特性"

## 核心内容

### SKILL.md（主文件）

包含：
- YAML frontmatter（name, description, version）
- 核心概念（代理模式、Lambda 字段引用）
- 快速参考表（CRUD、Join、关系类型）
- 常见操作模式（查询、Join、差异化更新、分页、VO 映射）
- 关系映射配置
- 高级特性概览
- 常见问题速查

### references/（详细文档）

#### advanced-features.md
详解 Easy-Query 的五大隐式特性：
1. **隐式 Join** - 自动处理 OneToOne/ManyToOne
2. **隐式子查询** - 自动处理 OneToMany/ManyToMany
3. **隐式分组** - 多子查询合并为 GROUP BY
4. **隐式分区分组** - First/Nth 元素操作
5. **隐式 CASE WHEN** - 聚合函数过滤

#### relationship-mapping.md
`@Navigate` 注解完整配置：
- 基本属性（value, selfProperty, targetProperty）
- 高级配置（mappingClass, subQueryToGroupJoin, required）
- 四种关系类型配置示例
- 排序和级联查询配置
- 常见问题（循环引用、性能优化）

#### performance-optimization.md
性能优化指南：
- 查询优化（隐式分组、字段选择、分页）
- 更新优化（差异化更新、批量更新）
- 关系查询优化
- 索引优化建议
- 常见性能坑点
- 性能监控方法

### examples/（代码示例）

#### BlogEntity.java
完整的实体类示例，演示：
- `@EntityProxy`、`@Table`、`@Column` 注解
- `@Navigate` 关系映射
- 各种关系类型（一对一、一对多、多对多）

#### QueryExamples.java
12 种查询场景：
- 基础查询（单条、多条件、IN、BETWEEN）
- 分组查询、分页查询
- 字段选择、统计查询
- EXISTS 子查询、UNION 查询
- 动态表名、CASE WHEN

#### JoinExamples.java
10 种 Join 场景：
- Left/Inner/Right Join
- 多表 Join、Join + Group By
- 隐式 Join、Join 子查询
- Join 聚合查询

#### TrackingUpdateExample.java
7 种差异化更新场景：
- 基础差异化更新
- 多字段、批量更新
- 事务中的差异化更新
- 条件更新、关联对象更新

## 设计原则

此 Skill 遵循 [Claude Code Skill Development Best Practices](https://github.com/anthropics/claude-code-plugin-dev):

1. **YAML Frontmatter** - 第三人称描述，具体触发短语
2. **渐进式披露** - SKILL.md 保持精简（~2,000 字），详细内容在 references/
3. **祈使语气** - 使用动词优先的指令，而非第二人称
4. **完整示例** - examples/ 中的代码可直接运行
5. **资源引用** - SKILL.md 明确指向 references/ 和 examples/

## 使用方式

### 作为项目级 Skill

将此 Skill 放在项目的 `.claude/skills/` 目录下：

```
easy-query-project/
└── .claude/
    └── skills/
        └── easy-query-expert/
            ├── SKILL.md
            ├── references/
            └── examples/
```

### 作为插件 Skill

将此 Skill 放在插件的 `skills/` 目录下：

```
easy-query-plugin/
├── .claude-plugin/
│   └── plugin.json
└── skills/
    └── easy-query-expert/
        ├── SKILL.md
        ├── references/
        └── examples/
```

## 版本历史

### v1.0.0 (2024-02-04)
- 初始版本
- 包含核心概念、五大隐式特性、性能优化
- 4 个完整代码示例
- 3 个详细参考文档

## 贡献

欢迎提交 Issue 和 PR 来改进此 Skill！

## 许可

此 Skill 使用 Apache 2.0 许可证，与 Easy-Query 项目保持一致。

## 相关链接

- [Easy-Query 官方文档](https://www.easy-query.com/)
- [Easy-Query GitHub](https://github.com/dromara/easy-query)
