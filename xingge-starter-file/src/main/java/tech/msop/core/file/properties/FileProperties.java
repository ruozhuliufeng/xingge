package tech.msop.core.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件服务配置属性
 *
 * @author ruozhuliufeng
 */
@Data
@ConfigurationProperties(prefix = "xingge.file")
public class FileProperties {

    /**
     * 是否启用文件服务
     */
    private boolean enabled = true;

    /**
     * 默认字符编码
     */
    private String defaultCharset = "UTF-8";

    /**
     * 临时文件目录
     */
    private String tempDirectory = System.getProperty("java.io.tmpdir");

    /**
     * 文件上传目录
     */
    private String uploadDirectory = "./uploads";

    /**
     * 最大文件大小（字节）
     */
    private long maxFileSize = 100 * 1024 * 1024;

    /**
     * 允许的文件扩展名（逗号分隔，空表示不限制）
     */
    private String allowedExtensions = "";

    /**
     * 压缩配置
     */
    private CompressionConfig compression = new CompressionConfig();

    /**
     * Excel配置
     */
    private ExcelConfig excel = new ExcelConfig();

    /**
     * PDF配置
     */
    private PdfConfig pdf = new PdfConfig();

    @Data
    public static class CompressionConfig {
        /**
         * 解压时是否自动创建目标目录
         */
        private boolean autoCreateTargetDir = true;

        /**
         * 默认解压字符集
         */
        private String defaultCharset = "UTF-8";
    }

    @Data
    public static class ExcelConfig {
        /**
         * 是否启用流式写入（大文件）
         */
        private boolean enableStreaming = false;

        /**
         * 流式写入时的行缓存大小
         */
        private int rowAccessWindowSize = 100;
    }

    @Data
    public static class PdfConfig {
        /**
         * PDF作者
         */
        private String author = "XingGe";

        /**
         * PDF创建者
         */
        private String creator = "XingGe File Service";

        /**
         * 是否压缩PDF
         */
        private boolean compress = true;
    }
}
