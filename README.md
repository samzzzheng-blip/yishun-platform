# 一瞬平台源码归档

整理日期：2026-09-23。本仓库集中保存两台服务器对应的应用源码、两套小程序，以及现有部署补丁和交接文档。

## 项目入口

| 目录 | 系统 | 技术与用途 |
| --- | --- | --- |
| `collection/backend` | 收藏交易服务端 | Java 17、Spring Boot、多模块 Maven；拍卖、一口价、仓库、钱包、订单 |
| `collection/admin` | 收藏交易管理后台 | Vue 3、TypeScript、Vite、Element Plus |
| `collection/miniapp` | 一瞬收藏小程序 | uni-app、Vue；使用 HBuilderX 和微信开发者工具 |
| `certificate/backend` | 签名证书／评级服务端 | Java 8、Spring Boot 2.1.4、JPA、Shiro |
| `certificate/admin` | 签名证书／评级网页 | React 16、Webpack 3；包含管理页面与查询页面 |
| `certificate/miniapp` | 原生证书查询小程序 | 微信原生小程序；保留源码，当前发布状态未核实 |
| `operations` | 历史部署与修复 | PowerShell、Python、Node 和 Java 补丁源码 |

## 阅读顺序

1. [源码来源与上线差异](docs/SOURCE-STATUS.md)
2. [开发与构建](docs/DEVELOPMENT.md)
3. [服务器部署与回退](docs/DEPLOYMENT.md)
4. [配置与密钥](docs/CONFIGURATION.md)
5. [数据备份与恢复](docs/BACKUP-RESTORE.md)
6. [验收及交接](docs/HANDOVER.md)

**这是源码交接仓库，不是完整生产数据备份。** 数据库数据、照片、视频、SSH 私钥、证书私钥、真实密码和构建依赖未上传。恢复业务还需要网盘备份和受保护的生产配置。

归档保留了本地尚未上线的修改，不能把整个仓库直接覆盖到生产环境。证书系统存在基于线上 JAR 的增量补丁，原始源码完整构建结果与现网并不保证相同。具体边界见源码状态文档。

本仓库从当前文件新建历史，未迁移旧 Git 历史，避免旧提交中的密钥或数据进入新仓库。原项目及服务器未因本次整理而修改。

## 授权

各模块原有 LICENSE 和第三方授权声明随源码保留。用户于 2026-09-23 确认使用公开仓库。各模块原有授权要求仍然适用，公开可见不代表另行授予第三方素材的使用许可。
