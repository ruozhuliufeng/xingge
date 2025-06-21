package tech.msop.core.tool.beans;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * copy key
 *
 * @author ruozhuliufeng
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class MsBeanCopierKey {
    private final Class<?> source;
    private final Class<?> target;
    private final boolean useConverter;
    private final boolean nonNull;
}
