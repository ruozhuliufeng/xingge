package tech.msop.core.tool.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bean 属性
 *
 * @author ruozhuliufeng
 */
@Getter
@AllArgsConstructor
public class BeanProperty {
    private final String name;
    private final Class<?> type;
}
