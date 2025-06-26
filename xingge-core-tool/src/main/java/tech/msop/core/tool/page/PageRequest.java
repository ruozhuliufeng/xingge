package tech.msop.core.tool.page;

import lombok.Data;

import java.util.List;

/**
 * 分页请求参数
 *
 * @author ruozhuliufeng
 */
@Data
public class PageRequest {
    /**
     * 起始页，从0开始
     */
    private Integer start;
    /**
     * 每页数量
     */
    private Integer size;
    /**
     * 排序字段
     */
    private String orderKey;
    /**
     * 是否逆序: true：逆序 false：顺序
     */
    private boolean desc;
    /**
     * 排序规则
     */
    private List<OrderRule> orderRules;
}
