# XingGe Starter File - 文件服务工具模块

XingGe文件服务工具模块，提供文件、文件夹、压缩文件、Excel、PDF操作及文件格式转换支持。

## 功能特性

### 1. 基础文件操作
- 文件创建、读取、写入、删除
- 文件复制、移动
- 文件大小获取
- 文件扩展名处理

### 2. 文件夹操作
- 目录创建、删除、清空
- 文件列表（递归/非递归）
- 目录复制、移动
- 目录大小计算

### 3. 压缩文件操作
- 支持格式：ZIP、RAR、TAR、TAR.GZ、TAR.BZ2、TAR.XZ
- 支持密码保护的ZIP文件解压
- 支持指定字符集解压（可通过字符串名称指定，如"GBK"、"GB2312"、"ISO-8859-1"等，默认UTF-8）
- 自动创建目标目录

### 4. Excel文件操作
- Excel文件读取（支持xls、xlsx）
- Excel文件创建（支持样式）
- 模板填充（占位符替换）
- 添加图片/盖章到Excel
- 单元格合并

### 5. PDF文件操作
- PDF文本内容读取
- PDF页面切分
- PDF文件合并
- PDF页面旋转
- 添加图片/盖章到PDF
- 添加文本水印
- 删除/提取指定页面

### 6. 文件格式转换
支持多种格式互转，保持精度不丢失：
- Excel → PDF
- Excel → HTML
- Markdown → PDF
- Markdown → HTML
- Markdown → Word
- Word → PDF
- Word → HTML
- HTML → PDF
- HTML → Word

## 快速开始

### 1. 添加依赖

在项目的`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-file</artifactId>
    <version>0.0.4</version>
</dependency>
```

### 2. 配置

在`application.yml`中添加配置（可选）：

```yaml
xg:
  enabled: true
  default-charset: UTF-8
  temp-directory: ${java.io.tmpdir}
  upload-directory: ./uploads
  max-file-size: 104857600  # 100MB
  compression:
    default-charset: UTF-8
```

### 3. 使用示例

#### 注入FileService

```java
@Autowired
private FileService fileService;
```

#### 基础文件操作

```java
// 创建文件
File file = fileService.createFile("/path/to/file.txt");

// 读取文件内容
String content = fileService.readFileToString("/path/to/file.txt");

// 写入文件内容
fileService.writeStringToFile("/path/to/file.txt", "Hello World");

// 删除文件
fileService.deleteFile("/path/to/file.txt");
```

#### 文件夹操作

```java
// 创建目录
File dir = fileService.createDirectory("/path/to/directory");

// 列出文件（递归）
List<File> files = fileService.listFiles("/path/to/directory", true);

// 获取目录大小
long size = fileService.getDirectorySize("/path/to/directory");
```

#### 压缩文件操作

```java
// 解压ZIP文件
fileService.decompress(
    new File("/path/to/archive.zip"),
    new File("/path/to/output")
);

// 解压带密码的ZIP文件
fileService.decompress(
    new File("/path/to/archive.zip"),
    new File("/path/to/output"),
    "password"
);

// 使用指定字符集（如GBK）解压
fileService.decompress(
    new File("/path/to/archive.zip"),
    new File("/path/to/output"),
    "password",
    "GBK"
);

// 使用字符串路径指定字符集解压
fileService.decompress(
    "/path/to/archive.zip",
    "/path/to/output",
    "GBK"
);

// 检查压缩文件是否包含某个文件
boolean contains = fileService.archiveContains(
    new File("/path/to/archive.zip"),
    "file.txt"
);
```

#### Excel操作

```java
// 读取Excel
List<List<String>> data = fileService.readExcel(new File("/path/to/excel.xlsx"));

// 填充Excel模板
Map<String, String> templateData = new HashMap<>();
templateData.put("name", "张三");
templateData.put("date", "2024-01-01");
fileService.fillExcelTemplate(
    new File("/path/to/template.xlsx"),
    new File("/path/to/output.xlsx"),
    templateData
);

// 添加图片到Excel（盖章）
fileService.addImageToExcel(
    new File("/path/to/excel.xlsx"),
    new File("/path/to/stamp.png"),
    0,  // sheet索引
    5, 5,  // 起始行列
    8, 8   // 结束行列
);
```

#### PDF操作

```java
// 读取PDF文本
String text = fileService.readPdfText(new File("/path/to/document.pdf"));

// 切分PDF
fileService.splitPdf(
    new File("/path/to/document.pdf"),
    new File("/path/to/output/"),
    1,  // 起始页
    5   // 结束页
);

// 合并PDF
List<File> pdfFiles = Arrays.asList(
    new File("/path/to/pdf1.pdf"),
    new File("/path/to/pdf2.pdf")
);
fileService.mergePdfs(pdfFiles, new File("/path/to/merged.pdf"));

// 旋转PDF
fileService.rotatePdf(
    new File("/path/to/document.pdf"),
    new File("/path/to/rotated.pdf"),
    90  // 旋转角度
);

// 添加图片到PDF（盖章）
fileService.addImageToPdf(
    new File("/path/to/document.pdf"),
    new File("/path/to/output.pdf"),
    new File("/path/to/stamp.png"),
    400, 500,  // X, Y坐标
    100, 100,  // 宽度、高度
    -1  // 页面索引（-1表示所有页面）
);

// 添加水印
fileService.addWatermark(
    new File("/path/to/document.pdf"),
    new File("/path/to/watermarked.pdf"),
    "机密文件",  // 水印文字
    0.3f,       // 透明度
    50f         // 字体大小
);
```

#### 文件格式转换

```java
// Excel转PDF
fileService.excelToPdf(
    new File("/path/to/excel.xlsx"),
    new File("/path/to/output.pdf")
);

// Markdown转PDF
fileService.markdownToPdf(
    new File("/path/to/document.md"),
    new File("/path/to/output.pdf")
);

// Word转PDF
fileService.wordToPdf(
    new File("/path/to/document.docx"),
    new File("/path/to/output.pdf")
);

// 通用转换方法
fileService.convert(
    new File("/path/to/source.xlsx"),
    new File("/path/to/output.pdf"),
    "pdf"
);
```

## 工具类直接使用

除了通过`FileService`使用外，也可以直接使用各个工具类：

```java
// 使用FileUtil
FileUtil.createFile("/path/to/file.txt");
String content = FileUtil.readFileToString("/path/to/file.txt");

// 使用DirectoryUtil
DirectoryUtil.createDirectory("/path/to/directory");
List<File> files = DirectoryUtil.listFiles("/path/to/directory", true);

// 使用CompressionUtil（支持指定字符集字符串）
CompressionUtil.decompress("/path/to/archive.zip", "/path/to/output");

// 指定字符集解压
CompressionUtil.decompress(
    "/path/to/archive.zip",
    "/path/to/output",
    "GBK"
);

// 指定字符集与密码解压
CompressionUtil.decompress(
    "/path/to/archive.zip",
    "/path/to/output",
    "password",
    "GBK"
);

// 使用ExcelUtil
List<List<String>> data = ExcelUtil.readExcel(new File("/path/to/excel.xlsx"));

// 使用PdfUtil
String text = PdfUtil.readPdfText(new File("/path/to/document.pdf"));

// 使用FileConvertUtil
FileConvertUtil.excelToPdf(
    new File("/path/to/excel.xlsx"),
    new File("/path/to/output.pdf")
);
```

## 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `xg.enabled` | boolean | true | 是否启用文件服务 |
| `xg.default-charset` | String | UTF-8 | 默认字符编码 |
| `xg.temp-directory` | String | ${java.io.tmpdir} | 临时文件目录 |
| `xg.upload-directory` | String | ./uploads | 文件上传目录 |
| `xg.max-file-size` | long | 104857600 | 最大文件大小（字节） |
| `xg.compression.auto-create-target-dir` | boolean | true | 解压时是否自动创建目标目录 |
| `xg.compression.default-charset` | String | UTF-8 | 默认解压字符集（支持UTF-8、GBK、GB2312、ISO-8859-1等） |
| `xg.excel.enable-streaming` | boolean | false | 是否启用流式写入 |
| `xg.pdf.author` | String | XingGe | PDF作者 |
| `xg.pdf.compress` | boolean | true | 是否压缩PDF |

## 依赖说明

该模块依赖以下主要库：
- Apache Commons IO - 基础文件操作
- Apache Commons Compress - 压缩文件处理
- Zip4j - ZIP文件处理（支持密码）
- Junrar - RAR文件处理
- Apache POI - Excel文件处理
- Apache PDFBox - PDF文件处理
- Flexmark - Markdown解析
- Docx4j - Word文件处理
- Openhtmltopdf - HTML转PDF

## 注意事项

1. 处理大文件时注意内存使用
2. PDF/Word格式转换可能需要较长时间
3. RAR解压仅支持RAR 5.0以下版本
4. 中文字体支持需要相关字体文件
5. 部分格式转换可能有精度损失
6. 解压文件时支持通过字符串名称指定字符集，如"GBK"、"GB2312"、"ISO-8859-1"等，自动处理无效字符集名称（回退到UTF-8）

## 许可证

Apache License 2.0
