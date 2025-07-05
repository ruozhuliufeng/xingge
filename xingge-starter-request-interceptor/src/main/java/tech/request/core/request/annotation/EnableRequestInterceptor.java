/*
 * Copyright (c) 2024 行歌(xingge)
 * 启用请求拦截器注解
 * 
 * 功能说明：
 * - 用于启用请求拦截器功能
 * - 支持配置拦截器参数
 * - 自动导入相关配置类
 */
package tech.request.core.request.annotation;

import org.springframework.context.annotation.Import;
import tech.request.core.request.config.RequestInterceptorAutoConfiguration;
import tech.request.core.request.properties.RequestInterceptorProperty.StorageType;

import java.lang.annotation.*;

/**
 * 启用请求拦截器注解
 * 
 * <p>在Spring Boot应用的启动类上添加此注解，可启用请求拦截器功能。</p>
 * <p>示例：</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableRequestInterceptor
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </pre>
 * 
 * <p>也可以通过注解参数进行配置：</p>
 * <pre>
 * &#64;EnableRequestInterceptor(storageType = StorageType.DATABASE)
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RequestInterceptorAutoConfiguration.class)
public @interface EnableRequestInterceptor {
    
    /**
     * 是否启用请求拦截器
     * 
     * @return true表示启用，false表示禁用
     */
    boolean enabled() default true;
    
    /**
     * 数据存储类型
     * 
     * @return 存储类型枚举值
     */
    StorageType storageType() default StorageType.LOG;
    
    /**
     * 是否拦截请求头
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean includeHeaders() default true;
    
    /**
     * 是否拦截请求体
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean includeRequestBody() default true;
    
    /**
     * 是否拦截响应体
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean includeResponseBody() default true;
    
    /**
     * 是否拦截OkHttp请求
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean okHttpEnabled() default true;
    
    /**
     * 是否拦截RestTemplate请求
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean restTemplateEnabled() default true;
    
    /**
     * 是否拦截OpenFeign请求
     * 
     * @return true表示拦截，false表示不拦截
     */
    boolean openFeignEnabled() default true;
}