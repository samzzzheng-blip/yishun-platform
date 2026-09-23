# 宝贝管理姓名搜索与页码跳转部署记录

日期：2026-09-18。目标：47.111.232.58，yishunqianming.com/preciousManage。

## 结果

已部署。新 Java 进程 PID 24548（部署完成时）；后续应动态查询 PID，不可照此结束进程。

- 前端新增证书编号/签名人姓名提示和指定页跳转，保留筛选条件，修改页大小重置第一页。
- 后端新增姓名/编号 OR 查询，保留状态过滤，转义 LIKE 通配符，稳定排序，支持请求页大小。
- 只基于线上基线修改三个类的查询相关方法和一个前端页面模块。其余方法字节码和 JAR 内其他条目逐项比对保留。
- 没有部署整个本地源码树，没有修改数据库结构、照片目录或图片删除逻辑。

## 备份和运行方式

服务器备份目录：`D:\deploy\precious-search-20260918\backup`，包含原 application.jar、index.html、client.b066.js。

原 JAR SHA256：`563aa42be2ac864641ea0bc90440b8692d9697db64ee628eab8f36808e7ddbda`

新 JAR SHA256：`10c36a3aef34b04ca1e5f467e24af649ba3df52f0a5d77ba3754cb4ea21b40c7`

原 Java 路径和启动参数保留：`D:\deploy\java8-ready-20260914-153727\jre8\bin\java.exe`，prod，ddl-auto=none，私有照片目录参数不变。通过本机 WMI 创建独立于 SSH 会话的进程，工作目录 D:\project。未新增开机启动机制。

日志：`D:\deploy\precious-search-20260918\application-new.log`。

## 验证

- 对最终补丁类运行 PreciousSearchTest：3 项通过，0 错误。
- 前端限定单模块变更，语法解析、分页边界、筛选保留、页大小重置测试通过。
- 安装包、安装脚本 SHA256 校验通过。
- 备份 JAR/index 校验通过；新 JAR 除三个声明类外全部条目内容一致。
- 本机和公网鉴权端点正常返回未登录 code=401。
- 公网首页引用 client.precious-20260918.js，线上 JS 与本地补丁 SHA256 一致。
- 独立 SSH 会话确认新进程仍运行、新 JAR 和首页哈希一致。
- 登录态页面自动点击验收因浏览器连接超时未完成，不能声称 UI 实测通过。

## 过程与回退

首次切换因 PowerShell/.NET File.Replace 第三个参数传 null 被转换成空路径失败，自动恢复原 JAR 和首页并通过健康检查。修正为显式备份路径后重新部署成功。

回退必须先动态核验当前进程、Java 路径和目标文件，再恢复上述备份；不要盲目重跑部署脚本。原前端 client.b066.js 保留，可恢复原 index.html。数据库和照片不需要也不得通过本次备份回滚。

`baseline/application.jar.partial` 是中止下载留下的不完整副本，不能用于恢复；有效完整备份在服务器上述 backup 目录。
