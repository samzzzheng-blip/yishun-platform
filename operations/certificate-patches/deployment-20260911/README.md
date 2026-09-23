# 2026-09-11 正式系统合并部署

目标：https://yishunqianming.com （Windows 47.111.232.58）。

- 后端只补丁更新 34 个正式 Java 8 类；随后更新评级控制器以兼容原系统父菜单权限继承。
- 前端为原系统构建产物，保留旧静态资源以兼容缓存和回滚。
- 不包含本地 H2 数据库、WorkflowSandbox、测试账号、模板、业务记录、上传照片。
- 新建 6 个空的 grading_* InnoDB 表；rate 从 MyISAM 转换为 InnoDB，保留原记录与字段，支持整批发布事务回滚。
- 不修改账号、角色或权限表。
- 私有照片目录：D:\yishun-private\grading，不是 nginx 公共上传目录。
- 生产禁用 Hibernate 自动 DDL；表结构由已审核迁移创建。

## 备份

服务器 D:\deploy\grading-workflow-20260911\backup 保存：

- application.jar：原正式后端。
- dist：原前端。
- database.sql：完整数据库备份，MyISAM 使用全表锁确保一致性。
- database-at-cutover.sql：停止服务后的第二份切换点完整备份。
- before-compat.jar：权限兼容补丁前的程序。
- start-yishun.bat：原启动脚本（持久化启动参数时备份）。

部署前后核对：rate 18928、precious 60643、cartoon 7694、user 25 条，数量一致；新增工作流表总记录数 0。

## 验证

本地 57 项后端测试通过。线上只读验证原评级列表、新流程列表、批次列表、模板列表、未登录拦截、同账号双会话、前端 SHA256。未创建测试业务记录。

## 注意

不要再次执行 deploy.ps1：该脚本仅适用于首次创建空的流程表，会拒绝已有表。未来升级需要单独迁移审查。

使用服务器 D:\project\start-yishun.bat 重启；新增启动参数保证私有照片路径稳定、禁用自动 DDL。

出现问题优先回滚代码，保留数据库现状和备份。不要自动恢复整库、删除新表或删除照片，以免覆盖部署后的正式业务数据。
