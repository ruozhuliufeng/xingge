package tech.msop.xingge.interceptor.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.msop.xingge.interceptor.DataProcessorManager;
import tech.msop.xingge.interceptor.InterceptData;
import tech.msop.xingge.interceptor.InterceptorProperties;
import tech.msop.xingge.interceptor.InterceptorProperties.InterceptorConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HTTP请求拦截器
 * 拦截外部HTTP请求和响应数据
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class HttpRequestInterceptor implements HandlerInterceptor {

    @Autowired
    private InterceptorProperties interceptorProperties;
    
    @Autowired
    private DataProcessorManager dataProcessorManager;
    
    // 请求开始时间
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    
    // 请求体缓存
    private static final String REQUEST_BODY_CACHE = "REQUEST_BODY_CACHE";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        
        // 缓存请求体（如果需要）
        if (shouldRecordRequestBody()) {
            String requestBody = getRequestBody(request);
            request.setAttribute(REQUEST_BODY_CACHE, requestBody);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
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
            InterceptData data = buildInterceptData(request, response, ex);
            
            // 处理数据
            dataProcessorManager.processData(data, config.getProcessors());
            
        } catch (Exception e) {
            log.error("HTTP请求拦截器处理时发生异常", e);
        }
    }

    /**
     * 查找匹配的拦截器配置
     */
    private InterceptorConfig findMatchingConfig(HttpServletRequest request) {
        List<InterceptorConfig> configs = interceptorProperties.getInterceptors();
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        for (InterceptorConfig config : configs) {
            // 检查拦截类型
            if (!"request".equals(config.getInterceptType()) && !"all".equals(config.getInterceptType())) {
                continue;
            }
            
            // 检查拦截范围
            if (!"external".equals(config.getInterceptScope()) && !"all".equals(config.getInterceptScope())) {
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
    private InterceptData buildInterceptData(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        InterceptData data = new InterceptData();
        
        // 基础信息
        data.setId(UUID.randomUUID().toString().replace("-", ""));
        data.setInterceptType("request");
        data.setInterceptScope("external");
        data.setTimestamp(new Date());
        
        // 请求信息
        data.setMethod(request.getMethod());
        data.setUrl(getFullURL(request));
        data.setPath(request.getRequestURI());
        data.setQueryParams(getQueryParams(request));
        data.setHeaders(getHeaders(request));
        
        // 请求体
        if (shouldRecordRequestBody()) {
            String requestBody = (String) request.getAttribute(REQUEST_BODY_CACHE);
            data.setRequestBody(requestBody);
        }
        
        // 响应信息
        data.setResponseStatus(response.getStatus());
        data.setResponseHeaders(getResponseHeaders(response));
        
        // 响应体（注意：在拦截器中很难获取响应体，可能需要使用Filter）
        // data.setResponseBody(responseBody);
        
        // 耗时
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        if (startTime != null) {
            data.setDuration(System.currentTimeMillis() - startTime);
        }
        
        // 客户端信息
        data.setClientIp(getClientIp(request));
        data.setUserAgent(request.getHeader("User-Agent"));
        
        // 用户信息（需要根据实际业务获取）
        data.setUserId(getUserId(request));
        data.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);
        data.setTenantId(getTenantId(request));
        
        // 应用信息
        data.setApplicationName(getApplicationName());
        
        // 异常信息
        if (ex != null) {
            data.setException(ex.getMessage());
        }
        
        return data;
    }

    /**
     * 获取完整URL
     */
    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    /**
     * 获取查询参数
     */
    private Map<String, String> getQueryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        
        return params;
    }

    /**
     * 获取请求头
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }

    /**
     * 获取响应头
     */
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            return sb.toString();
        } catch (IOException e) {
            log.warn("读取请求体失败", e);
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取用户ID（需要根据实际业务实现）
     */
    private String getUserId(HttpServletRequest request) {
        // 这里可以从JWT token、session或其他方式获取用户ID
        // 示例实现：从请求头获取
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            return userId;
        }
        
        // 从session获取
        Object userIdObj = request.getSession(false) != null ? 
                request.getSession().getAttribute("userId") : null;
        if (userIdObj != null) {
            return userIdObj.toString();
        }
        
        return null;
    }

    /**
     * 获取租户ID（需要根据实际业务实现）
     */
    private String getTenantId(HttpServletRequest request) {
        // 这里可以从请求头、域名或其他方式获取租户ID
        String tenantId = request.getHeader("X-Tenant-Id");
        if (tenantId != null) {
            return tenantId;
        }
        
        // 从参数获取
        tenantId = request.getParameter("tenantId");
        if (tenantId != null) {
            return tenantId;
        }
        
        return null;
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
}