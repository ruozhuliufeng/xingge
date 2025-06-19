package tech.msop.xingge.interceptor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tech.msop.xingge.interceptor.interceptor.HttpRequestInterceptor;

/**
 * 拦截器配置类
 * 注册HTTP请求拦截器
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "xingge.interceptor", name = "enabled", havingValue = "true")
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private HttpRequestInterceptor httpRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpRequestInterceptor)
                .addPathPatterns("/**"); // 拦截所有路径，具体匹配在拦截器内部处理
    }
}