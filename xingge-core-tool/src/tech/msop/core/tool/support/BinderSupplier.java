package tech.msop.core.tool.support;

import java.util.function.Supplier;

/**
 * 解决no binder avaliable 问题
 *
 * @author ruozhuliufeng
 */
public class BinderSupplier implements Supplier<Object> {

    @Override
    public Object get() {
        return null;
    }
}
