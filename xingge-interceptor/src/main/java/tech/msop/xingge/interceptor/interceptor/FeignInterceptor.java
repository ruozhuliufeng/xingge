package tech.msop.xingge.interceptor.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessorManager;
import tech.msop.xingge.interceptor.InterceptData;
import tech.msop.xingge.interceptor.InterceptorProperties;
import tech.msop.xingge.interceptor.InterceptorProperties.InterceptorConfig;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Feign请求拦截器
 * 拦截Feign客户端的请求数据
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class FeignInterceptor implements RequestInterceptor {

    @Autowired
    private InterceptorProperties interceptorProperties;
    
    @Autowired
    private DataProcessorManager dataProcessorManager;

    @Override
    public void apply(RequestTemplate template) {
        try {
            // 检查是否启用拦截
            if (!interceptorProperties.isEnabled()) {
                return;
            }
            
            // 查找匹配的拦截器配置
            InterceptorConfig config = findMatchingConfig(template);
            if (config == null) {
                return;
            }
            
            // 构建拦截数据
            InterceptData data = buildInterceptData(template);
            
            // 处理数据
            dataProcessorManager.processData(data, config.getProcessors());
            
        } catch (Exception e) {
            log.error("Feign拦截器处理时发生异常", e);
        }
    }

    /**
     * 查找匹配的拦截器配置
     */
    private InterceptorConfig findMatchingConfig(RequestTemplate template) {
        List<InterceptorConfig> configs = interceptorProperties.getInterceptors();
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        
        String url = template.url();
        String requestPath = extractPath(url);
        String method = template.method();
        
        for (InterceptorConfig config : configs) {
            // 检查拦截类型
            if (!"request".equals(config.getInterceptType()) && !"all".equals(config.getInterceptType())) {
                continue;
            }
            
            // 检查拦截范围
            if (!"internal".equals(config.getInterceptScope()) && !"all".equals(config.getInterceptScope())) {
                continue;
            }
            
            // 检查URL匹配
            if (isUrlMatched(requestPath, method, config)) {
                return config;
            }
        }
        
        return null;
    }

    /**
     * 从URL中提取路径
     */
    private String extractPath(String url) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URI uri = URI.create(url);
                return uri.getPath();
            } else {
                // 相对路径
                int queryIndex = url.indexOf('?');
                if (queryIndex > 0) {
                    return url.substring(0, queryIndex);
                }
                return url;
            }
        } catch (Exception e) {
            log.warn("解析URL路径失败: {}", url, e);
            return url;
        }
    }

    /**
     * 检查URL是否匹配
     */
    private boolean isUrlMatched(String requestPath, String method, InterceptorConfig config) {
        List<String> includePatterns = config.getIncludePatterns();
        List<String> excludePatterns = config.getExcludePatterns();
        List<String> methods = config.getMethods();
        
        // 检查HTTP方法
        if (methods != null && !methods.isEmpty() && !methods.contains(method.toUpperCase())) {
            return false;
        }
        
        // 检查排除模式
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            for (String pattern : excludePatterns) {
                if (isPatternMatched(requestPath, pattern)) {
                    return false;
                }
            }
        }
        
        // 检查包含模式
        if (includePatterns == null || includePatterns.isEmpty()) {
            return true; // 没有包含模式，默认匹配所有
        }
        
        for (String pattern : includePatterns) {
            if (isPatternMatched(requestPath, pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查路径是否匹配模式
     */
    private boolean isPatternMatched(String path, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }
        
        // 简单的通配符匹配
        if (pattern.contains("*")) {
            String regexPattern = pattern.replace("*", ".*");
            return Pattern.matches(regexPattern, path);
        }
        
        // 精确匹配
        return pattern.equals(path);
    }

    /**
     * 构建拦截数据
     */
    private InterceptData buildInterceptData(RequestTemplate template) {
        InterceptData data = new InterceptData();
        
        // 基础信息
        data.setId(UUID.randomUUID().toString().replace("-", ""));
        data.setInterceptType("request");
        data.setInterceptScope("internal");
        data.setTimestamp(new Date());
        
        // 请求信息
        String url = template.url();
        data.setMethod(template.method());
        data.setUrl(url);
        data.setPath(extractPath(url));
        data.setQueryParams(getQueryParams(template));
        data.setHeaders(getRequestHeaders(template));
        
        // 请求体
        if (shouldRecordRequestBody()) {
            String requestBody = getRequestBody(template);
            data.setRequestBody(requestBody);
        }
        
        // 目标服务信息
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URI uri = URI.create(url);
                data.setTargetHost(uri.getHost());
                data.setTargetPort(uri.getPort());
            }
        } catch (Exception e) {
            log.warn("解析目标服务信息失败: {}", url, e);
        }
        
        // 应用信息
        data.setApplicationName(getApplicationName());
        
        // Feign特有信息
        data.setClientType("feign");
        
        return data;
    }

    /**
     * 获取查询参数
     */
    private Map<String, String> getQueryParams(RequestTemplate template) {
        Map<String, String> params = new HashMap<>();
        
        // 从URL中解析查询参数
        String url = template.url();
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0 && queryIndex < url.length() - 1) {
            String queryString = url.substring(queryIndex + 1);
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        
        // 从模板查询参数中获取
        Map<String, Collection<String>> queries = template.queries();
        if (queries != null) {
            for (Map.Entry<String, Collection<String>> entry : queries.entrySet()) {
                Collection<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    params.put(entry.getKey(), String.join(",", values));
                }
            }
        }
        
        return params;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(RequestTemplate template) {
        Map<String, String> headers = new HashMap<>();
        
        Map<String, Collection<String>> templateHeaders = template.headers();
        if (templateHeaders != null) {
            for (Map.Entry<String, Collection<String>> entry : templateHeaders.entrySet()) {
                Collection<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    headers.put(entry.getKey(), String.join(",", values));
                }
            }
        }
        
        return headers;
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(RequestTemplate template) {
        try {
            byte[] body = template.body();
            if (body != null && body.length > 0) {
                return new String(body, StandardCharsets.UTF_8);
            }
            return null;
        } catch (Exception e) {
            log.warn("读取Feign请求体失败", e);
            return null;
        }
    }

    /**
     * 获取应用名称
     */
    private String getApplicationName() {
        return System.getProperty("spring.application.name", "unknown");
    }

    /**
     * 是否应该记录请求体
     */
    private boolean shouldRecordRequestBody() {
        InterceptorProperties.GlobalConfig globalConfig = interceptorProperties.getGlobal();
        if (globalConfig != null) {
            return globalConfig.isRecordRequestBody();
        }
        return false;
    }
}