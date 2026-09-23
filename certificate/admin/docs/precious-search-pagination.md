# 宝贝管理：姓名搜索与页码跳转（2026-09-18）

仅本地修改，未部署；没有访问或修改生产数据库、照片、服务器文件。

## 行为

- 搜索框同时匹配证书编号或签名人姓名，支持部分匹配、忽略英文大小写、去除首尾空格。
- 保留上架状态过滤；空搜索显示全部。百分号、下划线和感叹号作为普通字符查询。
- 提交搜索回到第一页；分页保留已提交的关键词及状态。
- 宝贝管理页启用 Ant Design 原生快速跳页，输入页码后回车。其他列表不改变。
- 修改每页条数回到第一页，后端使用请求中的 pageSize。
- 更新时间相同时以记录ID倒序，避免分页顺序不稳定。

## 代码

前端：app/pages/managerCenter/preciousManage/index.js。

后端：PreciousDao.searchByNumberOrSigner、PreciousServiceImpl.findPreciousPageByKeywords、PreciousController.queryPreciousList。

## 验证

- scripts/precious-search.test.cjs：搜索、状态保留、分页重置、非法页码、每页条数、快速跳页属性。
- PreciousSearchTest：本地H2集成测试3项，验证实际查询及分页。
- LegacyPhotoRetentionTest：照片保留保护12项回归通过。

后续若要求部署，必须从当前生产版本制作独立补丁并核对备份；不能直接把整个本地工作目录覆盖到服务器。PreciousController 还包含此前本地照片保护改动，需要独立审阅线上差异。
