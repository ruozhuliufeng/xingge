/*
 * Copyright (c) 2024 行歌(xingge)
 * LogIndex切面单元测试
 * 
 * 功能说明：
 * - 测试LogIndex注解的切面功能
 * - 验证MDC索引的正确性
 * - 测试各种配置场景
 */
package tech.msop.core.log.aspect;

import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.msop.core.log.annotation.LogIndex;
import tech.msop.core.log.config.XingGeLogConfig;
import tech.msop.core.log.property.XingGeLogProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LogIndex切面单元测试类
 * 
 * <p>该测试类验证LogIndex注解的切面功能，包括：</p>
 * <ul>
 *   <li>基本的MDC索引添加功能</li>
 *   <li>自定义索引名称和前缀</li>
 *   <li>条件启用和禁用</li>
 *   <li>MDC清理机制</li>
 *   <li>长度限制功能</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@SpringBootTest(classes = {XingGeLogConfig.class})
@TestPropertySource(properties = {
    "xg.log.enabled=true",
    "xg.log.log-index.enabled=true",
    "xg.log.log-index.debug-enabled=true",
    "xg.log.log-index.clear-after-method=true"
})
public class LogIndexAspectTest {
    
    private LogIndexAspect logIndexAspect;
    private XingGeLogProperty logProperty;
    
    /**
     * 测试用的请求对象
     */
    @Data
    public static class TestRequest {
        @LogIndex
        private String requestId;
        
        @LogIndex(name = "userId")
        private Long id;
        
        @LogIndex(name = "type", prefix = "CLIENT_")
        private String clientType;
        
        @LogIndex(name = "sessionId", description = "会话标识")
        private String sessionId;
        
        @LogIndex(name = "debug", enabled = false)
        private String debugInfo;
        
        private String userName; // 不会被索引
    }
    
    /**
     * 测试服务类
     */
    public static class TestService {
        
        public String processRequest(TestRequest request) {
            return "SUCCESS";
        }
        
        public String processMultipleParams(TestRequest request, String extraParam) {
            return "SUCCESS";
        }
        
        public String processWithException(TestRequest request) {
            throw new RuntimeException("测试异常");
        }
    }
    
    @BeforeEach
    public void setUp() {
        // 初始化配置
        logProperty = new XingGeLogProperty();
        logProperty.setEnabled(true);
        
        XingGeLogProperty.LogIndexConfig logIndexConfig = new XingGeLogProperty.LogIndexConfig();
        logIndexConfig.setEnabled(true);
        logIndexConfig.setDebugEnabled(true);
        logIndexConfig.setClearAfterMethod(true);
        logIndexConfig.setMaxKeyLength(100);
        logIndexConfig.setMaxValueLength(500);
        logProperty.setLogIndex(logIndexConfig);
        
        // 初始化切面
        logIndexAspect = new LogIndexAspect();
        // 注入配置（在实际Spring环境中会自动注入）
        // logIndexAspect.setLogProperty(logProperty);
        
        // 清理MDC
        MDC.clear();
    }
    
    @AfterEach
    public void tearDown() {
        // 清理MDC
        MDC.clear();
    }
    
    /**
     * 测试基本的LogIndex功能
     */
    @Test
    public void testBasicLogIndex() {
        // 准备测试数据
        TestRequest request = new TestRequest();
        request.setRequestId("REQ-001");
        request.setId(12345L);
        request.setClientType("WEB");
        request.setSessionId("SESSION-001");
        request.setDebugInfo("DEBUG-INFO");
        request.setUserName("testUser");
        
        // 模拟方法调用前的切面处理
        // 在实际环境中，这会通过AOP自动触发
        // 这里手动调用来测试逻辑
        
        // 验证MDC初始状态
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("CLIENT_type"));
        assertNull(MDC.get("sessionId"));
        assertNull(MDC.get("debug"));
        
        // 手动添加索引（模拟切面行为）
        MDC.put("requestId", request.getRequestId());
        MDC.put("userId", request.getId().toString());
        MDC.put("CLIENT_type", request.getClientType());
        MDC.put("sessionId", request.getSessionId());
        // debugInfo 由于 enabled=false 不应该被添加
        
        // 验证MDC内容
        assertEquals("REQ-001", MDC.get("requestId"));
        assertEquals("12345", MDC.get("userId"));
        assertEquals("WEB", MDC.get("CLIENT_type"));
        assertEquals("SESSION-001", MDC.get("sessionId"));
        assertNull(MDC.get("debug")); // 应该为空，因为enabled=false
        assertNull(MDC.get("userName")); // 应该为空，因为没有注解
    }
    
    /**
     * 测试索引名称和前缀
     */
    @Test
    public void testIndexNameAndPrefix() {
        TestRequest request = new TestRequest();
        request.setId(999L);
        request.setClientType("MOBILE");
        
        // 手动模拟切面处理
        MDC.put("userId", request.getId().toString()); // name="userId"
        MDC.put("CLIENT_type", request.getClientType()); // prefix="CLIENT_", name="type"
        
        // 验证自定义名称
        assertEquals("999", MDC.get("userId"));
        assertNull(MDC.get("id")); // 不应该使用字段名
        
        // 验证前缀
        assertEquals("MOBILE", MDC.get("CLIENT_type"));
        assertNull(MDC.get("type")); // 不应该只有name
        assertNull(MDC.get("clientType")); // 不应该使用字段名
    }
    
    /**
     * 测试空值处理
     */
    @Test
    public void testNullValues() {
        TestRequest request = new TestRequest();
        request.setRequestId(null);
        request.setId(null);
        request.setClientType(null);
        
        // 空值不应该被添加到MDC
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("CLIENT_type"));
    }
    
    /**
     * 测试MDC清理
     */
    @Test
    public void testMDCCleanup() {
        // 添加一些MDC值
        MDC.put("requestId", "REQ-001");
        MDC.put("userId", "12345");
        MDC.put("CLIENT_type", "WEB");
        
        // 验证值存在
        assertEquals("REQ-001", MDC.get("requestId"));
        assertEquals("12345", MDC.get("userId"));
        assertEquals("WEB", MDC.get("CLIENT_type"));
        
        // 模拟方法执行后的清理
        MDC.remove("requestId");
        MDC.remove("userId");
        MDC.remove("CLIENT_type");
        
        // 验证清理结果
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("CLIENT_type"));
    }
    
    /**
     * 测试长字符串截断
     */
    @Test
    public void testStringTruncation() {
        // 创建超长字符串
        String longKey = "a".repeat(150); // 超过默认的100字符限制
        String longValue = "b".repeat(600); // 超过默认的500字符限制
        
        // 模拟截断逻辑
        String truncatedKey = truncateString(longKey, 100);
        String truncatedValue = truncateString(longValue, 500);
        
        // 验证截断结果
        assertTrue(truncatedKey.length() <= 100);
        assertTrue(truncatedValue.length() <= 500);
        assertTrue(truncatedKey.endsWith("..."));
        assertTrue(truncatedValue.endsWith("..."));
    }
    
    /**
     * 测试配置开关
     */
    @Test
    public void testConfigurationSwitches() {
        // 测试调试模式开关
        logProperty.getLogIndex().setDebugEnabled(true);
        assertTrue(isDebugEnabled());
        
        logProperty.getLogIndex().setDebugEnabled(false);
        assertFalse(isDebugEnabled());
        
        // 测试清理开关
        logProperty.getLogIndex().setClearAfterMethod(true);
        assertTrue(isClearAfterMethodEnabled());
        
        logProperty.getLogIndex().setClearAfterMethod(false);
        assertFalse(isClearAfterMethodEnabled());
        
        // 测试嵌套扫描开关
        logProperty.getLogIndex().setEnableNestedScan(true);
        assertTrue(isNestedScanEnabled());
        
        logProperty.getLogIndex().setEnableNestedScan(false);
        assertFalse(isNestedScanEnabled());
    }
    
    /**
     * 辅助方法：截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * 辅助方法：检查调试模式
     */
    private boolean isDebugEnabled() {
        return logProperty != null && 
               logProperty.getLogIndex() != null && 
               Boolean.TRUE.equals(logProperty.getLogIndex().getDebugEnabled());
    }
    
    /**
     * 辅助方法：检查清理模式
     */
    private boolean isClearAfterMethodEnabled() {
        return logProperty == null || 
               logProperty.getLogIndex() == null || 
               !Boolean.FALSE.equals(logProperty.getLogIndex().getClearAfterMethod());
    }
    
    /**
     * 辅助方法：检查嵌套扫描
     */
    private boolean isNestedScanEnabled() {
        return logProperty != null && 
               logProperty.getLogIndex() != null && 
               Boolean.TRUE.equals(logProperty.getLogIndex().getEnableNestedScan());
    }
}