<p align="center">
	<img alt="Luck-Report" src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/header-01.png" width="64" data-local-src="/images-plus/git/header-01.png">
</p>
<h1 align="center" style="margin: 20px 0; font-weight: bold;">Luck-Report V2.0.8</h1>
<h4 align="center">Chinese-style complex reporting engine · Built-in AI assistant</h4>
<p align="center">
	<a href="https://gitee.com/LuckyPools/luck-report-server/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-green.svg"></a>
	<a href="https://gitee.com/LuckyPools/luck-report-server/stargazers"><img src="https://gitee.com/LuckyPools/luck-report-server/badge/star.svg"></a>
	<a href="https://gitee.com/LuckyPools/luck-report-server/members"><img src="https://img.shields.io/badge/Fork%20on%20Gitee-Click%20Here-blue"></a>
</p>
<p align="center">
	[<a href="./README.md">中文</a>] | [<a href="./README_EN.md">English</a>]
</p>

## 📖 Introduction

Luck-Report is an open-source reporting engine for **Chinese-style complex reports**. Built on **cell iteration**, it supports designing, previewing, and exporting crosstabs, grouped reports, master-detail layouts, and other advanced formats in the browser.

Luck-Report includes a built-in **AI assistant**. With **LLM configuration** and a **knowledge base**, you can create, edit, and get answers about reports in natural language. The backend is based on **Spring Boot** and can be deployed standalone. The project is a refactor of open-source **UReport2**: it keeps Chinese-style reporting capabilities while adding a modern architecture and AI assistance.

This repository is the backend source of the reporting engine, open-sourced under **Apache-2.0**, **free for commercial use**.

## ✨ Core Capabilities

| Capability | Description |
|------------|-------------|
| Chinese-style complex reports | Cell-iteration model; crosstabs, grouped reports, master-detail, form overlay, and more in the web designer |
| AI assistant | Natural-language create / edit / Q&A, with deep thinking |
| Knowledge base | Report and business knowledge bases; PDF / Word / TXT / Markdown / Q&A vector retrieval; hybrid multi-path recall and optional Rerank |
| Report management | Management UI for reports, role permissions, and preview authorization |
| Project integration | Embed into backend projects via Starter |

## 🌐 Resources

| Resource | URL |
|----------|-----|
| Live demo | [https://www.tinyluck.cn:8060/login](https://www.tinyluck.cn:8060/login) |
| Docs | [https://www.tinyluck.cn:8099/se/docs](https://www.tinyluck.cn:8099/se/docs) |
| Source | [https://gitee.com/LuckyPools/luck-report-server](https://gitee.com/LuckyPools/luck-report-server) |
| Issues | [Gitee Issues](https://gitee.com/LuckyPools/luck-report-server/issues) |

## 💻 Requirements

| Runtime | Version |
|---------|---------|
| JDK | >= 1.8 |
| Maven | >= 3.6 |
| MySQL | >= 5.7 |

> Redis and vector databases are only required when the corresponding plugins / settings are enabled.

### Database compatibility

| Type | Supported |
|------|-----------|
| Business DB | MySQL, PostgreSQL, Oracle, SQL Server |
| Vector DB (knowledge base, optional) | PostgreSQL Vector, Milvus, Chroma |

## 🚀 Quick Start

### 1. Initialize the database

Run the schema script for your database (under `sql/relation/`):

| Database | Script |
|----------|--------|
| MySQL | `sql/relation/mysql/luck_report.sql` |
| PostgreSQL | `sql/relation/pgsql/luck_report.sql` |
| Oracle | `sql/relation/oracle/luck_report.sql` |
| SQL Server | `sql/relation/sqlserver/luck_report.sql` |

Vector-related scripts are under `sql/vector/` (run as needed when enabling the knowledge base).

### 2. Update configuration

Edit `luck-report-pub/src/main/resources/application-dev.yml` and at least set the datasource:

```yaml
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://localhost:3306/luck_report?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.cj.jdbc.Driver

luck-report:
  servletPrefix: luck-report
  # Prefer local cache for a first run to avoid a hard Redis dependency
  disableLocalReportCache: false
  token:
    enabled: false
```

If you are not using the knowledge base / vector search yet, comment out the entire `luck-report.vector` block in the same file, and optionally remove the vector plugin dependency from `luck-report-pub`, so startup does not try to connect to a vector store.

### 3. Build and run

From the repository root:

```bash
mvn clean package -DskipTests
java -jar luck-report-pub/target/luck-report-pub.jar
```

Or run the main class in your IDE: `com.luck.report.pub.LuckReportApplication` (active profile `dev`).

The default development port is defined in `application-dev.yml` (typically `8049`). `servletPrefix` is set to `luck-report`.

### 4. Open the app

| Page | URL |
|------|-----|
| Manage | http://localhost:8049/luck-report/manage |
| Designer | http://localhost:8049/luck-report/designer |
| Preview | http://localhost:8049/luck-report/preview?reportPath=db:{reportId} |

## 🗂 Project Structure

```text
.
├── luck-report-core/                 # Core engine: compute, expressions, export
├── luck-report-web/                  # Web APIs, designer/preview assets, services
├── luck-report-jdbc/                 # JDBC access layer
├── luck-report-font/                 # Bundled fonts
├── luck-report-plugins/              # Optional plugins
│   ├── luck-report-infra/            # Infra (cache, servlet adapters, etc.)
│   ├── luck-report-redis/            # Redis cache extension
│   ├── luck-report-postgresql-vector/
│   ├── luck-report-milvus-vector/
│   └── luck-report-chroma-vector/    # Vector DB plugins (knowledge base; enable as needed)
├── luck-report-spring-boot2-starter/ # Spring Boot 2.x adapter
├── luck-report-spring-boot3-starter/ # Spring Boot 3.x adapter
├── luck-report-pub/                  # Standalone boot module (local entry point)
├── sql/                              # Schema and vector init scripts
│   ├── relation/                     # Business tables (mysql / pgsql / oracle / sqlserver)
│   └── vector/                       # Vector store scripts
└── pom.xml
```

## 🧩 Features

| Feature | Description |
|---------|-------------|
| Report designer | Web visual designer; drag-and-drop field binding, cell and layout config, instant preview |
| AI assistant | Natural-language create / edit / Q&A with deep thinking |
| Agent rules | Centralized global rules for the AI assistant |
| Knowledge base | Report and business knowledge bases; PDF / Word / TXT / Markdown / Q&A vector retrieval; hybrid multi-path recall + optional Reranker for the assistant |
| Model configuration | Unified chat, embedding, and rerank model settings |
| Shared datasources / datasets | Central connections and reusable datasets across reports |
| Datasources | JDBC, built-in datasource, Spring Bean, static JSON |
| Query parameters | URL, search form, and built-in params (e.g. user ID) drive queries |
| Expression engine | Built-in expressions and functions for complex compute and dynamic SQL |
| Conditional properties | Dynamic style, paging, links, and more by condition |
| Row bands & paging | Title / repeating header & footer / summary; blank-row fill and fixed rows per page |
| Charts | 10 chart types via ECharts (pie, bar, line, radar, scatter, bubble, etc.) |
| Images / barcodes | Path, Base64, expression images; 1D barcodes and QR codes |
| Float elements / form overlay | Floating text/images; overlay background aligned to pre-printed forms |
| Preview & toolbar | Preview / paged preview; toolbar buttons configurable per report |
| Export & print | PDF, Word, Excel (paged / multi-sheet); PDF and browser print |
| Permissions | Admin/designer role whitelist; preview access by role |
| Cache | Local cache and Redis distributed cache extension |
| i18n | Chinese / English UI (`zh_CN` / `en_US`) |
| Legacy migration | Tools to convert UReport2 and Luck-Report V1 reports |

## 📷 Screenshots

**Report designer**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表设计器-1.png" alt="Report designer" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/报表设计器-1.png" />

**AI assistant**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/智能助手-1.png" alt="AI assistant" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/智能助手-1.png" />

**Charts**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/图表-1.png" alt="Charts" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/图表-1.png" />

**Preview**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/预览-1.png" alt="Preview" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/预览-1.png" />

**Report management**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表管理-1.png" alt="Report management" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/报表管理-1.png" />

**Knowledge base**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/报表知识库-1.png" alt="Knowledge base" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/报表知识库-1.png" />

**Shared datasources**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/公共数据源-1.png" alt="Shared datasources" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/公共数据源-1.png" />

**Permissions**

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/权限管理-1.png" alt="Permissions" style="max-width: 100%; height: auto;" data-local-src="/images-plus/git/权限管理-1.png" />

## ❤️ Sponsorship

If this project helps you, a tip via the QR code below helps keep maintenance going.

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/git/support-pay.jpg" alt="Sponsorship QR code" width="200" data-local-src="/images-plus/git/support-pay.jpg" />

## 💬 Contact

For questions or frontend source access, contact the author to join the group.

When adding on WeChat, please note **【Luck Report】**.

<img src="https://www.tinyluck.cn:8088/assets/image/luck-report-v2/contact/weixin.png" alt="Author WeChat" width="200" data-local-src="/images-plus/contact/weixin.png" />
