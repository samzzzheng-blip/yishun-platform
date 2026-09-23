# 开发与构建

先在隔离的开发环境准备配置，禁止直接连接生产数据库运行测试。

## 收藏服务端

要求 JDK 17、Maven 3.9；生产当前使用 Java 17。源码 Dockerfile 的 Java 镜像版本与现网可能不同，部署前统一版本。

```sh
cd collection/backend
mvn -pl onebook-server -am -DskipTests package
```

预期产物 `onebook-server/target/onebook-server.jar`。先补全本地环境变量和独立数据库配置，再运行。特定排序测试：

```sh
mvn -pl onebook-module-app -am test -Dtest=YikoujiaImageOrderTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false
```

SQL 位于 `sql/mysql` 和 server resources 的 migration 目录。不要仅按文件名全量执行；先确定现有数据库版本及已完成迁移。

## 收藏管理后台

查看 `package.json` 的 engines、packageManager 字段及锁文件，使用相容 Node/pnpm。

```sh
cd collection/admin
pnpm install --frozen-lockfile
pnpm build:prod
```

生产构建使用 `.env.prod`。整理后的敏感值需在本地配置；配置说明见 CONFIGURATION。产物默认 `dist`。`build/` 是 Vite 配置源码，不应当作生成物删除。

## 收藏小程序

1. 安装依赖，使用 HBuilderX 导入 `collection/miniapp`。
2. 核对 manifest.json 中微信 AppID、接口域名、合法域名及支付设置。
3. 发行到微信小程序，用微信开发者工具导入生成目录。
4. 真机测试登录、仓库、拍卖、支付、取回等流程后上传开发版本。
5. 在微信后台确认版本并另行提交审核／发布。不要把“上传成功”当作“正式上线”。

包体限制、忽略文件应以本次构建和微信工具校验结果为准，不能沿用旧产物。

## 签名证书服务端

要求 JDK 8，Maven。使用系统 Maven；归档未包含 Maven wrapper 二进制 JAR。

```sh
cd certificate/backend
mvn -DskipTests package
```

预期产物 `target/yishun-0.0.1-SNAPSHOT.jar`。数据库端口和路径从环境配置读取，生产已观察到 MySQL 使用 3308。JPA 生产设置应保留 `ddl-auto=none`，不允许自动改表。

## 签名证书网页

旧项目使用 Webpack 3、React 16。Node 24 实测触发 HappyPack 的 isRegExp 错误；应隔离使用 Node 18.20.8 进行旧构建验证，不修改生产运行环境。Node 18 为旧工具兼容环境，后续应升级前端构建链。

```sh
cd certificate/admin
npm ci
npm run localbuild
```

项目同时提供 localbuild（启用 openssl legacy provider）；仅在所用 Node 支持且确有需要时使用。以 scripts/webpack.prod.config.js 中 output.path 确认产物位置。

## 原生证书小程序

微信开发者工具直接导入 `certificate/miniapp`，重新核对 AppID 和请求域名。其是否仍为当前线上小程序尚未确认。

完整构建和业务验收状态见 HANDOVER；本归档不保证两个系统从空机器一键上线。
