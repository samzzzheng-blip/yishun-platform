# 配置与密钥

真实密钥不在 GitHub。配置中的 `${...}`、`REPLACE_WITH_LOCAL_SECRET` 是待配置项，不是有效凭据。

## 必备配置

- MySQL 地址、端口、数据库名、账号密码；收藏和证书系统分别配置。
- Redis 地址、账号／密码和数据库编号。
- 微信 AppID、AppSecret、支付商户与证书；AppID 本身可保留，私钥与 AppSecret 不入库。
- OSS endpoint、bucket、AccessKey；照片对象本体在数据备份中。
- 闲鱼／Goofish 接口账号、密钥及回调签名配置。
- 短信、邮件、地图、AI 服务等实际启用服务的凭据。
- 公私有图片路径、域名、TLS 证书位置。

## 归档中的明确改动

- 收藏 `YikoujiaServiceImpl` 的硬编码接口凭据改为环境变量 `GOOFISH_API_KEY` 和 `GOOFISH_API_SECRET`。接口编号必须是可解析的整数。
- 证书 `JwtUtil.SECRET` 改为 `YISHUN_JWT_SECRET`。必须设置并保管，改变该值会影响旧令牌验证；不要用任意新值直接替换现网配置。
- Java properties 中占位符是否自动解析取决于加载方式。对直接 Properties.load 读取的文件，应在本地受保护配置中填写值，不能假定 `${VAR}` 自动展开。
- 前端历史 RSA 私钥移除，使用 `VITE_LOCAL_RSA_PRIVATE_KEY` 占位。前端变量会被打包到浏览器，不可放生产服务端私钥；使用该旧解密功能前需重新设计密钥方案或仅使用无敏感性的演示数据。
- `.env` 中演示账号、服务密钥已去除或替换。前端配置只应包含可公开的运行参数。
- SQL 中业务/账号写入数据已排除；生产数据恢复走受保护备份。

这些仅改变归档副本，没有改动当前生产配置或原工作目录。

## 本地管理

推荐独立 secrets 目录、密码管理器和环境变量；`.env.local`、私钥、证书和容器 inspect 原始输出不得提交。不要把真实秘密写进 README、截图、测试断言或 issue。

仓库初始化前已运行 Gitleaks 全目录扫描并处理发现项。自动扫描不能证明任何形式的秘密都不存在，后续每次提交仍需检查差异。
