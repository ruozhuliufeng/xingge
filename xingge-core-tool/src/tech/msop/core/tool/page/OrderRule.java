package tech.msop.core.tool.page;

import lombok.Data;

/**
 * 排序规则
 *
 * @author ruozhuliufeng
 */
@Data
public class OrderRule {
    /**
     * 排序字段
     */
    private String orderKey;
    /**
     * 是否倒叙
     */
    private boolean desc;
}
