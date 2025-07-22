/*
 * Copyright (c) 2024 行歌(xingge)
 * 日志索引切面处理类
 * 
 * 功能说明：
 * - 拦截带有LogIndex注解的字段
 * - 自动将字段值添加到MDC中
 * - 支持自定义索引名称和前缀
 * - 提供日志上下文管理
 */
package tech.msop.core.log.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.msop.core.log.annotation.LogIndex;
import tech.msop.core.log.property.XingGeLogProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志索引切面处理类
 * 
 * <p>该切面用于自动处理带有{@link LogIndex}注解的字段，
 * 将字段值作为索引添加到MDC（Mapped Diagnostic Context）中，
 * 便于日志追踪和检索。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>自动扫描对象中带有LogIndex注解的字段</li>
 *   <li>将字段值添加到MDC中，支持自定义索引名称和前缀</li>
 *   <li>在方法执行前设置MDC，执行后清理MDC</li>
 *   <li>支持嵌套对象的索引字段处理</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>请求日志追踪</li>
 *   <li>业务操作日志索引</li>
 *   <li>分布式链路追踪</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Aspect
@Component
@Order(1)
public class LogIndexAspect {
    
    /**
     * 线程本地存储，用于保存当前线程的MDC键集合
     * 确保在方法执行完成后能够正确清理MDC
     */
    private static final ThreadLocal<Map<String, String>> MDC_KEYS_HOLDER = new ThreadLocal<>();
    
    /**
     * 日志配置属性
     */
    @Autowired
    private XingGeLogProperty logProperty;
    
    /**
     * 方法执行前处理
     * 
     * <p>扫描方法参数中带有LogIndex注解的字段，
     * 将字段值添加到MDC中。</p>
     * 
     * @param joinPoint 连接点信息
     */
    @Before("execution(* *(..))")
    public void beforeMethod(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }
            
            Map<String, String> mdcKeys = new HashMap<>();
            
            // 遍历所有方法参数
            for (Object arg : args) {
                if (arg != null) {
                    processLogIndexFields(arg, mdcKeys);
                }
            }
            
            // 保存MDC键集合到线程本地存储
            if (!mdcKeys.isEmpty()) {
                MDC_KEYS_HOLDER.set(mdcKeys);
                if (isDebugEnabled()) {
                    System.out.println("LogIndex切面处理完成，添加了" + mdcKeys.size() + "个MDC索引");
                }
            }
            
        } catch (Exception e) {
            System.err.println("LogIndex切面处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 方法执行后处理
     * 
     * <p>清理当前线程的MDC上下文，
     * 避免内存泄漏和上下文污染。</p>
     * 
     * @param joinPoint 连接点信息
     */
    @After("execution(* *(..))")
    public void afterMethod(JoinPoint joinPoint) {
        try {
            // 检查是否需要清理MDC
            if (!isClearAfterMethodEnabled()) {
                return;
            }
            
            Map<String, String> mdcKeys = MDC_KEYS_HOLDER.get();
            if (mdcKeys != null && !mdcKeys.isEmpty()) {
                // 清理MDC中的索引键
                for (String key : mdcKeys.keySet()) {
                    MDC.remove(key);
                }
                if (isDebugEnabled()) {
                    System.out.println("LogIndex切面清理完成，移除了" + mdcKeys.size() + "个MDC索引");
                }
            }
        } catch (Exception e) {
            System.err.println("LogIndex切面清理异常: " + e.getMessage());
        } finally {
            // 清理线程本地存储
            MDC_KEYS_HOLDER.remove();
        }
    }
    
    /**
     * 处理对象中带有LogIndex注解的字段
     * 
     * <p>递归扫描对象及其父类中的所有字段，
     * 找到带有LogIndex注解的字段并处理。</p>
     * 
     * @param obj 要处理的对象
     * @param mdcKeys MDC键值对集合
     */
    private void processLogIndexFields(Object obj, Map<String, String> mdcKeys) {
        if (obj == null) {
            return;
        }
        
        Class<?> clazz = obj.getClass();
        
        // 跳过基本类型和常见的系统类
        if (isSkipClass(clazz)) {
            return;
        }
        
        // 检查是否启用嵌套扫描
        if (!isNestedScanEnabled() && isNestedObject(obj)) {
            return;
        }
        
        // 处理当前类及其父类的字段
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                LogIndex logIndex = field.getAnnotation(LogIndex.class);
                if (logIndex != null && logIndex.enabled()) {
                    processLogIndexField(obj, field, logIndex, mdcKeys);
                }
            }
            
            clazz = clazz.getSuperclass();
        }
    }
    
    /**
     * 处理单个带有LogIndex注解的字段
     * 
     * <p>提取字段值并添加到MDC中，
     * 支持自定义索引名称和前缀。</p>
     * 
     * @param obj 字段所属对象
     * @param field 字段信息
     * @param logIndex LogIndex注解信息
     * @param mdcKeys MDC键值对集合
     */
    private void processLogIndexField(Object obj, Field field, LogIndex logIndex, Map<String, String> mdcKeys) {
        try {
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            
            if (fieldValue != null) {
                String indexName = StringUtils.hasText(logIndex.name()) ? 
                    logIndex.name() : field.getName();
                
                String indexKey = StringUtils.hasText(logIndex.prefix()) ? 
                    logIndex.prefix() + indexName : indexName;
                
                String indexValue = fieldValue.toString();
                
                // 应用长度限制
                indexKey = truncateString(indexKey, getMaxKeyLength());
                indexValue = truncateString(indexValue, getMaxValueLength());
                
                // 添加到MDC
                MDC.put(indexKey, indexValue);
                mdcKeys.put(indexKey, indexValue);
                
                if (isDebugEnabled()) {
                    System.out.println("添加日志索引: " + indexKey + " = " + indexValue);
                }
            }
            
        } catch (Exception e) {
            System.err.println("处理LogIndex字段异常: " + field.getName() + ", " + e.getMessage());
        }
    }
    
    /**
     * 判断是否跳过处理的类
     * 
     * <p>跳过基本类型、包装类型、字符串、集合等系统类，
     * 避免不必要的反射操作。</p>
     * 
     * @param clazz 类型
     * @return true表示跳过，false表示需要处理
     */
    private boolean isSkipClass(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz.getName().startsWith("java.") ||
               clazz.getName().startsWith("javax.") ||
               clazz.getName().startsWith("org.springframework.") ||
               clazz.getName().startsWith("com.sun.") ||
               clazz.isArray();
    }
    
    /**
     * 判断是否为嵌套对象
     * 
     * @param obj 对象
     * @return true表示是嵌套对象
     */
    private boolean isNestedObject(Object obj) {
        Class<?> clazz = obj.getClass();
        return !clazz.getName().startsWith("tech.msop.");
    }
    
    /**
     * 截断字符串到指定长度
     * 
     * @param str 原字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * 是否启用调试日志
     * 
     * @return true表示启用
     */
    private boolean isDebugEnabled() {
        return logProperty != null && 
               logProperty.getLogIndex() != null && 
               Boolean.TRUE.equals(logProperty.getLogIndex().getDebugEnabled());
    }
    
    /**
     * 是否在方法执行后清理MDC
     * 
     * @return true表示需要清理
     */
    private boolean isClearAfterMethodEnabled() {
        return logProperty == null || 
               logProperty.getLogIndex() == null || 
               !Boolean.FALSE.equals(logProperty.getLogIndex().getClearAfterMethod());
    }
    
    /**
     * 是否启用嵌套对象扫描
     * 
     * @return true表示启用
     */
    private boolean isNestedScanEnabled() {
        return logProperty != null && 
               logProperty.getLogIndex() != null && 
               Boolean.TRUE.equals(logProperty.getLogIndex().getEnableNestedScan());
    }
    
    /**
     * 获取索引键的最大长度
     * 
     * @return 最大长度
     */
    private int getMaxKeyLength() {
        if (logProperty != null && logProperty.getLogIndex() != null && 
            logProperty.getLogIndex().getMaxKeyLength() != null) {
            return logProperty.getLogIndex().getMaxKeyLength();
        }
        return 100;
    }
    
    /**
     * 获取索引值的最大长度
     * 
     * @return 最大长度
     */
    private int getMaxValueLength() {
        if (logProperty != null && logProperty.getLogIndex() != null && 
            logProperty.getLogIndex().getMaxValueLength() != null) {
            return logProperty.getLogIndex().getMaxValueLength();
        }
        return 500;
    }
}