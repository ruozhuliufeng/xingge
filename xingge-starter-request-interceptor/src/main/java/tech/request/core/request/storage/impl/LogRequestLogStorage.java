/*
 * Copyright (c) 2024 行歌(xingge)
 * 日志请求日志存储实现
 * 
 * 功能说明：
 * - 实现日志输出方式的请求日志记录
 * - 支持自定义日志格式和级别
 * - 支持批量存储和异步存储
 * - 格式化输出拦截的内容
 */
package tech.request.core.request.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import tech.msop.core.tool.async.AsyncProcessor;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.storage.RequestLogStorage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 日志请求日志存储实现类
 * 
 * <p>该类实现了日志输出方式的请求日志记录功能，支持：</p>
 * <ul>
 *   <li>格式化输出拦截的请求和响应内容</li>
 *   <li>自定义日志级别和格式</li>
 *   <li>批量存储优化性能</li>
 *   <li>异步存储避免阻塞主线程</li>
 *   <li>美观的日志输出格式</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   request:
 *     storage-type: LOG
 *     log:
 *       enabled: true
 *       level: INFO
 *       pattern: "[REQUEST-INTERCEPTOR] %s"
 * </pre>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
@ConditionalOnProperty(name = "xg.request.log.enabled", havingValue = "true")
public class LogRequestLogStorage implements RequestLogStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(LogRequestLogStorage.class);
    
    /**
     * 请求拦截器配置属性
     */
    @Autowired
    private RequestInterceptorProperty properties;
    
    /**
     * 异步处理器
     */
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 分隔线
     */
    private static final String SEPARATOR = createRepeatedString("=", 80);
    private static final String SUB_SEPARATOR = createRepeatedString("-", 40);
    
    /**
     * 创建重复字符串（Java 8兼容）
     * 
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private static String createRepeatedString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 初始化日志存储服务
     * 
     * @throws Exception 初始化异常
     */
    @PostConstruct
    @Override
    public void initialize() throws Exception {
        try {
            logger.info("日志请求日志存储初始化成功，日志级别: {}", properties.getLog().getLevel());
        } catch (Exception e) {
            logger.error("日志请求日志存储初始化失败", e);
            throw e;
        }
    }
    
    /**
     * 存储单个请求日志
     * 
     * @param logInfo 请求日志信息
     * @throws Exception 存储异常
     */
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        try {
            
            String formattedLog = formatLogInfo(logInfo);
            String pattern = properties.getLog().getPattern();
            String finalLog = String.format(pattern, formattedLog);
            
            // 根据配置的日志级别输出
            String level = properties.getLog().getLevel().toUpperCase();
            switch (level) {
                case "DEBUG":
                    logger.debug(finalLog);
                    break;
                case "INFO":
                    logger.info(finalLog);
                    break;
                case "WARN":
                    logger.warn(finalLog);
                    break;
                case "ERROR":
                    logger.error(finalLog);
                    break;
                default:
                    logger.info(finalLog);
                    break;
            }
        } catch (Exception e) {
            logger.error("输出请求日志失败: {}", logInfo.getRequestId(), e);
            throw e;
        }
    }
    
    /**
     * 批量存储请求日志
     * 
     * @param logInfoList 请求日志信息列表
     * @throws Exception 存储异常
     */
    @Override
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        try {

            StringBuilder batchLog = new StringBuilder();
            batchLog.append("\n").append(SEPARATOR).append("\n");
            batchLog.append("批量请求日志输出 - 共 ").append(logInfoList.size()).append(" 条记录\n");
            batchLog.append(SEPARATOR).append("\n");
            
            for (int i = 0; i < logInfoList.size(); i++) {
                RequestLogInfo logInfo = logInfoList.get(i);
                batchLog.append("[第 ").append(i + 1).append(" 条]\n");
                batchLog.append(formatLogInfo(logInfo));
                if (i < logInfoList.size() - 1) {
                    batchLog.append("\n").append(SUB_SEPARATOR).append("\n");
                }
            }
            
            batchLog.append("\n").append(SEPARATOR);
            
            String pattern = properties.getLog().getPattern();
            String finalLog = String.format(pattern, batchLog);
            
            // 根据配置的日志级别输出
            String level = properties.getLog().getLevel().toUpperCase();
            switch (level) {
                case "DEBUG":
                    logger.debug(finalLog);
                    break;
                case "INFO":
                    logger.info(finalLog);
                    break;
                case "WARN":
                    logger.warn(finalLog);
                    break;
                case "ERROR":
                    logger.error(finalLog);
                    break;
                default:
                    logger.info(finalLog);
                    break;
            }
        } catch (Exception e) {
            logger.error("批量输出请求日志失败，数量: {}", logInfoList.size(), e);
            throw e;
        }
    }
    
    /**
     * 异步存储单个请求日志
     * 
     * <p>默认为异步输出，不阻碍现有业务流程，所有异常通过日志输出</p>
     * 
     * @param logInfo 请求日志信息
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return asyncProcessor.executeAsyncWithResult(
            () -> {
                try {
                    store(logInfo);
                    return null;
                } catch (Exception e) {
                    // 异常通过日志输出，不抛出异常以避免阻碍业务流程
                    logger.error("异步输出请求日志失败: {}", logInfo.getRequestId(), e);
                    throw new RuntimeException(e);
                }
            },
            "日志输出请求日志-" + logInfo.getRequestId()
        );
    }
    
    /**
     * 异步批量存储请求日志
     * 
     * <p>默认为异步输出，不阻碍现有业务流程，所有异常通过日志输出</p>
     * 
     * @param logInfoList 请求日志信息列表
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return asyncProcessor.executeAsyncWithResult(
            () -> {
                try {
                    batchStore(logInfoList);
                    return null;
                } catch (Exception e) {
                    // 异常通过日志输出，不抛出异常以避免阻碍业务流程
                    logger.error("异步批量输出请求日志失败，数量: {}", 
                            logInfoList != null ? logInfoList.size() : 0, e);
                    throw new RuntimeException(e);
                }
            },
            "日志批量输出请求日志-" + (logInfoList != null ? logInfoList.size() : 0) + "条"
        );
    }
    
    /**
     * 检查日志存储服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    @Override
    public boolean isAvailable() {
        // 日志输出总是可用的
        return true;
    }
    
    /**
     * 获取存储类型名称
     * 
     * @return 存储类型名称
     */
    @Override
    public String getStorageType() {
        return "LOG";
    }
    
    /**
     * 销毁存储服务，释放资源
     * 
     * @throws Exception 销毁异常
     */
    @PreDestroy
    @Override
    public void destroy() throws Exception {
        try {
            // 清理MDC
            logger.info("日志请求日志存储服务已销毁");
        } catch (Exception e) {
            logger.error("销毁日志请求日志存储服务失败", e);
            throw e;
        }
    }
    
    /**
     * 格式化请求日志信息
     * 
     * @param logInfo 请求日志信息
     * @return 格式化后的日志字符串
     */
    private String formatLogInfo(RequestLogInfo logInfo) {
        StringBuilder sb = new StringBuilder();
        
        // 基本信息
        sb.append("\n┌─ 请求信息 ─────────────────────────────────────────────────────────────\n");
        sb.append("│ 请求ID: ").append(logInfo.getRequestId()).append("\n");
        sb.append("│ 客户端类型: ").append(logInfo.getClientType()).append("\n");
        sb.append("│ 请求方法: ").append(logInfo.getMethod()).append("\n");
        sb.append("│ 请求URL: ").append(logInfo.getUrl()).append("\n");
        
        // 时间信息
        if (logInfo.getRequestTime() != null) {
            sb.append("│ 开始时间: ").append(logInfo.getRequestTime().format(DATE_TIME_FORMATTER)).append("\n");
        }
        if (logInfo.getResponseTime() != null) {
            sb.append("│ 结束时间: ").append(logInfo.getResponseTime().format(DATE_TIME_FORMATTER)).append("\n");
        }
        sb.append("│ 耗时: ").append(logInfo.getDuration()).append("ms\n");
        sb.append("│ 执行状态: ").append(logInfo.getSuccess() != null && logInfo.getSuccess() ? "成功" : "失败").append("\n");
        
        if ((logInfo.getSuccess() == null || !logInfo.getSuccess()) && logInfo.getErrorMessage() != null) {
            sb.append("│ 错误信息: ").append(logInfo.getErrorMessage()).append("\n");
        }
        
        // 请求头信息
        if (properties.isIncludeHeaders() && logInfo.getRequestHeaders() != null && !logInfo.getRequestHeaders().isEmpty()) {
            sb.append("├─ 请求头 ─────────────────────────────────────────────────────────────\n");
            logInfo.getRequestHeaders().forEach((key, value) -> sb.append("│ ").append(key).append(": ").append(value).append("\n"));
        }
        
        // 请求体信息
        if (properties.isIncludeRequestBody() && logInfo.getRequestBody() != null && !logInfo.getRequestBody().trim().isEmpty()) {
            sb.append("├─ 请求体 ─────────────────────────────────────────────────────────────\n");
            sb.append("│ ").append(formatBody(logInfo.getRequestBody())).append("\n");
        }
        
        // 响应信息
        sb.append("├─ 响应信息 ───────────────────────────────────────────────────────────\n");
        sb.append("│ 响应状态: ").append(logInfo.getResponseStatus()).append("\n");
        
        // 响应头信息
        if (properties.isIncludeHeaders() && logInfo.getResponseHeaders() != null && !logInfo.getResponseHeaders().isEmpty()) {
            sb.append("├─ 响应头 ─────────────────────────────────────────────────────────────\n");
            logInfo.getResponseHeaders().forEach((key, value) -> sb.append("│ ").append(key).append(": ").append(value).append("\n"));
        }
        
        // 响应体信息
        if (properties.isIncludeResponseBody() && logInfo.getResponseBody() != null && !logInfo.getResponseBody().trim().isEmpty()) {
            sb.append("├─ 响应体 ─────────────────────────────────────────────────────────────\n");
            sb.append("│ ").append(formatBody(logInfo.getResponseBody())).append("\n");
        }
        
        sb.append("└───────────────────────────────────────────────────────────────────────");
        
        return sb.toString();
    }
    
    /**
     * 格式化请求体或响应体内容
     * 
     * @param body 原始内容
     * @return 格式化后的内容
     */
    private String formatBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "(空)";
        }
        
        // 如果内容过长，进行截断
        if (body.length() > properties.getMaxBodySize()) {
            return body.substring(0, (int) properties.getMaxBodySize()) + "... (内容已截断)";
        }
        
        // 尝试格式化JSON内容
        String trimmed = body.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || 
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try {
                // 简单的JSON格式化（添加换行和缩进）
                return formatJson(trimmed);
            } catch (Exception e) {
                // 如果格式化失败，返回原始内容
                return body;
            }
        }
        
        return body;
    }
    
    /**
     * 简单的JSON格式化
     * 
     * @param json JSON字符串
     * @return 格式化后的JSON字符串
     */
    private String formatJson(String json) {
        StringBuilder formatted = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        boolean escape = false;
        
        for (char c : json.toCharArray()) {
            if (escape) {
                formatted.append(c);
                escape = false;
                continue;
            }
            
            if (c == '\\') {
                formatted.append(c);
                escape = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                formatted.append(c);
                continue;
            }
            
            if (inString) {
                formatted.append(c);
                continue;
            }
            
            switch (c) {
                case '{':
                case '[':
                    formatted.append(c).append('\n');
                    indent++;
                    for (int i = 0; i < indent; i++) {
                        formatted.append("  ");
                    }
                    break;
                case '}':
                case ']':
                    formatted.append('\n');
                    indent--;
                    for (int i = 0; i < indent; i++) {
                        formatted.append("  ");
                    }
                    formatted.append(c);
                    break;
                case ',':
                    formatted.append(c).append('\n');
                    for (int i = 0; i < indent; i++) {
                        formatted.append("  ");
                    }
                    break;
                case ':':
                    formatted.append(c).append(' ');
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        formatted.append(c);
                    }
                    break;
            }
        }
        
        return formatted.toString();
    }
}