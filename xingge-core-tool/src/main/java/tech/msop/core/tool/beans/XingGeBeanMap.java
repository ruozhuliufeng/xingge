package tech.msop.core.tool.beans;

import org.springframework.asm.ClassVisitor;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.cglib.core.AbstractClassGenerator;
import org.springframework.cglib.core.ReflectUtils;

import java.security.ProtectionDomain;

/**
 * 重写 cglib BeanMap，支持链式bean
 *
 * @author ruozhuliufeng
 */
public abstract class XingGeBeanMap extends BeanMap {
    protected XingGeBeanMap() {
    }

    protected XingGeBeanMap(Object bean) {
        super(bean);
    }

    public static XingGeBeanMap create(Object bean) {
        MsGenerator gen = new MsGenerator();
        gen.setBean(bean);
        return gen.create();
    }

    /**
     * newInstance
     *
     * @param o Object
     * @return MsBeanMap
     */
    @Override
    public abstract XingGeBeanMap newInstance(Object o);

    public static class MsGenerator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(MsGenerator.class.getName());

        private Object bean;
        private Class beanClass;
        private int require;

        public MsGenerator() {
            super(SOURCE);
        }

        /**
         * Set the bean that the generated map should reflect. The bean may be swapped
         * out for another bean of the same type using {@link #setBean}.
         * Calling this method overrides any value previously set using {@link #setBeanClass}.
         * You must call either this method or {@link #setBeanClass} before {@link #create}.
         *
         * @param bean the initial bean
         */
        public void setBean(Object bean) {
            this.bean = bean;
            if (bean != null) {
                beanClass = bean.getClass();
            }
        }

        /**
         * Set the class of the bean that the generated map should support.
         * You must call either this method or {@link #setBeanClass} before {@link #create}.
         *
         * @param beanClass the class of the bean
         */
        public void setBeanClass(Class beanClass) {
            this.beanClass = beanClass;
        }

        /**
         * Limit the properties reflected by the generated map.
         *
         * @param require any combination of {@link #REQUIRE_GETTER} and
         *                {@link #REQUIRE_SETTER}; default is zero (any property allowed)
         */
        public void setRequire(int require) {
            this.require = require;
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return beanClass.getClassLoader();
        }

        @Override
        protected ProtectionDomain getProtectionDomain() {
            return ReflectUtils.getProtectionDomain(beanClass);
        }

        /**
         * Create a new instance of the <code>BeanMap</code>. An existing
         * generated class will be reused if possible.
         *
         * @return {MsBeanMap}
         */
        public XingGeBeanMap create() {
            if (beanClass == null) {
                throw new IllegalArgumentException("Class of bean unknown");
            }
            setNamePrefix(beanClass.getName());
            XingGeBeanMapKey key = new XingGeBeanMapKey(beanClass, require);
            return (XingGeBeanMap) super.create(key);
        }

        @Override
        public void generateClass(ClassVisitor v) throws Exception {
            new XingGeBeanMapEmitter(v, getClassName(), beanClass, require);
        }

        @Override
        protected Object firstInstance(Class type) {
            return ((BeanMap) ReflectUtils.newInstance(type)).newInstance(bean);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return ((BeanMap) instance).newInstance(bean);
        }
    }

}
