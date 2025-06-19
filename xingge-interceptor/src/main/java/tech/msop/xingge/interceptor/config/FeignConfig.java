package tech.msop.xingge.interceptor.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.msop.xingge.interceptor.interceptor.FeignInterceptor;

/**
 * Feign客户端配置
 * 配置Feign拦截器
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignConfig {

    @Autowired
    private FeignInterceptor feignInterceptor;

    /**
     * 注册Feign请求拦截器
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return feignInterceptor;
    }
}