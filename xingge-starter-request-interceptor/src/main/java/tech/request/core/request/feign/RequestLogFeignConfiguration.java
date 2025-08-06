/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志Feign配置
 * 
 * 功能说明：
 * - 配置Feign客户端的行为
 * - 设置超时时间和重试策略
 * - 配置日志级别和错误处理
 */
package tech.request.core.request.feign;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 请求日志Feign配置类
 * 
 * <p>该配置类用于定制Feign客户端的行为，包括：</p>
 * <ul>
 *   <li>超时时间配置</li>
 *   <li>重试策略配置</li>
 *   <li>日志级别配置</li>
 *   <li>错误解码器配置</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Configuration
public class RequestLogFeignConfiguration {
    
    /**
     * 配置Feign请求选项
     * 
     * @return 请求选项
     */
    @Bean
    public Request.Options requestOptions() {
        // 连接超时：5秒，读取超时：10秒
        return new Request.Options(
            5000, TimeUnit.MILLISECONDS,  // 连接超时
            10000, TimeUnit.MILLISECONDS, // 读取超时
            true  // 跟随重定向
        );
    }
    
    /**
     * 配置重试策略
     * 
     * @return 重试器
     */
    @Bean
    public Retryer retryer() {
        // 重试间隔：1秒，最大重试间隔：3秒，最大重试次数：3次
        return new Retryer.Default(1000, 3000, 3);
    }
    
    /**
     * 配置日志级别
     * 
     * @return 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        // 记录请求和响应的基本信息
        return Logger.Level.BASIC;
    }
    
    /**
     * 配置错误解码器
     * 
     * @return 错误解码器
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new RequestLogErrorDecoder();
    }
    
    /**
     * 自定义错误解码器
     */
    public static class RequestLogErrorDecoder implements ErrorDecoder {
        
        private final ErrorDecoder defaultErrorDecoder = new Default();
        
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // 对于请求日志API的错误，进行特殊处理
            if (response.status() >= 400 && response.status() < 500) {
                // 客户端错误，不重试
                return new RequestLogApiException(
                    "请求日志API客户端错误: " + response.status() + " - " + response.reason(),
                    response.status(),
                    false
                );
            } else if (response.status() >= 500) {
                // 服务器错误，可以重试
                return new RequestLogApiException(
                    "请求日志API服务器错误: " + response.status() + " - " + response.reason(),
                    response.status(),
                    true
                );
            }
            
            // 其他错误使用默认处理
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
    
    /**
     * 请求日志API异常
     */
    public static class RequestLogApiException extends Exception {
        
        /**
         * HTTP状态码
         */
        private final int status;
        
        /**
         * 是否可重试
         */
        private final boolean retryable;
        
        /**
         * 构造函数
         * 
         * @param message 异常消息
         * @param status HTTP状态码
         * @param retryable 是否可重试
         */
        public RequestLogApiException(String message, int status, boolean retryable) {
            super(message);
            this.status = status;
            this.retryable = retryable;
        }
        
        /**
         * 获取HTTP状态码
         * 
         * @return HTTP状态码
         */
        public int getStatus() {
            return status;
        }
        
        /**
         * 是否可重试
         * 
         * @return true表示可重试，false表示不可重试
         */
        public boolean isRetryable() {
            return retryable;
        }
    }
}