package tech.msop.xingge.interceptor.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessorManager;
import tech.msop.xingge.interceptor.InterceptData;
import tech.msop.xingge.interceptor.InterceptorProperties;
import tech.msop.xingge.interceptor.InterceptorProperties.InterceptorConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HTTP客户端拦截器
 * 拦截内部向外部发送的HTTP请求和响应数据
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class HttpClientInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private InterceptorProperties interceptorProperties;
    
    @Autowired
    private DataProcessorManager dataProcessorManager;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = null;
        Exception exception = null;
        
        try {
            // 执行请求
            response = execution.execute(request, body);
            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                // 处理拦截数据
                processInterceptData(request, body, response, startTime, exception);
            } catch (Exception e) {
                log.error("HTTP客户端拦截器处理时发生异常", e);
            }
        }
    }

    /**
     * 处理拦截数据
     */
    private void processInterceptData(HttpRequest request, byte[] requestBody, 
                                    ClientHttpResponse response, long startTime, Exception exception) {
        try {
            // 检查是否启用拦截
            if (!interceptorProperties.isEnabled()) {
                return;
            }
            
            // 查找匹配的拦截器配置
            InterceptorConfig config = findMatchingConfig(request);
            if (config == null) {
                return;
            }
            
            // 构建拦截数据
            InterceptData data = buildInterceptData(request, requestBody, response, startTime, exception);
            
            // 处理数据
            dataProcessorManager.processData(data, config.getProcessors());
            
        } catch (Exception e) {
            log.error("处理HTTP客户端拦截数据时发生异常", e);
        }
    }

    /**
     * 查找匹配的拦截器配置
     */
    private InterceptorConfig findMatchingConfig(HttpRequest request) {
        List<InterceptorConfig> configs = interceptorProperties.getInterceptors();
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        
        URI uri = request.getURI();
        String requestPath = uri.getPath();
        String method = request.getMethod().name();
        
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
    private InterceptData buildInterceptData(HttpRequest request, byte[] requestBody, 
                                           ClientHttpResponse response, long startTime, Exception exception) {
        InterceptData data = new InterceptData();
        
        // 基础信息
        data.setId(UUID.randomUUID().toString().replace("-", ""));
        data.setInterceptType("request");
        data.setInterceptScope("internal");
        data.setTimestamp(new Date());
        
        // 请求信息
        URI uri = request.getURI();
        data.setMethod(request.getMethod().name());
        data.setUrl(uri.toString());
        data.setPath(uri.getPath());
        data.setQueryParams(getQueryParams(uri));
        data.setHeaders(getRequestHeaders(request));
        
        // 请求体
        if (shouldRecordRequestBody() && requestBody != null && requestBody.length > 0) {
            data.setRequestBody(new String(requestBody, StandardCharsets.UTF_8));
        }
        
        // 响应信息
        if (response != null) {
            try {
                data.setResponseStatus(response.getStatusCode().value());
                data.setResponseHeaders(getResponseHeaders(response));
                
                // 响应体
                if (shouldRecordResponseBody()) {
                    String responseBody = getResponseBody(response);
                    data.setResponseBody(responseBody);
                }
            } catch (IOException e) {
                log.warn("读取响应信息失败", e);
            }
        }
        
        // 耗时
        data.setDuration(System.currentTimeMillis() - startTime);
        
        // 目标服务信息
        data.setTargetHost(uri.getHost());
        data.setTargetPort(uri.getPort());
        
        // 应用信息
        data.setApplicationName(getApplicationName());
        
        // 异常信息
        if (exception != null) {
            data.setException(exception.getMessage());
        }
        
        return data;
    }

    /**
     * 获取查询参数
     */
    private Map<String, String> getQueryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        
        return params;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(HttpRequest request) {
        Map<String, String> headers = new HashMap<>();
        
        request.getHeaders().forEach((name, values) -> {
            if (values != null && !values.isEmpty()) {
                headers.put(name, String.join(",", values));
            }
        });
        
        return headers;
    }

    /**
     * 获取响应头
     */
    private Map<String, String> getResponseHeaders(ClientHttpResponse response) {
        Map<String, String> headers = new HashMap<>();
        
        response.getHeaders().forEach((name, values) -> {
            if (values != null && !values.isEmpty()) {
                headers.put(name, String.join(",", values));
            }
        });
        
        return headers;
    }

    /**
     * 获取响应体
     */
    private String getResponseBody(ClientHttpResponse response) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
            
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.warn("读取响应体失败", e);
            return null;
        }
    }

    /**
     * 获取应用名称
     */
    private String getApplicationName() {
        // 可以从配置文件或环境变量获取
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

    /**
     * 是否应该记录响应体
     */
    private boolean shouldRecordResponseBody() {
        InterceptorProperties.GlobalConfig globalConfig = interceptorProperties.getGlobal();
        if (globalConfig != null) {
            return globalConfig.isRecordResponseBody();
        }
        return false;
    }
}