package tech.msop.core.oss.model;

import io.minio.messages.Item;
import io.minio.messages.Owner;
import lombok.Data;
import tech.msop.core.tool.utils.DateUtil;

import java.util.Date;

/**
 * Minio Item
 *
 * @author ruozhuliufeng
 */
@Data
public class MinioItem {
    private String objectName;
    private Date lastModified;
    private String etag;
    private Long size;
    private String storageClass;
    private Owner owner;
    private boolean isDir;
    private String category;

    public MinioItem(Item item) {
        this.objectName = item.objectName();
        this.lastModified = DateUtil.toDate(item.lastModified().toLocalDateTime());
        this.etag = item.etag();
        this.size = item.size();
        this.storageClass = item.storageClass();
        this.owner = item.owner();
        this.isDir = item.isDir();
        this.category = isDir ? "dir" : "file";
    }
}
