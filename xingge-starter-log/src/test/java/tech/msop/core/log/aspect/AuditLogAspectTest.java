package tech.msop.core.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import tech.msop.core.log.annotation.AuditLog;
import tech.msop.core.log.handler.AuditLogHandler;
import tech.msop.core.log.model.AuditLogInfo;
import tech.msop.core.log.property.XingGeLogProperty;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 审计日志切面测试类
 * 测试AuditLogAspect的各种功能
 *
 * @author 星歌
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditLogAspectTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AuditLogHandler mockHandler1;

    @Mock
    private AuditLogHandler mockHandler2;

    private AuditLogAspect auditLogAspect;
    private XingGeLogProperty logProperty;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 初始化配置
        logProperty = new XingGeLogProperty();
        logProperty.getAudit().setEnabled(true);
        logProperty.getAudit().setDebugEnabled(true);
        logProperty.getAudit().setDefaultAsync(false);

        objectMapper = new ObjectMapper();
        auditLogAspect = new AuditLogAspect(logProperty);
        auditLogAspect.setApplicationContext(applicationContext);

        // 配置Mock处理器
        when(mockHandler1.getName()).thenReturn("handler1");
        when(mockHandler1.getPriority()).thenReturn(1);
        when(mockHandler1.isEnabled()).thenReturn(true);
        when(mockHandler1.supports(any())).thenReturn(true);

        when(mockHandler2.getName()).thenReturn("handler2");
        when(mockHandler2.getPriority()).thenReturn(2);
        when(mockHandler2.isEnabled()).thenReturn(true);
        when(mockHandler2.supports(any())).thenReturn(true);

        Map<String, AuditLogHandler> handlers = Map.of(
            "handler1", mockHandler1,
            "handler2", mockHandler2
        );
        when(applicationContext.getBeansOfType(AuditLogHandler.class)).thenReturn(handlers);
    }

    /**
     * 测试基本审计日志记录
     */
    @Test
    void testBasicAuditLog() throws Throwable {
        // 准备测试方法
        Method method = TestService.class.getMethod("basicOperation", String.class);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        Object[] args = {"test-param"};
        Object result = "test-result";

        // 执行审计日志记录
        auditLogAspect.recordAuditLog(method, args, result, null, 100L, auditLog);

        // 验证处理器被调用
        ArgumentCaptor<AuditLogInfo> captor = ArgumentCaptor.forClass(AuditLogInfo.class);
        verify(mockHandler1).handle(captor.capture());
        verify(mockHandler2).handle(captor.capture());

        // 验证审计日志信息
        List<AuditLogInfo> capturedLogs = captor.getAllValues();
        assertEquals(2, capturedLogs.size());

        AuditLogInfo logInfo = capturedLogs.get(0);
        assertEquals("TEST_OPERATION", logInfo.getOperation());
        assertEquals("测试模块", logInfo.getModule());
        assertEquals("基本操作测试", logInfo.getDescription());
        assertEquals("basicOperation", logInfo.getMethodName());
        assertEquals(AuditLogInfo.Status.SUCCESS, logInfo.getStatus());
        assertEquals(100L, logInfo.getExecutionTime());
        assertNotNull(logInfo.getLogId());
        assertNotNull(logInfo.getOperationTime());
    }

    /**
     * 测试记录方法参数
     */
    @Test
    void testRecordMethodArgs() throws Throwable {
        Method method = TestService.class.getMethod("operationWithArgs", String.class, Integer.class);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        Object[] args = {"param1", 123};

        auditLogAspect.recordAuditLog(method, args, "result", null, 50L, auditLog);

        ArgumentCaptor<AuditLogInfo> captor = ArgumentCaptor.forClass(AuditLogInfo.class);
        verify(mockHandler1).handle(captor.capture());

        AuditLogInfo logInfo = captor.getValue();
        assertNotNull(logInfo.getMethodArgs());
        assertTrue(logInfo.getMethodArgs().contains("param1"));
        assertTrue(logInfo.getMethodArgs().contains("123"));
    }

    /**
     * 测试记录方法返回值
     */
    @Test
    void testRecordMethodResult() throws Throwable {
        Method method = TestService.class.getMethod("operationWithResult");
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        Object result = new TestResult("success", 200);

        auditLogAspect.recordAuditLog(method, new Object[0], result, null, 30L, auditLog);

        ArgumentCaptor<AuditLogInfo> captor = ArgumentCaptor.forClass(AuditLogInfo.class);
        verify(mockHandler1).handle(captor.capture());

        AuditLogInfo logInfo = captor.getValue();
        assertNotNull(logInfo.getMethodResult());
        assertTrue(logInfo.getMethodResult().contains("success"));
        assertTrue(logInfo.getMethodResult().contains("200"));
    }

    /**
     * 测试记录异常信息
     */
    @Test
    void testRecordException() throws Throwable {
        Method method = TestService.class.getMethod("operationWithException");
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        Exception exception = new RuntimeException("测试异常");

        auditLogAspect.recordAuditLog(method, new Object[0], null, exception, 20L, auditLog);

        ArgumentCaptor<AuditLogInfo> captor = ArgumentCaptor.forClass(AuditLogInfo.class);
        verify(mockHandler1).handle(captor.capture());

        AuditLogInfo logInfo = captor.getValue();
        assertEquals(AuditLogInfo.Status.FAILURE, logInfo.getStatus());
        assertNotNull(logInfo.getExceptionInfo());
        assertTrue(logInfo.getExceptionInfo().contains("测试异常"));
        assertTrue(logInfo.getExceptionInfo().contains("RuntimeException"));
    }

    /**
     * 测试异步处理
     */
    @Test
    void testAsyncProcessing() throws Throwable {
        // 启用异步处理
        logProperty.getAudit().setDefaultAsync(true);

        Method method = TestService.class.getMethod("asyncOperation");
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        auditLogAspect.recordAuditLog(method, new Object[0], "result", null, 10L, auditLog);

        // 异步处理需要等待一段时间
        Thread.sleep(100);

        ArgumentCaptor<AuditLogInfo> captor = ArgumentCaptor.forClass(AuditLogInfo.class);
        verify(mockHandler1).handleAsync(captor.capture());
        verify(mockHandler2).handleAsync(captor.capture());
    }

    /**
     * 测试条件启用
     */
    @Test
    void testConditionalEnabled() throws Throwable {
        // 禁用审计日志
        logProperty.getAudit().setEnabled(false);

        Method method = TestService.class.getMethod("basicOperation", String.class);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        auditLogAspect.recordAuditLog(method, new Object[]{"test"}, "result", null, 10L, auditLog);

        // 验证处理器未被调用
        verify(mockHandler1, never()).handle(any());
        verify(mockHandler2, never()).handle(any());
    }

    /**
     * 测试处理器优先级排序
     */
    @Test
    void testHandlerPriorityOrdering() throws Throwable {
        // 设置不同的优先级
        when(mockHandler1.getPriority()).thenReturn(2);
        when(mockHandler2.getPriority()).thenReturn(1);

        Method method = TestService.class.getMethod("basicOperation", String.class);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        auditLogAspect.recordAuditLog(method, new Object[]{"test"}, "result", null, 10L, auditLog);

        // 验证按优先级顺序调用（优先级低的先调用）
        verify(mockHandler2).handle(any());
        verify(mockHandler1).handle(any());
    }

    /**
     * 测试服务类
     */
    public static class TestService {

        @AuditLog(
            operation = "TEST_OPERATION",
            module = "测试模块",
            description = "基本操作测试"
        )
        public String basicOperation(String param) {
            return "result";
        }

        @AuditLog(
            operation = "ARGS_OPERATION",
            module = "测试模块",
            description = "参数记录测试",
            recordArgs = true
        )
        public String operationWithArgs(String param1, Integer param2) {
            return "result";
        }

        @AuditLog(
            operation = "RESULT_OPERATION",
            module = "测试模块",
            description = "返回值记录测试",
            recordResult = true
        )
        public TestResult operationWithResult() {
            return new TestResult("success", 200);
        }

        @AuditLog(
            operation = "EXCEPTION_OPERATION",
            module = "测试模块",
            description = "异常记录测试",
            recordException = true
        )
        public void operationWithException() {
            throw new RuntimeException("测试异常");
        }

        @AuditLog(
            operation = "ASYNC_OPERATION",
            module = "测试模块",
            description = "异步处理测试",
            async = true
        )
        public String asyncOperation() {
            return "async-result";
        }
    }

    /**
     * 测试结果类
     */
    public static class TestResult {
        private String message;
        private Integer code;

        public TestResult(String message, Integer code) {
            this.message = message;
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public Integer getCode() {
            return code;
        }
    }
}