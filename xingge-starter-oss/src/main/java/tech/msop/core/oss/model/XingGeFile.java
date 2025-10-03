package tech.msop.core.oss.model;

import lombok.Data;

/**
 * 文件相关信息
 *
 * @author ruozhuliufeng
 */
@Data
public class XingGeFile {
    /**
     * 文件链接
     */
    private String link;
    /**
     * 域名地址
     */
    private String domain;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 附件表ID
     */
    private Long attachId;
}
