# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Purpose

This is an **Easy-Query ORM Expert Skill** for Claude Code - a standalone skill that provides comprehensive guidance on the Easy-Query Java ORM framework. It is NOT an Easy-Query implementation itself, but rather educational content structured as a Claude Code skill.

## Architecture

### Progressive Disclosure Structure

This skill follows a three-tier documentation architecture designed for efficient knowledge retrieval:

1. **SKILL.md** (Primary entry point)
   - YAML frontmatter with metadata for skill discovery
   - Core concepts and quick reference tables
   - Common operation patterns with code examples
   - Links to detailed resources

2. **references/** (Deep-dive documentation)
   - `advanced-features.md` - Five implicit features (Implicit Join/Subquery/Grouping/Partition/CASE WHEN)
   - `relationship-mapping.md` - Complete @Navigate annotation configuration
   - `performance-optimization.md` - Performance tips and common pitfalls

3. **examples/** (Executable code)
   - `BlogEntity.java` - Complete entity class with all annotations
   - `QueryExamples.java` - 12 query scenarios
   - `JoinExamples.java` - 10 join operations
   - `TrackingUpdateExample.java` - 7 differential update scenarios

### Key Architectural Concept: Proxy Pattern

Easy-Query's core differentiator is compile-time proxy class generation via APT (Annotation Processing Tool). Understanding this is critical:

- **Entity Proxy** (`@EntityProxy`): Generated for database entities
- **VO Proxy** (`@EntityFileProxy`): Generated for value objects
- **Lambda Field References**: All queries MUST use proxy object Lambda expressions, NOT getter methods
- **Generation Location**: `target/generated-sources/annotations/`

### Five Implicit Features (Core Competitive Advantage)

These features automatically handle complex relationship queries - this is the main value proposition:

1. **Implicit Join**: Auto-generates LEFT JOIN for OneToOne/ManyToOne navigation
2. **Implicit Subquery**: Auto-generates EXISTS/IN subqueries for OneToMany/ManyToMany
3. **Implicit Grouping**: Merges multiple subqueries into single GROUP BY (performance optimization)
4. **Implicit Partition**: Uses window functions for First/Nth/Last operations
5. **Implicit CASE WHEN**: `.filter()` method for conditional aggregation

## Content Localization Strategy

**README.md is in Chinese** (project description for Chinese-speaking users)
**All other files are in English** (SKILL.md, references/, examples/)

This bilingual approach supports both local and international audiences.

## Working with This Skill

### When Making Changes

1. **SKILL.md updates**: Keep concise (~7,600 bytes). Move detailed explanations to `references/`
2. **Code examples**: Ensure all examples are compilable and use correct proxy syntax
3. **Feature additions**: Add quick reference to SKILL.md, detailed explanation in `references/`
4. **Version updates**: Update both frontmatter in SKILL.md and version history in README.md

### Critical Invariants

- **Proxy class compilation**: All code examples must use `b.title()` NOT `b.getTitle()`
- **Compile-first requirement**: Proxy classes don't exist until `mvn clean compile` runs
- **Relationship mapping**: Always use `@Navigate` annotation - never manual joins unless necessary
- **Performance mindset**: Default to implicit features, optimize with `subQueryToGroupJoin` for large datasets

### Common Pitfalls to Avoid

1. Don't add verbose explanations to SKILL.md - use `references/` instead
2. Don't mix Chinese and English in technical documentation (except README.md)
3. Don't create examples that require external setup beyond Easy-Query dependencies
4. Don't forget to update both SKILL.md frontmatter and README.md version history

## Integration Points

This skill can be integrated in three ways:
1. **Project-local**: `.claude/skills/easy-query-expert/`
2. **Plugin**: Plugin's `skills/easy-query-expert/` directory
3. **Submodule**: Git submodule in `.claude/skills/`

No build commands, test commands, or runtime dependencies - this is pure documentation/markdown content.
