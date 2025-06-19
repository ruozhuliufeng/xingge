package tech.msop.xingge.interceptor.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessorManager;
import tech.msop.xingge.interceptor.InterceptData;
import tech.msop.xingge.interceptor.InterceptorProperties;
import tech.msop.xingge.interceptor.InterceptorProperties.InterceptorConfig;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * OkHttp拦截器
 * 拦截OkHttp客户端的请求和响应数据
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class OkHttpInterceptor implements Interceptor {

    @Autowired
    private InterceptorProperties interceptorProperties;
    
    @Autowired
    private DataProcessorManager dataProcessorManager;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.currentTimeMillis();
        Response response = null;
        Exception exception = null;
        
        try {
            // 执行请求
            response = chain.proceed(request);
            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                // 处理拦截数据
                processInterceptData(request, response, startTime, exception);
            } catch (Exception e) {
                log.error("OkHttp拦截器处理时发生异常", e);
            }
        }
    }

    /**
     * 处理拦截数据
     */
    private void processInterceptData(Request request, Response response, long startTime, Exception exception) {
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
            InterceptData data = buildInterceptData(request, response, startTime, exception);
            
            // 处理数据
            dataProcessorManager.processData(data, config.getProcessors());
            
        } catch (Exception e) {
            log.error("处理OkHttp拦截数据时发生异常", e);
        }
    }

    /**
     * 查找匹配的拦截器配置
     */
    private InterceptorConfig findMatchingConfig(Request request) {
        List<InterceptorConfig> configs = interceptorProperties.getInterceptors();
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        
        HttpUrl httpUrl = request.url();
        String requestPath = httpUrl.encodedPath();
        String method = request.method();
        
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
    private InterceptData buildInterceptData(Request request, Response response, long startTime, Exception exception) {
        InterceptData data = new InterceptData();
        
        // 基础信息
        data.setId(UUID.randomUUID().toString().replace("-", ""));
        data.setInterceptType("request");
        data.setInterceptScope("internal");
        data.setTimestamp(new Date());
        
        // 请求信息
        HttpUrl httpUrl = request.url();
        data.setMethod(request.method());
        data.setUrl(httpUrl.toString());
        data.setPath(httpUrl.encodedPath());
        data.setQueryParams(getQueryParams(httpUrl));
        data.setHeaders(getRequestHeaders(request));
        
        // 请求体
        if (shouldRecordRequestBody()) {
            String requestBody = getRequestBody(request);
            data.setRequestBody(requestBody);
        }
        
        // 响应信息
        if (response != null) {
            data.setResponseStatus(response.code());
            data.setResponseHeaders(getResponseHeaders(response));
            
            // 响应体
            if (shouldRecordResponseBody()) {
                String responseBody = getResponseBody(response);
                data.setResponseBody(responseBody);
            }
        }
        
        // 耗时
        data.setDuration(System.currentTimeMillis() - startTime);
        
        // 目标服务信息
        data.setTargetHost(httpUrl.host());
        data.setTargetPort(httpUrl.port());
        
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
    private Map<String, String> getQueryParams(HttpUrl httpUrl) {
        Map<String, String> params = new HashMap<>();
        
        for (int i = 0; i < httpUrl.querySize(); i++) {
            String name = httpUrl.queryParameterName(i);
            String value = httpUrl.queryParameterValue(i);
            params.put(name, value);
        }
        
        return params;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getRequestHeaders(Request request) {
        Map<String, String> headers = new HashMap<>();
        Headers requestHeaders = request.headers();
        
        for (String name : requestHeaders.names()) {
            headers.put(name, requestHeaders.get(name));
        }
        
        return headers;
    }

    /**
     * 获取响应头
     */
    private Map<String, String> getResponseHeaders(Response response) {
        Map<String, String> headers = new HashMap<>();
        Headers responseHeaders = response.headers();
        
        for (String name : responseHeaders.names()) {
            headers.put(name, responseHeaders.get(name));
        }
        
        return headers;
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(Request request) {
        try {
            RequestBody requestBody = request.body();
            if (requestBody == null) {
                return null;
            }
            
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("读取OkHttp请求体失败", e);
            return null;
        }
    }

    /**
     * 获取响应体
     */
    private String getResponseBody(Response response) {
        try {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return null;
            }
            
            // 注意：这里会消费响应体，在实际使用中可能需要特殊处理
            String content = responseBody.string();
            
            // 重新创建响应体，以免影响后续处理
            ResponseBody newResponseBody = ResponseBody.create(
                responseBody.contentType(), content);
            response = response.newBuilder().body(newResponseBody).build();
            
            return content;
        } catch (Exception e) {
            log.warn("读取OkHttp响应体失败", e);
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