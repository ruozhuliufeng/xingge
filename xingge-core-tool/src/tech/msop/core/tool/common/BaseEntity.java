package tech.msop.core.tool.common;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author ruozhuliufeng
 */
@Data
public class BaseEntity {
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateUser;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
