# 评级 / 赛璐璐搜索与跳页部署（2026-09-18）

## 范围

- 评级：证书编号或 rateName 模糊搜索，保留起止编号条件。
- 赛璐璐：证书编号、roleName 或 cartoonName 模糊搜索。
- 关键词去首尾空格，不区分英文大小写；LIKE 特殊字符按字面匹配。
- 两页开启 Ant Design 快速跳页；保留筛选；每页条数修改回到第一页；后端读取 pageSize。
- 不改照片处理、删除方法、数据库结构。保留已上线的宝贝管理搜索功能。

## 部署记录

- 服务器 staging / 备份：`D:\deploy\rate-cartoon-search-20260918\backup`。
- 原 JAR：`10c36a3aef34b04ca1e5f467e24af649ba3df52f0a5d77ba3754cb4ea21b40c7`。
- 新 JAR：`eadd260ccbe220029ed41dbb23d8249f851fe8dbe789e9ebccb4c3e837cee500`。
- 新前端：`client.search-20260918.js`；SHA256 `a6089091c3ec889db3644367d15b9e5bc6489e04e6cbed698515351eeedb916c`。
- 新 index SHA256：`f248dd4ad2f6057a03e25c2b3127b80773daec681368b17088d7ae1240d7e498`。
- 切换结果 `DEPLOY_OK`，启动 Java PID 30380；通过 CIM 启动，保持原参数，ddl-auto=none。
- 程序包仅替换六个声明的类，逐条目校验其余内容一致；类内无关方法字节码保持不变。
- 前端仅替换两个目标模块，其他模块（包括宝贝管理）逐字节一致。

## 验证及限制

- 源码与最终补丁各运行 RateCartoonSearchTest、PreciousSearchTest，共六项测试，无失败。
- H2 测试中用 BIGINT domain 模拟 UNSIGNED，未将 H2 验证表述为真实 MySQL 集成验证。
- 前端补丁进行语法解析、跳页边界、筛选保留、页大小测试；界面机械检测无报告。
- 部署健康检查通过；verify-online.mjs 检查三个路由、线上 bundle 哈希及未登录接口 401。
- 浏览器控制超时，未完成登录状态下的页面点击验收；需刷新后验证搜索及跳页。

本目录脚本用于审计和重现，不应不经检查重复运行部署。服务器备份保留了本次修改之前（已含宝贝管理新功能）的程序及入口文件。
