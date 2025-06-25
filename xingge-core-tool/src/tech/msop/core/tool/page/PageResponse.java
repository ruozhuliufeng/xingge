package tech.msop.core.tool.page;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.msop.core.tool.common.BaseEntity;

/**
 * 分页响应数据
 *
 * @author ruozhuliufeng
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageResponse extends BaseEntity {
    /**
     * 分页总数
     */
    private Long count;
}
