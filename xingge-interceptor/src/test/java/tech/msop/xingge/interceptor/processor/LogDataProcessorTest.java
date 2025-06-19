package tech.msop.xingge.interceptor.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.msop.xingge.interceptor.InterceptData;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志数据处理器测试
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class LogDataProcessorTest {

    private LogDataProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new LogDataProcessor();
    }

    @Test
    void testGetType() {
        assertEquals("log", processor.getType());
    }

    @Test
    void testSupportsAsync() {
        assertTrue(processor.supportsAsync());
    }

    @Test
    void testGetPriority() {
        assertEquals(100, processor.getPriority());
    }

    @Test
    void testProcessData() {
        // 准备测试数据
        InterceptData data = createTestData();
        Map<String, Object> config = createTestConfig();

        // 执行测试
        assertDoesNotThrow(() -> processor.processData(data, config));
    }

    @Test
    void testValidateConfig() {
        // 测试有效配置
        Map<String, Object> validConfig = createTestConfig();
        assertTrue(processor.validateConfig(validConfig));

        // 测试无效配置
        Map<String, Object> invalidConfig = new HashMap<>();
        invalidConfig.put("level", "INVALID_LEVEL");
        assertFalse(processor.validateConfig(invalidConfig));
    }

    @Test
    void testInitialize() {
        Map<String, Object> config = createTestConfig();
        assertDoesNotThrow(() -> processor.initialize(config));
    }

    @Test
    void testDestroy() {
        assertDoesNotThrow(() -> processor.destroy());
    }

    private InterceptData createTestData() {
        InterceptData data = new InterceptData();
        data.setId(UUID.randomUUID().toString().replace("-", ""));
        data.setInterceptType("request");
        data.setInterceptScope("external");
        data.setMethod("GET");
        data.setUrl("http://localhost:8080/api/test");
        data.setPath("/api/test");
        data.setResponseStatus(200);
        data.setDuration(100L);
        data.setTimestamp(new Date());
        data.setClientIp("127.0.0.1");
        data.setUserId("user123");
        data.setApplicationName("test-app");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "Test-Agent");
        data.setHeaders(headers);
        
        data.setRequestBody("{\"test\": \"data\"}");
        data.setResponseBody("{\"result\": \"success\"}");
        
        return data;
    }

    private Map<String, Object> createTestConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("level", "INFO");
        config.put("includeHeaders", true);
        config.put("includeRequestBody", true);
        config.put("includeResponseBody", true);
        
        Map<String, String> mdcKeys = new HashMap<>();
        mdcKeys.put("traceId", "${id}");
        mdcKeys.put("userId", "${userId}");
        config.put("mdcKeys", mdcKeys);
        
        return config;
    }
}