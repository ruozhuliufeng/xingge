package tech.msop.core.tool.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点基类
 *
 * @author ruozhuliufeng
 */
@Data
public class BaseNode<T> implements INode<T> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    protected Long id;
    /**
     * 父节点ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    protected Long parentId;

    /**
     * 子孙节点
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<T> children = new ArrayList<>();

    /**
     * 是否有子孙节点
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean hasChildren;

    /**
     * 判断是否有子孙节点
     *
     * @return true/false
     */
    @Override
    public Boolean getHasChildren() {
        if (children.size() > 0) {
            return true;
        } else {
            return this.hasChildren;
        }
    }
}
