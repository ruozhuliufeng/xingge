package tech.msop.core.tool.config;

import tech.msop.core.tool.support.BinderSupplier;
import tech.msop.core.tool.utils.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.function.Supplier;

/**
 * 工具配置类
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ToolConfiguration {

    /**
     * Spring 上下文缓存
     * @return SpringUtil
     */
    @Bean
    public SpringUtil springUtil(){
        return new SpringUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public Supplier<Object> binderSupplier(){
        return new BinderSupplier();
    }

}
