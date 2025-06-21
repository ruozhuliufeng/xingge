package tech.msop.core.tool.node;

import java.io.Serializable;
import java.util.List;

/**
 * 节点
 *
 * @author ruozhuliufeng
 */
public interface INode<T> extends Serializable {
    /**
     * 主键
     *
     * @return Integer
     */
    Long getId();

    /**
     * 父主键
     *
     * @return Integer
     */
    Long getParentId();

    /**
     * 子孙节点
     *
     * @return List
     */
    List<T> getChildren();

    /**
     * 是否有子孙节点
     *
     * @return 默认为否
     */
    default Boolean getHasChildren() {
        return false;
    }
}
