# 部署与回退

## 当前服务器分工

| 项目 | 环境 | 主要目录/入口 |
| --- | --- | --- |
| 收藏交易 | Linux / Docker | `/work/projects/yudao-server/onebook-server.jar`；容器 `yudao-server` |
| 收藏后台 | Nginx | `/work/nginx/html/yudao-ui-admin`；`uni.yishunqianming.com` |
| 证书服务 | Windows | `D:\project\yishun-0.0.1-SNAPSHOT.jar`；`yishunqianming.com` |
| 证书静态网页 | Windows / Nginx | 核对 `D:\nginx-1.14.2\conf` 中实际 root，再更新对应静态目录 |
| 证书公开图片 | Windows 文件目录 | 历史路径 `D:\home\usr_upload`，依生产路由配置确认 |
| 证书私有图片 | Windows 文件目录 | `D:\yishun-private`；访问受服务端规则控制 |

主机地址、SSH 用户及私钥由运维另行提供。不要把私钥放入仓库。

## Linux

1. 记录当前容器的镜像、网络、环境变量、挂载、端口、重启策略，敏感导出保存于服务器受限目录。
2. 备份 JAR、前端、配置，记录 SHA256；确认数据库备份另行完成。
3. 使用测试过的 JAR 构建新镜像。**只复制宿主机 JAR 不会自动更新现有容器**，生产 app.jar 在镜像内。
4. 按原配置创建新容器；原容器保留用于回退。核对 Nginx upstream 和 Docker 网络。
5. 启动成功后切换静态入口，并保留旧 hash 资源以免旧页面加载失败；检查 Nginx 配置并 reload。
6. 验证后台登录、分页、关键接口、图片加载和小程序兼容性。

2026-09-23 排序上线的回退材料在 `/work/backups/image-order-20260923`，旧静态目录 `/work/nginx/html/yudao-ui-admin-before-image-order-20260923`，旧容器 `yudao-server-before-image-order-20260923`。回退前确认当前版本，不能把后续更新覆盖掉。

## Windows

1. 只读确认 Java 进程的可执行文件、JAR、工作目录、启动参数及照片目录参数。
2. 备份当前 JAR、页面入口、相关配置到独立部署目录，并校验哈希。
3. 在隔离环境验证新包兼容 Java 8、数据库结构和照片路由。历史补丁须匹配基线。
4. 安排维护窗口后仅停止对应应用进程，更新应用并沿用原参数启动；不要停止或覆盖数据库目录。
5. 从新的独立连接验证进程存活、健康响应、登录、查询和图片。
6. 失败则恢复应用和页面备份；不回滚整个数据盘或数据库。

现有脚本包含特定日期、机器绝对路径和一次性操作。阅读后按当前环境修改，禁止无检查重跑。

## 本次整理

仅创建源码归档，不执行数据库迁移、停服务、图片整理或新的业务发布。
