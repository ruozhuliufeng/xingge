package tech.msop.core.tool.beans;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * bean map key 提高性能
 *
 * @author ruozhuliufeng
 */
@EqualsAndHashCode
@AllArgsConstructor
public class XingGeBeanMapKey {
    private final Class type;
    private final int require;
}
