<p align="center">
	<img alt="Luck-Report" src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/header-01.png" width="64" data-local-src="/images-plus/git/header-01.png">
</p>
<h1 align="center" style="margin: 20px 0; font-weight: bold;">Luck-Report V2.0.8</h1>
<h4 align="center">中国式复杂报表引擎 · 内置 AI 智能助手</h4>
<p align="center">
	<a href="https://gitee.com/LuckyPools/luck-report-server/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-green.svg"></a>
	<a href="https://gitee.com/LuckyPools/luck-report-server/stargazers"><img src="https://gitee.com/LuckyPools/luck-report-server/badge/star.svg"></a>
	<a href="https://gitee.com/LuckyPools/luck-report-server/members"><img src="https://img.shields.io/badge/Fork%20on%20Gitee-Click%20Here-blue"></a>
</p>
<p align="center">
	[<a href="./README.md">中文</a>] | [<a href="./README_EN.md">English</a>]
</p>

## 📖 项目简介

Luck-Report 是一款面向**中国式复杂报表**的开源报表引擎。基于**单元格迭代**，可在浏览器中完成交叉表、分组表、主从表等复杂版式的设计、预览与导出。

Luck-Report 内置 **AI 智能助手**，可结合**大模型配置**与**知识库**，用自然语言完成制表、改表与答疑。后端基于 **Spring Boot**，支持独立部署。项目由开源 **UReport2** 重构而来，在保留中国式报表能力的基础上，补齐现代化架构与 AI 辅助能力。

本仓库为报表引擎后端源码，基于 **Apache-2.0** 开源，**可免费商用**。

## ✨ 核心能力

| 能力 | 说明 |
|------|------|
| 中国式复杂报表 | 基于单元格迭代模型，交叉表、分组表、主从表、套打等场景可在网页设计器中完成 |
| AI 智能助手 | 自然语言制表、改表与答疑，支持深度思考 |
| RAG 知识库 | 报表知识库与业务知识库；PDF / Word / TXT / Markdown / 问答向量化检索；混合多路召回与可选 Rerank |
| 报表管理    | 提供管理页维护报表，支持角色权限与预览授权 |
| 项目集成    | 可通过 Starter 集成进后端项目 |

## 🌐 相关资源

| 资源 | 地址 |
|------|------|
| 在线体验 | [https://www.tinyluck.cn:8060/login](https://www.tinyluck.cn:8060/login) |
| 技术文档 | [https://www.tinyluck.cn:8099/se/docs](https://www.tinyluck.cn:8099/se/docs) |
| 源码仓库 | [https://gitee.com/LuckyPools/luck-report-server](https://gitee.com/LuckyPools/luck-report-server) |
| 问题反馈 | [Gitee Issues](https://gitee.com/LuckyPools/luck-report-server/issues) |

## 💻 系统要求

| 环境 | 版本要求 |
|------|----------|
| JDK | >= 1.8 |
| Maven | >= 3.6 |
| MySQL | >= 5.7 |

> Redis、向量库仅在启用对应插件 / 配置时需要。

### 数据库兼容

| 类型 | 支持 |
|------|------|
| 业务库 | MySQL、PostgreSQL、Oracle、SQL Server |
| 向量库（知识库，可选） | PostgreSQL Vector、Milvus、Chroma |

## 🚀 快速开始

### 1. 初始化数据库

按所选数据库执行对应建表脚本（脚本位于 `sql/relation/`）：

| 数据库 | 脚本 |
|--------|------|
| MySQL | `sql/relation/mysql/luck_report.sql` |
| PostgreSQL | `sql/relation/pgsql/luck_report.sql` |
| Oracle | `sql/relation/oracle/luck_report.sql` |
| SQL Server | `sql/relation/sqlserver/luck_report.sql` |

向量库相关脚本见 `sql/vector/`（启用知识库时按需执行）。

### 2. 修改配置

编辑启动模块配置 `luck-report-pub/src/main/resources/application-dev.yml`，至少改数据源：

```yaml
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://localhost:3306/luck_report?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.cj.jdbc.Driver

luck-report:
  servletPrefix: luck-report
  # 本地体验建议先用内存缓存，避免强依赖 Redis
  disableLocalReportCache: false
  token:
    enabled: false
```

若暂不使用知识库 / 向量检索，可将同文件中的 `luck-report.vector` 整段注释掉，并视情况去掉 `luck-report-pub` 对向量插件的依赖，以免启动时连接向量库失败。

### 3. 编译并启动

在仓库根目录执行：

```bash
mvn clean package -DskipTests
java -jar luck-report-pub/target/luck-report-pub.jar
```

或在 IDE 中运行主类：`com.luck.report.pub.LuckReportApplication`（Profile 使用 `dev`）。

默认开发端口见 `application-dev.yml`（一般为 `8049`），`servletPrefix` 设置为 `luck-report`。

### 4. 访问

| 页面 | 地址 |
|------|------|
| 管理页 | http://localhost:8049/luck-report/manage |
| 设计器 | http://localhost:8049/luck-report/designer |
| 预览 | http://localhost:8049/luck-report/preview?reportPath=db:{报表ID} |

## 🗂 目录结构

```text
.
├── luck-report-core/                 # 报表计算、表达式、导出等核心引擎
├── luck-report-web/                  # Web 接口、设计器/预览页面资源、业务服务
├── luck-report-jdbc/                 # JDBC 访问封装
├── luck-report-font/                 # 内置字体资源
├── luck-report-plugins/              # 可选插件
│   ├── luck-report-infra/            # 基础设施（缓存、Servlet 适配等）
│   ├── luck-report-redis/            # Redis 缓存扩展
│   ├── luck-report-postgresql-vector/
│   ├── luck-report-milvus-vector/
│   └── luck-report-chroma-vector/    # 向量库插件（知识库相关，按需启用）
├── luck-report-spring-boot2-starter/ # Spring Boot 2.x 适配
├── luck-report-spring-boot3-starter/ # Spring Boot 3.x 适配
├── luck-report-pub/                  # 可独立运行的启动模块（本地体验入口）
├── sql/                              # 建表与向量库初始化脚本
│   ├── relation/                     # 业务库表（mysql / pgsql / oracle / sqlserver）
│   └── vector/                       # 向量库相关脚本
└── pom.xml
```

## 🧩 内置功能

| 功能 | 描述                                                                            |
|------|-------------------------------------------------------------------------------|
| 报表设计器 | 网页端可视化设计器，拖拽绑定字段，配置单元格与版式，设计后可即时预览                                            |
| AI 智能助手 | 自然语言对话制表、改表与答疑，支持深度思考                                                         |
| 智能体规则 | 统一管理 AI 智能助手的全局规则                                                             |
| 知识库 | 报表知识库与业务知识库；PDF / Word / TXT / Markdown / 问答向量化检索；混合多路召回 + 可选 Reranker，辅助智能助手 |
| 大模型配置 | 统一管理对话、嵌入、重排序模型                                                               |
| 公共数据源 / 数据集 | 统一维护库连接与可复用数据集，可跨报表引用                                                         |
| 数据源 | JDBC、内置数据源、Spring Bean、静态 JSON                                                |
| 查询参数 | URL、查询表单与内置参数（如用户 ID）驱动取数                                                     |
| 表达式引擎 | 内置表达式与函数，支持复杂计算与动态 SQL                                                        |
| 条件属性 | 按条件动态改样式、分页、链接等                                                               |
| 行类型与分页 | 标题 / 重复表头表尾 / 总结行，补充空白行与固定行数分页                                                |
| 图表 | 饼图、柱图、折线、雷达、散点、气泡等 10 种图表（ECharts）                                            |
| 图片 / 条码 | 路径、Base64、表达式图片；一维条码与二维码                                                      |
| 悬浮元素 / 套打 | 纸面悬浮图文；套打背景图对齐预印单据                                                            |
| 预览与工具栏 | 预览 / 分页预览；工具栏按钮可按报表配置显隐                                                       |
| 导出与打印 | 导出 PDF、Word、Excel（分页 / 分 Sheet）；PDF 与浏览器打印                                    |
| 权限 | 管理端 / 设计器角色白名单，预览端按角色授权报表                                                     |
| 缓存 | 本地缓存与 Redis 分布式缓存扩展                                                           |
| 国际化 | 中英文界面切换（`zh_CN` / `en_US`）                                                    |
| 旧版迁移 | 提供 UReport2、Luck-Report V1 报表转化工具                                             |

## 📷 演示图

**报表设计器**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表设计器-1.png" alt="报表设计器" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/报表设计器-1.png" />

**智能助手**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/智能助手-1.png" alt="智能助手" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/智能助手-1.png" />

**图表**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/图表-1.png" alt="图表" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/图表-1.png" />

**预览**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/预览-1.png" alt="预览" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/预览-1.png" />

**报表管理**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表管理-1.png" alt="报表管理" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/报表管理-1.png" />

**知识库**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表知识库-1.png" alt="报表知识库" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/报表知识库-1.png" />

**公共数据源**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/公共数据源-1.png" alt="公共数据源" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/公共数据源-1.png" />

**权限管理**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/权限管理-1.png" alt="权限管理" style="max-width: 100%; height: auto;"  data-local-src="/images-plus/git/权限管理-1.png" />

## ❤️ 赞助支持

如果觉得本项目对你有帮助，欢迎扫码赞助，你的支持是项目持续维护的动力～

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/support-pay.jpg" alt="赞助二维码" width="200"  data-local-src="/images-plus/git/support-pay.jpg" />

## 💬 沟通交流

想要咨询相关问题、获取前端源码，可联系作者邀请进群。

添加微信时请备注 **【Luck Report】**。

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/contact/weixin.png" alt="作者微信" width="200"  data-local-src="/images-plus/contact/weixin.png" />

| 项目 | 报价（元） |
|------|------------|
| 报表开发手册 | 200 / 年 |
| 报表前端源码 | 1800 |
