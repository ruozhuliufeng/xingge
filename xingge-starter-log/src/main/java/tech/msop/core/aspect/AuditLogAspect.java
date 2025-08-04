/*
 * Copyright (c) 2024 行歌(xingge)
 * 审计日志切面
 * 
 * 功能说明：
 * - 拦截带有@AuditLog注解的方法
 * - 收集审计日志信息
 * - 调用配置的处理器进行处理
 */
package tech.msop.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.msop.core.annotation.AuditLog;
import tech.msop.core.handler.AuditLogHandler;
import tech.msop.core.model.AuditLogInfo;
import tech.msop.core.property.XingGeLogProperty;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 审计日志切面
 * 
 * <p>该切面负责拦截带有@AuditLog注解的方法，收集审计信息并调用处理器进行处理。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>方法执行前后的拦截</li>
 *   <li>审计信息的收集和封装</li>
 *   <li>异常信息的捕获</li>
 *   <li>执行时间的统计</li>
 *   <li>上下文信息的提取</li>
 *   <li>多处理器的调用管理</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Slf4j
@Aspect
@Component
@Order(200) // 确保在其他切面之后执行
public class AuditLogAspect {
    
    @Autowired
    private XingGeLogProperty logProperty;
    
    @Autowired(required = false)
    private List<AuditLogHandler> auditLogHandlers = new ArrayList<>();
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    /**
     * 环绕通知：拦截带有@AuditLog注解的方法
     * 
     * @param joinPoint 连接点
     * @param auditLog 审计日志注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        
        // 检查是否启用审计日志
        if (!isAuditLogEnabled() || !auditLog.enabled()) {
            return joinPoint.proceed();
        }
        
        // 生成日志ID
        String logId = generateLogId();
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        LocalDateTime operationTime = LocalDateTime.now();
        
        // 构建基础审计信息
        AuditLogInfo.AuditLogInfoBuilder builder = AuditLogInfo.builder()
            .logId(logId)
            .operation(auditLog.operation())
            .module(auditLog.module())
            .description(auditLog.description())
            .priority(auditLog.priority())
            .operationTime(operationTime)
            .createTime(LocalDateTime.now());
        
        // 收集方法信息
        collectMethodInfo(joinPoint, auditLog, builder);
        
        // 收集上下文信息
        collectContextInfo(builder);
        
        // 收集标签
        if (auditLog.tags().length > 0) {
            builder.tags(Arrays.asList(auditLog.tags()));
        }
        
        Object result = null;
        Throwable exception = null;
        String status = AuditLogInfo.Status.SUCCESS.name();
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录返回值（如果启用）
            if (auditLog.includeResult() && result != null) {
                builder.methodResult(serializeObject(result));
            }
            
        } catch (Throwable e) {
            exception = e;
            status = AuditLogInfo.Status.ERROR.name();
            
            // 记录异常信息（如果启用）
            if (auditLog.includeException()) {
                builder.exceptionInfo(formatException(e));
            }
            
            throw e; // 重新抛出异常
            
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 完善审计信息
            AuditLogInfo auditLogInfo = builder
                .status(status)
                .executionTime(executionTime)
                .build();
            
            // 处理审计日志
            processAuditLog(auditLogInfo, auditLog.async());
        }
        
        return result;
    }
    
    /**
     * 收集方法信息
     * 
     * @param joinPoint 连接点
     * @param auditLog 审计日志注解
     * @param builder 构建器
     */
    private void collectMethodInfo(ProceedingJoinPoint joinPoint, AuditLog auditLog, 
                                  AuditLogInfo.AuditLogInfoBuilder builder) {
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 方法名
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        builder.methodName(methodName);
        
        // 方法参数（如果启用）
        if (auditLog.includeArgs()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                builder.methodArgs(serializeObject(args));
            }
        }
    }
    
    /**
     * 收集上下文信息
     * 
     * @param builder 构建器
     */
    private void collectContextInfo(AuditLogInfo.AuditLogInfoBuilder builder) {
        
        // 从MDC获取信息
        String userId = MDC.get("userId");
        String username = MDC.get("username");
        String userRole = MDC.get("userRole");
        String sessionId = MDC.get("sessionId");
        String traceId = MDC.get("traceId");
        
        builder.userId(userId)
               .username(username)
               .userRole(userRole)
               .sessionId(sessionId)
               .traceId(traceId);
        
        // 从HTTP请求获取信息
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                builder.clientIp(getClientIp(request))
                       .userAgent(request.getHeader("User-Agent"))
                       .requestUrl(request.getRequestURL().toString())
                       .httpMethod(request.getMethod());
            }
        } catch (Exception e) {
            System.out.println("获取HTTP请求信息失败: " + e.getMessage());
        }
        
        // 服务器信息
        try {
            builder.serverName(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            System.out.println("获取服务器名称失败: " + e.getMessage());
        }
        
        // 应用信息
        builder.applicationName(getApplicationName())
               .environment(getEnvironment());
    }
    
    /**
     * 处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @param async 是否异步处理
     */
    private void processAuditLog(AuditLogInfo auditLogInfo, boolean async) {
        
        if (auditLogHandlers.isEmpty()) {
            System.out.println("没有配置审计日志处理器");
            return;
        }
        
        // 过滤和排序处理器
        List<AuditLogHandler> enabledHandlers = auditLogHandlers.stream()
            .filter(handler -> handler.isEnabled() && handler.supports(auditLogInfo))
            .sorted(Comparator.comparingInt(AuditLogHandler::getPriority))
            .collect(ArrayList::new, (list, handler) -> list.add(handler), ArrayList::addAll);
        
        if (enabledHandlers.isEmpty()) {
            System.out.println("没有启用的审计日志处理器");
            return;
        }
        
        // 处理审计日志
        if (async) {
            // 异步处理
            CompletableFuture.runAsync(() -> {
                for (AuditLogHandler handler : enabledHandlers) {
                    try {
                        handler.handleAsync(auditLogInfo);
                    } catch (Exception e) {
                        System.err.println("审计日志处理器异步处理失败: " + handler.getHandlerName() + ", " + e.getMessage());
                    }
                }
            });
        } else {
            // 同步处理
            for (AuditLogHandler handler : enabledHandlers) {
                try {
                    boolean success = handler.handle(auditLogInfo);
                    if (!success) {
                        System.err.println("审计日志处理器处理失败: " + handler.getHandlerName());
                    }
                } catch (Exception e) {
                    System.err.println("审计日志处理器同步处理失败: " + handler.getHandlerName() + ", " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 序列化对象为JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串
     */
    private String serializeObject(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            if (objectMapper != null) {
                return objectMapper.writeValueAsString(obj);
            } else {
                return obj.toString();
            }
        } catch (Exception e) {
            System.out.println("对象序列化失败: " + e.getMessage());
            return obj.toString();
        }
    }
    
    /**
     * 格式化异常信息
     * 
     * @param throwable 异常
     * @return 格式化后的异常信息
     */
    private String formatException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getSimpleName())
          .append(": ")
          .append(throwable.getMessage());
        
        // 添加堆栈信息（限制长度）
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 0) {
            sb.append(" at ").append(stackTrace[0].toString());
        }
        
        return sb.toString();
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理的情况，取第一个IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 生成日志ID
     * 
     * @return 日志ID
     */
    private String generateLogId() {
        return "AUDIT_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
    
    /**
     * 检查是否启用审计日志
     * 
     * @return 是否启用
     */
    private boolean isAuditLogEnabled() {
        return logProperty.getEnabled() && 
               logProperty.getAudit() != null && 
               logProperty.getAudit().getEnabled();
    }
    
    /**
     * 获取应用名称
     * 
     * @return 应用名称
     */
    private String getApplicationName() {
        try {
            return System.getProperty("spring.application.name", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * 获取环境标识
     * 
     * @return 环境标识
     */
    private String getEnvironment() {
        try {
            String profiles = System.getProperty("spring.profiles.active");
            if (profiles != null && !profiles.isEmpty()) {
                return profiles.split(",")[0].trim();
            }
            return "default";
        } catch (Exception e) {
            return "unknown";
        }
    }
}