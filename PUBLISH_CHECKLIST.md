# Easy-Query Expert Skill 发布清单

本文档提供将 easy-query-expert skill 发布到独立 GitHub 仓库所需的详细步骤。

## 前置条件

- [ ] 拥有 GitHub 账户
- [ ] 已验证 SKILL.md 符合 Agent Skills 规范
- [ ] 确定仓库名称（建议：`easy-query-expert-skill`）
- [ ] 确定许可证（已选择：Apache 2.0）

## 第一步：验证规范符合性

### Frontmatter 验证

当前 SKILL.md frontmatter：

```yaml
---
name: easy-query-expert
description: This skill should be used when the user asks to "use Easy-Query", "write an Easy-Query query", "@EntityProxy annotation", "Easy-Query join query", "Easy-Query tracking update", "Easy-Query pagination", or mentions "proxy class generation", "implicit join", "implicit subquery", "entity navigation". Provides comprehensive guidance for the Easy-Query ORM framework including type-safe Lambda queries, implicit relationships, tracking updates, and proxy pattern usage.
version: 1.0.0
---
```

### 规范检查结果

| 字段 | 要求 | 当前值 | 状态 |
|------|------|--------|------|
| `name` | 1-64 字符，小写字母、数字、连字符 | `easy-query-expert` | ✅ 符合 |
| `name` | 不能以连字符开头或结尾 | 不以连字符开头/结尾 | ✅ 符合 |
| `name` | 不能包含连续连字符 | 无连续连字符 | ✅ 符合 |
| `description` | 1-1024 字符 | 468 字符 | ✅ 符合 |
| `version` | 建议使用语义化版本 | `1.0.0` | ✅ 符合 |

### 建议的 Frontmatter 增强

为了更符合 Agent Skills 规范，建议添加可选字段：

```yaml
---
name: easy-query-expert
description: This skill should be used when the user asks to "use Easy-Query", "write an Easy-Query query", "@EntityProxy annotation", "Easy-Query join query", "Easy-Query tracking update", "Easy-Query pagination", or mentions "proxy class generation", "implicit join", "implicit subquery", "entity navigation". Provides comprehensive guidance for the Easy-Query ORM framework including type-safe Lambda queries, implicit relationships, tracking updates, and proxy pattern usage.
version: 1.0.0
license: Apache-2.0
compatibility:
  - claude-code
  - claude-api
metadata:
  tags:
    - orm
    - java
    - database
    - query
    - lambda
  category: development
---
```

## 第二步：准备发布文件

### 已创建的文件

- [x] **SKILL.md** - 主文件（已存在）
- [x] **README.md** - 仓库说明（已存在）
- [x] **LICENSE** - Apache 2.0 许可证（已创建）
- [x] **.gitignore** - Git 忽略配置（已创建）
- [x] **references/** - 详细参考文档（已存在）
  - advanced-features.md
  - relationship-mapping.md
  - performance-optimization.md
- [x] **examples/** - 代码示例（已存在）
  - BlogEntity.java
  - QueryExamples.java
  - JoinExamples.java
  - TrackingUpdateExample.java

### 目录结构确认

```
easy-query-expert-skill/
├── SKILL.md                                    # ✅ 主文件
├── README.md                                   # ✅ 仓库说明
├── LICENSE                                     # ✅ Apache 2.0 许可证
├── .gitignore                                  # ✅ Git 配置
├── PUBLISH_CHECKLIST.md                        # ✅ 本文件
├── references/                                 # ✅ 详细参考文档
│   ├── advanced-features.md
│   ├── relationship-mapping.md
│   └── performance-optimization.md
└── examples/                                   # ✅ 代码示例
    ├── BlogEntity.java
    ├── QueryExamples.java
    ├── JoinExamples.java
    └── TrackingUpdateExample.java
```

## 第三步：创建 GitHub 仓库

### 1. 在 GitHub 创建新仓库

- [ ] 访问 https://github.com/new
- [ ] 仓库名称：`easy-query-expert-skill`
- [ ] 描述：`Easy-Query ORM framework expert skill for Claude Code and Claude API`
- [ ] 可见性：Public
- [ ] **不要**初始化 README、.gitignore 或 LICENSE（已有这些文件）

### 2. 设置仓库主题

- [ ] 访问仓库设置
- [ ] 添加主题标签：`documentation`, `java`, `orm`, `database`, `claude`, `agent-skills`

### 3. 创建分支保护（可选）

- [ ] 保护 `main` 分支
- [ ] 要求 PR review
- [ ] 启用状态检查

## 第四步：推送代码到 GitHub

### 方法 1：使用 SSH（推荐）

```bash
cd /Users/cc123hh/Projects/easy-query/.claude/skills/easy-query-expert
git init
git add .
git commit -m "Initial commit: Easy-Query Expert Skill v1.0.0

- Add comprehensive Easy-Query ORM guidance
- Include 3 detailed reference documents
- Provide 4 complete code examples
- Follow Agent Skills specification
- Apache 2.0 license"

git remote add origin git@github.com:YOUR_USERNAME/easy-query-expert-skill.git
git branch -M main
git push -u origin main
```

### 方法 2：使用 HTTPS

```bash
cd /Users/cc123hh/Projects/easy-query/.claude/skills/easy-query-expert
git init
git add .
git commit -m "Initial commit: Easy-Query Expert Skill v1.0.0

- Add comprehensive Easy-Query ORM guidance
- Include 3 detailed reference documents
- Provide 4 complete code examples
- Follow Agent Skills specification
- Apache 2.0 license"

git remote add origin https://github.com/YOUR_USERNAME/easy-query-expert-skill.git
git branch -M main
git push -u origin main
```

**记得将 `YOUR_USERNAME` 替换为你的 GitHub 用户名！**

## 第五步：配置仓库

### 1. 设置仓库描述

在仓库根目录添加 **README.md**（已存在，内容完善）：

- ✅ 清晰的项目描述
- ✅ 使用方式和场景
- ✅ 文档结构说明
- ✅ 许可证信息

### 2. 启用 GitHub Pages（可选）

如果想要展示为网站：

- [ ] 访问仓库 Settings → Pages
- [ ] Source: Deploy from a branch
- [ ] Branch: main / (root)
- [ ] 选择主题

### 3. 添加仓库标签

在 About 部分添加：
- `documentation`
- `java`
- `orm`
- `database`
- `claude`
- `agent-skills`

### 4. 设置社区健康文件（可选）

- [ ] **CONTRIBUTING.md** - 贡献指南
- [ ] **CODE_OF_CONDUCT.md** - 行为准则
- [   **SECURITY.md** - 安全政策
- [ ] **SUPPORT.md** - 支持指南

## 第六步：提交到 Agent Skills 生态

### 选项 1：添加到 skills.sh（如果可用）

```bash
# 检查 skills.sh 是否支持提交
curl https://skills.sh/api/submit \
  -H "Content-Type: application/json" \
  -d '{
    "name": "easy-query-expert",
    "repository": "https://github.com/YOUR_USERNAME/easy-query-expert-skill",
    "version": "1.0.0",
    "description": "Easy-Query ORM framework expert skill"
  }'
```

### 选项 2：提交到 Agent Skills Registry（如果存在）

- [ ] 访问 Agent Skills 官方网站
- [ ] 提交 skill 到目录
- [ ] 等待审核

### 选项 3：添加到 Claude Code Plugin Dev（如果适用）

- [ ] Fork https://github.com/anthropics/claude-code-plugin-dev
- [ ] 添加到 skills 目录
- [ ] 提交 PR

## 第七步：推广和维护

### 推广

- [ ] 在 Easy-Query 社区分享
- [ ] 发布到社交媒体（Twitter、LinkedIn）
- [ ] 写博客文章介绍 skill
- [ ] 提交到 Awesome Lists

### 维护

- [ ] 定期更新示例代码
- [ ] 回答 Issues 和 PRs
- [ ] 跟踪 Easy-Query 框架更新
- [ ] 发布新版本

## 第八步：集成到项目中

### 使用方案 1：作为项目本地 skill

```bash
# 复制到项目目录
cp -r /path/to/easy-query-expert-skill \
      /path/to/your-project/.claude/skills/
```

### 使用方案 2：作为 Git Submodule

```bash
cd your-project/.claude/skills
git submodule add https://github.com/YOUR_USERNAME/easy-query-expert-skill.git
```

### 使用方案 3：作为 Claude Code Plugin

在插件的 `skills/` 目录中添加：

```json
{
  "skills": [
    {
      "name": "easy-query-expert",
      "path": "skills/easy-query-expert"
    }
  ]
}
```

## 第九步：版本管理

### 发布 v1.0.0

```bash
git tag -a v1.0.0 -m "Release v1.0.0: Initial stable release"
git push origin v1.0.0
```

### 创建 GitHub Release

- [ ] 访问仓库 → Releases → Create a new release
- [ ] Tag: `v1.0.0`
- [ ] Title: `v1.0.0 - Initial Stable Release`
- [ ] Description: 包含 Release Notes
- [ ] 发布为 Pre-release：否

## 第十步：验证和测试

### 本地验证

- [ ] 在测试项目中安装 skill
- [ ] 测试所有触发短语
- [ ] 验证代理类生成示例
- [ ] 测试所有查询示例

### 远程验证

- [ ] 从 GitHub 克隆 skill
- [ ] 在新项目中测试
- [ ] 检查所有链接是否有效
- [ ] 验证代码示例可编译

## 检查清单总结

### 文件准备
- [x] SKILL.md（主文件）
- [x] README.md（仓库说明）
- [x] LICENSE（Apache 2.0）
- [x] .gitignore（Git 配置）
- [x] references/（详细文档）
- [x] examples/（代码示例）

### 规范验证
- [x] name 字段符合要求
- [x] description 字段符合要求
- [x] 可选字段添加

### 仓库设置
- [ ] 创建 GitHub 仓库
- [ ] 推送代码
- [ ] 配置仓库设置
- [ ] 添加仓库标签

### 生态集成
- [ ] 提交到 skills.sh（如可用）
- [ ] 提交到 Agent Skills Registry（如可用）
- [ ] 提交到 Claude Code Plugin Dev（如适用）

### 发布
- [ ] 创建 Git 标签
- [ ] 创建 GitHub Release
- [ ] 推广和分享

## 参考资料

- [Agent Skills Specification](https://agentskills.io/specification)
- [Claude Code Plugin Development](https://github.com/anthropics/claude-code-plugin-dev)
- [Easy-Query Documentation](https://www.easy-query.com/)
- [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0)

## 快速命令参考

```bash
# 初始化仓库
git init
git add .
git commit -m "Initial commit: Easy-Query Expert Skill v1.0.0"
git remote add origin git@github.com:YOUR_USERNAME/easy-query-expert-skill.git
git branch -M main
git push -u origin main

# 创建发布标签
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# 作为 submodule 添加到项目
cd your-project/.claude/skills
git submodule add https://github.com/YOUR_USERNAME/easy-query-expert-skill.git
```

## 注意事项

1. **替换占位符**：将 `YOUR_USERNAME` 替换为实际的 GitHub 用户名
2. **审核时间**：提交到 Agent Skills 生态可能需要审核时间
3. **版本更新**：每次更新后记得更新 SKILL.md 中的版本号
4. **维护**：定期更新示例代码和文档以跟上 Easy-Query 框架的发展

---

创建日期：2026-02-05
最后更新：2026-02-05
