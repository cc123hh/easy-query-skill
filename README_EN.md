# Easy-Query Expert Skill

An expert skill for the Easy-Query ORM framework, providing professional guidance on type-safe Lambda queries, implicit relationships, differential updates, and more.

## Skill Structure

```
easy-query-expert/
├── SKILL.md                                    # Main file (skill metadata + core content)
├── README.md                                   # This file
├── README_EN.md                                # English version
├── references/                                 # Detailed reference documentation
│   ├── advanced-features.md                   # Five implicit features explained
│   ├── relationship-mapping.md                # @Navigate relationship mapping complete configuration
│   └── performance-optimization.md            # Performance optimization guide
└── examples/                                   # Code examples
    ├── BlogEntity.java                        # Complete entity class example
    ├── QueryExamples.java                     # Various query operation examples
    ├── JoinExamples.java                      # Multi-table Join examples
    └── TrackingUpdateExample.java             # Differential update examples
```

## Use Cases

This skill is automatically triggered when users ask:

- "How to use Easy-Query for database queries"
- "How to use @EntityProxy annotation"
- "Easy-Query multi-table Join queries"
- "Differential Update (Tracking Update)"
- "Easy-Query pagination queries"
- "Proxy class compilation or generation issues"
- "Implicit Join, Implicit Subquery and other advanced features"

## Core Content

### SKILL.md (Main File)

Contains:
- YAML frontmatter (name, description, version)
- Core concepts (proxy pattern, Lambda field references)
- Quick reference tables (CRUD, Join, relationship types)
- Common operation patterns (queries, Joins, differential updates, pagination, VO mapping)
- Relationship mapping configuration
- Advanced features overview
- Common issues cheat sheet

### references/ (Detailed Documentation)

#### advanced-features.md
Explains Easy-Query's five implicit features:
1. **Implicit Join** - Automatically handles OneToOne/ManyToOne
2. **Implicit Subquery** - Automatically handles OneToMany/ManyToMany
3. **Implicit Grouping** - Merges multiple subqueries into GROUP BY
4. **Implicit Partition Grouping** - First/Nth element operations
5. **Implicit CASE WHEN** - Aggregate function filtering

#### relationship-mapping.md
Complete `@Navigate` annotation configuration:
- Basic properties (value, selfProperty, targetProperty)
- Advanced configuration (mappingClass, subQueryToGroupJoin, required)
- Four relationship type configuration examples
- Sorting and cascade query configuration
- Common issues (circular references, performance optimization)

#### performance-optimization.md
Performance optimization guide:
- Query optimization (implicit grouping, field selection, pagination)
- Update optimization (differential updates, batch updates)
- Relationship query optimization
- Index optimization recommendations
- Common performance pitfalls
- Performance monitoring methods

### examples/ (Code Examples)

#### BlogEntity.java
Complete entity class example demonstrating:
- `@EntityProxy`, `@Table`, `@Column` annotations
- `@Navigate` relationship mapping
- Various relationship types (one-to-one, one-to-many, many-to-many)

#### QueryExamples.java
12 query scenarios:
- Basic queries (single record, multi-condition, IN, BETWEEN)
- Group queries, pagination queries
- Field selection, statistical queries
- EXISTS subqueries, UNION queries
- Dynamic table names, CASE WHEN

#### JoinExamples.java
10 Join scenarios:
- Left/Inner/Right Join
- Multi-table Join, Join + Group By
- Implicit Join, Join subqueries
- Join aggregation queries

#### TrackingUpdateExample.java
7 differential update scenarios:
- Basic differential update
- Multi-field, batch updates
- Differential updates in transactions
- Conditional updates, related object updates

## Design Principles

This skill follows [Claude Code Skill Development Best Practices](https://github.com/anthropics/claude-code-plugin-dev):

1. **YAML Frontmatter** - Third-person descriptions, specific trigger phrases
2. **Progressive Disclosure** - Keep SKILL.md concise (~2,000 words), detailed content in references/
3. **Imperative Mood** - Use verb-first instructions, not second-person
4. **Complete Examples** - Code in examples/ can be run directly
5. **Resource References** - SKILL.md explicitly points to references/ and examples/

## Usage

### As a Project-Level Skill

Place this skill in your project's `.claude/skills/` directory:

```
easy-query-project/
└── .claude/
    └── skills/
        └── easy-query-expert/
            ├── SKILL.md
            ├── references/
            └── examples/
```

### As a Plugin Skill

Place this skill in your plugin's `skills/` directory:

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

## Version History

### v1.0.0 (2024-02-04)
- Initial release
- Includes core concepts, five implicit features, performance optimization
- 4 complete code examples
- 3 detailed reference documents

## Contributing

Issues and PRs are welcome to improve this skill!

## License

This skill uses the Apache 2.0 license, consistent with the Easy-Query project.

## Related Links

- [Easy-Query Official Documentation](https://www.easy-query.com/)
- [Easy-Query GitHub](https://github.com/dromara/easy-query)
- [Agent Skills Specification](https://agentskills.io/specification)
