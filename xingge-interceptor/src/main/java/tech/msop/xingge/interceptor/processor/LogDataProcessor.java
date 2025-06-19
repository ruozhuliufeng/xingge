package tech.msop.xingge.interceptor.processor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessor;
import tech.msop.xingge.interceptor.InterceptData;

import java.util.Map;

/**
 * 日志数据处理器
 * 记录拦截数据到日志，并添加相关索引
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class LogDataProcessor implements DataProcessor {

    private static final String PROCESSOR_TYPE = "log";
    
    // 配置键
    private static final String CONFIG_LOGGER_NAME = "loggerName";
    private static final String CONFIG_LOG_LEVEL = "logLevel";
    private static final String CONFIG_INCLUDE_HEADERS = "includeHeaders";
    private static final String CONFIG_INCLUDE_BODY = "includeBody";
    private static final String CONFIG_MDC_PREFIX = "mdcPrefix";

    @Override
    public String getType() {
        return PROCESSOR_TYPE;
    }

    @Override
    public void process(InterceptData data, Map<String, Object> config) {
        try {
            // 设置 MDC 上下文，用于日志索引
            setMDCContext(data, config);
            
            // 构建日志消息
            String logMessage = buildLogMessage(data, config);
            
            // 根据配置的日志级别输出日志
            String logLevel = (String) config.getOrDefault(CONFIG_LOG_LEVEL, "INFO");
            String loggerName = (String) config.getOrDefault(CONFIG_LOGGER_NAME, "INTERCEPTOR");
            
            writeLog(loggerName, logLevel, logMessage);
            
        } catch (Exception e) {
            log.error("日志处理器处理数据时发生异常", e);
        } finally {
            // 清理 MDC 上下文
            clearMDCContext(config);
        }
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级，优先记录日志
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        // 验证日志级别
        String logLevel = (String) config.get(CONFIG_LOG_LEVEL);
        if (logLevel != null) {
            try {
                org.slf4j.event.Level.valueOf(logLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的日志级别: {}", logLevel);
                return false;
            }
        }
        return true;
    }

    /**
     * 设置 MDC 上下文
     */
    private void setMDCContext(InterceptData data, Map<String, Object> config) {
        String mdcPrefix = (String) config.getOrDefault(CONFIG_MDC_PREFIX, "intercept");
        
        MDC.put(mdcPrefix + ".id", data.getId());
        MDC.put(mdcPrefix + ".type", data.getInterceptType());
        MDC.put(mdcPrefix + ".scope", data.getInterceptScope());
        MDC.put(mdcPrefix + ".method", data.getMethod());
        MDC.put(mdcPrefix + ".url", data.getUrl());
        MDC.put(mdcPrefix + ".path", data.getPath());
        
        if (data.getResponseStatus() != null) {
            MDC.put(mdcPrefix + ".status", String.valueOf(data.getResponseStatus()));
        }
        
        if (data.getDuration() != null) {
            MDC.put(mdcPrefix + ".duration", String.valueOf(data.getDuration()));
        }
        
        if (data.getClientIp() != null) {
            MDC.put(mdcPrefix + ".clientIp", data.getClientIp());
        }
        
        if (data.getUserId() != null) {
            MDC.put(mdcPrefix + ".userId", data.getUserId());
        }
        
        if (data.getSessionId() != null) {
            MDC.put(mdcPrefix + ".sessionId", data.getSessionId());
        }
        
        if (data.getTenantId() != null) {
            MDC.put(mdcPrefix + ".tenantId", data.getTenantId());
        }
        
        if (data.getApplicationName() != null) {
            MDC.put(mdcPrefix + ".app", data.getApplicationName());
        }
    }

    /**
     * 清理 MDC 上下文
     */
    private void clearMDCContext(Map<String, Object> config) {
        String mdcPrefix = (String) config.getOrDefault(CONFIG_MDC_PREFIX, "intercept");
        
        MDC.remove(mdcPrefix + ".id");
        MDC.remove(mdcPrefix + ".type");
        MDC.remove(mdcPrefix + ".scope");
        MDC.remove(mdcPrefix + ".method");
        MDC.remove(mdcPrefix + ".url");
        MDC.remove(mdcPrefix + ".path");
        MDC.remove(mdcPrefix + ".status");
        MDC.remove(mdcPrefix + ".duration");
        MDC.remove(mdcPrefix + ".clientIp");
        MDC.remove(mdcPrefix + ".userId");
        MDC.remove(mdcPrefix + ".sessionId");
        MDC.remove(mdcPrefix + ".tenantId");
        MDC.remove(mdcPrefix + ".app");
    }

    /**
     * 构建日志消息
     */
    private String buildLogMessage(InterceptData data, Map<String, Object> config) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[拦截数据] ");
        sb.append("类型=").append(data.getInterceptType());
        sb.append(", 范围=").append(data.getInterceptScope());
        sb.append(", 方法=").append(data.getMethod());
        sb.append(", URL=").append(data.getUrl());
        
        if (data.getResponseStatus() != null) {
            sb.append(", 状态=").append(data.getResponseStatus());
        }
        
        if (data.getDuration() != null) {
            sb.append(", 耗时=").append(data.getDuration()).append("ms");
        }
        
        if (data.getClientIp() != null) {
            sb.append(", IP=").append(data.getClientIp());
        }
        
        // 是否包含请求头
        boolean includeHeaders = (Boolean) config.getOrDefault(CONFIG_INCLUDE_HEADERS, false);
        if (includeHeaders && data.getHeaders() != null && !data.getHeaders().isEmpty()) {
            sb.append(", 请求头=").append(data.getHeaders());
        }
        
        // 是否包含请求体和响应体
        boolean includeBody = (Boolean) config.getOrDefault(CONFIG_INCLUDE_BODY, false);
        if (includeBody) {
            if (data.getRequestBody() != null) {
                sb.append(", 请求体=").append(data.getRequestBody());
            }
            if (data.getResponseBody() != null) {
                sb.append(", 响应体=").append(data.getResponseBody());
            }
        }
        
        if (data.getException() != null) {
            sb.append(", 异常=").append(data.getException());
        }
        
        return sb.toString();
    }

    /**
     * 写入日志
     */
    private void writeLog(String loggerName, String logLevel, String message) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(loggerName);
        
        switch (logLevel.toUpperCase()) {
            case "TRACE":
                logger.trace(message);
                break;
            case "DEBUG":
                logger.debug(message);
                break;
            case "INFO":
                logger.info(message);
                break;
            case "WARN":
                logger.warn(message);
                break;
            case "ERROR":
                logger.error(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }
}