package tech.msop.core.oss.model;

import lombok.Data;

import java.util.Date;

/**
 * OSS上传文件信息
 * @author ruozhuliufeng
 */
@Data
public class OssFile {
    /**
     * 文件链接
     */
    private String link;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件hash
     */
    private String hash;
    /**
     * 文件大小
     */
    private long length;
    /**
     * 文件上传时间
     */
    private Date putTime;
    /**
     * 文件contentType
     */
    private String contentType;
}
