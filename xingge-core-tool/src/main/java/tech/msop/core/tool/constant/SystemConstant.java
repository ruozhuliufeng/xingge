package tech.msop.core.tool.constant;

import lombok.Data;

/**
 * 系统配置类
 */
@Data
public class SystemConstant {
    /**
     * 开发模式
     */
    private boolean devMode = false;
    /**
     * 远程上传模式
     */
    private boolean remoteMode = false;
    /**
     * 外网访问地址
     */
    private String domain = "http://localhost:8888";
    /**
     * 上传下载路径(物理路径)
     */
    private String remotePath = System.getProperty("user.dir") + "/target/ms";
    /**
     * 上传路径(相对路径)
     */
    private String uploadPath = "/upload";
    /**
     * 下载路径
     */
    private String downloadPath = "/download";
    /**
     * 图片压缩
     */
    private boolean compress = false;
    /**
     * 图片压缩比例
     */
    private Double compressScale = 2.00;
    /**
     * 图片缩放选择：true 放大; false 缩小
     */
    private boolean compressFlag = false;
    /**
     * 项目物理路径
     */
    private String realPath = System.getProperty("user.dir");
    /**
     * 项目相对路径
     */
    private String contextPath = "/";

    private SystemConstant() {
    }

    private static final SystemConstant ME = new SystemConstant();

    public static SystemConstant me() {
        return ME;
    }

    public String getUploadRealPath() {
        return (remoteMode ? remotePath : realPath) + uploadPath;
    }

    public String getUploadCtxPath() {
        return contextPath + uploadPath;
    }

}
