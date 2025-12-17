<!--
=== 同步影响报告 (Sync Impact Report) ===
版本变更: 0.0.0 → 1.0.0 (MAJOR - 首次制定项目治理原则)
新增原则:
  - I. 代码质量 (Code Quality)
  - II. 测试标准 (Testing Standards)
  - III. 用户体验一致性 (User Experience Consistency)
  - IV. 性能要求 (Performance Requirements)
新增章节:
  - 核心原则 (Core Principles)
  - 技术规范 (Technical Specifications)
  - 开发流程 (Development Workflow)
  - 治理 (Governance)
模板更新状态:
  - ✅ plan-template.md - 无需更新（已包含性能目标和约束）
  - ✅ spec-template.md - 无需更新（已包含用户场景和测试要求）
  - ✅ tasks-template.md - 无需更新（已包含测试任务和质量检查点）
待办事项: 无
===
-->

# Mini-Spring 项目准则

## 核心原则

### I. 代码质量 (Code Quality)

代码必须保持清晰、可读、可维护的高标准，遵循Spring框架的设计模式和最佳实践。

**强制要求**:
- 所有类和公共方法必须有清晰的JavaDoc注释，说明用途和使用方式
- 代码必须遵循统一的命名规范：类名使用PascalCase，方法和变量使用camelCase
- 每个类必须职责单一（Single Responsibility Principle），核心逻辑不超过200行
- 必须使用有意义的变量名，禁止使用单字母变量（循环变量除外）
- 代码复杂度（Cyclomatic Complexity）不得超过10
- 必须处理所有受检异常，禁止空catch块

**理由**: mini-spring作为学习Spring源码的教育项目，代码的可读性和清晰度直接影响学习效果。

### II. 测试标准 (Testing Standards)

测试是验证实现正确性的核心手段，必须覆盖所有关键功能路径。

**强制要求**:
- 所有核心功能（IoC、AOP、事件监听等）必须有对应的单元测试
- 测试覆盖率目标：核心模块 ≥ 80%，工具类 ≥ 70%
- 每个测试方法必须遵循Given-When-Then模式，清晰表达测试意图
- 测试必须独立可运行，不依赖外部状态或执行顺序
- 新增功能必须先编写测试用例，确保测试失败后再实现功能（TDD）
- 修复Bug必须附带能复现问题的测试用例

**理由**: 作为框架项目，正确性至关重要。测试既是质量保障，也是活文档。

### III. 用户体验一致性 (User Experience Consistency)

API设计和使用方式必须与Spring Framework保持一致，降低学习者的认知负担。

**强制要求**:
- 公共API的命名和参数顺序必须与Spring Framework一致
- 注解的使用方式（如@Autowired、@Value）必须与Spring行为兼容
- 异常信息必须清晰描述问题原因和可能的解决方案
- XML配置文件格式必须与Spring标准schema兼容
- 所有配置项必须有合理的默认值，支持零配置启动
- README和changelog必须及时更新，每个功能点都有对应说明

**理由**: 学习者期望mini-spring的行为与真实Spring一致，一致性能帮助学习者建立正确的心智模型。

### IV. 性能要求 (Performance Requirements)

虽然是简化版框架，但核心操作的性能必须在可接受范围内。

**强制要求**:
- Bean初始化时间：单个Bean创建不超过10ms
- 依赖注入解析：单次依赖查找不超过1ms
- 内存效率：框架本身内存开销不超过50MB
- 启动时间：ApplicationContext初始化（100个Bean）不超过500ms
- 禁止在热路径中使用反射缓存未命中的操作
- 所有集合操作必须选择适当的数据结构（如使用ConcurrentHashMap处理并发场景）

**理由**: 性能问题会掩盖设计问题，同时学习者需要理解Spring的性能优化策略。

## 技术规范

**语言/版本**: Java 8+（兼容JDK 8-21）
**构建工具**: Maven 3.6+
**核心依赖**: cglib (动态代理), AspectJ (AOP织入), dom4j (XML解析)
**测试框架**: JUnit 5
**代码风格**: 遵循Google Java Style Guide
**目标平台**: JVM (跨平台)

## 开发流程

**代码审查要求**:
- 所有变更必须通过Pull Request提交
- PR必须包含变更说明和对应的测试
- 核心模块变更需要至少一位维护者审批

**质量门禁**:
- 编译通过（mvn compile）
- 所有测试通过（mvn test）
- 代码风格检查通过
- 无新增编译警告

**发布流程**:
- 版本号遵循语义化版本规范（MAJOR.MINOR.PATCH）
- 每次发布必须更新changelog.md
- 重要变更必须在README中说明

## 治理

本准则是项目开发的最高指导原则，所有代码变更和设计决策必须符合上述原则。

**修订程序**:
- 原则修订需要在Issue中提出并讨论
- 修订必须记录变更理由和影响范围
- 重大修订需要同步更新相关模板和文档

**合规检查**:
- 每次PR审查必须验证是否符合核心原则
- 复杂度超标必须提供书面理由
- 性能相关变更必须附带基准测试数据

**Version**: 1.0.0 | **Ratified**: 2025-12-16 | **Last Amended**: 2025-12-16
