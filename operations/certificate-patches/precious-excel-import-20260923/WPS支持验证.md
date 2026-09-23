# WPS 单元格图片支持（2026-09-23 已部署）

部署备份：`D:\deploy\precious-wps-20260923\backup`。仅部署 3 个后端类文件，线上类文件哈希、公网资源及服务健康/鉴权检查通过。未写入测试商品。

新增 `PreciousCellImages`，通过 DISPIMG 图片 ID → cellimages.xml → 关系文件 → 内嵌媒体，准确匹配第一张工作表的照片及证据图片列。兼容 cellimages.xml 大小写，不处理外部链接。原浮动图片、网络地址、本地附件匹配逻辑保留。

安全检查：禁止 XML DTD/外部实体；仅允许工作簿 xl/media 内资源；单图 10MB、解压图片总量 50MB；拒绝重复 ID、缺失资源、同单元格多种图片来源。仅支持 .xlsx 格式的 WPS 单元格图片；.xls 提示另存为 .xlsx。

使用用户原文件 `/Users/mac/Downloads/西村千奈美-卡牌-东京-2026.08.xlsx` 完成本地隔离验证：

- 主图读取 xl/media/image2.png，证据图片读取 xl/media/image3.png。
- 测试写出的两张图片与 Excel 原始图片字节完全一致。
- 预检只返回实际视频链接 C:/Users/Mayn/Desktop/9月20日(7).mp4，不将 DISPIMG 误当成附件路径。
- 使用模拟视频附件、模拟数据库完成测试；没有导入真实服务器，也没有修改原 Excel。
- 10 项测试全部通过，无跳过；包括图片资源缺失、恶意 XML 拒绝，以及原导入功能回归测试。

视频链接与所选文件名不一致、已有证书编号跳过仍需分别处理，本改动不改变这些规则。

部署注意：这是上一版上线后的新增后端变更。必须重新核对当前线上 JAR 基线、备份后，仅加入 PreciousImportService.class 和 PreciousCellImages*.class（包括编译器生成的内部类）。不要直接重跑旧 package.py 或旧部署脚本。
