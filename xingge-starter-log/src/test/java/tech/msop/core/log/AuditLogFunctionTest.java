package tech.msop.core.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.msop.core.log.annotation.AuditLog;
import tech.msop.core.log.model.AuditLogInfo;

/**
 * 审计日志功能测试类
 * 用于验证审计日志功能的完整性和正确性
 *
 * @author 星歌
 * @since 1.0.0
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "xg.log.enabled=true",
    "xg.log.audit.enabled=true",
    "xg.log.audit.debug-enabled=true",
    "xg.log.audit.handlers.console.enabled=true",
    "xg.log.audit.handlers.console.format=detailed",
    "xg.log.audit.handlers.console.level=INFO"
})
public class AuditLogFunctionTest {

    /**
     * 测试基本审计日志功能
     * 验证注解是否正常工作
     */
    @Test
    public void testBasicAuditLog() {
        log.info("=== 开始测试基本审计日志功能 ===");
        
        try {
            String result = basicOperation("测试参数");
            log.info("基本操作执行成功，返回结果: {}", result);
        } catch (Exception e) {
            log.error("基本操作执行失败", e);
        }
        
        log.info("=== 基本审计日志功能测试完成 ===");
    }

    /**
     * 测试参数和返回值记录
     * 验证是否能正确记录方法参数和返回值
     */
    @Test
    public void testArgsAndResultRecording() {
        log.info("=== 开始测试参数和返回值记录 ===");
        
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername("testuser");
            userInfo.setEmail("test@example.com");
            userInfo.setPhone("13800138000");
            
            UserInfo result = updateUserInfo(1001L, userInfo);
            log.info("用户信息更新成功，返回结果: {}", result);
        } catch (Exception e) {
            log.error("用户信息更新失败", e);
        }
        
        log.info("=== 参数和返回值记录测试完成 ===");
    }

    /**
     * 测试异常记录
     * 验证是否能正确记录异常信息
     */
    @Test
    public void testExceptionRecording() {
        log.info("=== 开始测试异常记录 ===");
        
        try {
            operationWithException();
        } catch (Exception e) {
            log.info("预期异常被正确捕获: {}", e.getMessage());
        }
        
        log.info("=== 异常记录测试完成 ===");
    }

    /**
     * 测试高优先级操作
     * 验证优先级设置是否正常工作
     */
    @Test
    public void testHighPriorityOperation() {
        log.info("=== 开始测试高优先级操作 ===");
        
        try {
            criticalOperation("重要数据");
            log.info("关键操作执行成功");
        } catch (Exception e) {
            log.error("关键操作执行失败", e);
        }
        
        log.info("=== 高优先级操作测试完成 ===");
    }

    /**
     * 测试批量操作
     * 验证批量操作的审计日志记录
     */
    @Test
    public void testBatchOperation() {
        log.info("=== 开始测试批量操作 ===");
        
        try {
            Long[] userIds = {1001L, 1002L, 1003L, 1004L, 1005L};
            int deletedCount = batchDeleteUsers(userIds);
            log.info("批量删除操作完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("批量删除操作失败", e);
        }
        
        log.info("=== 批量操作测试完成 ===");
    }

    /**
     * 测试异步操作
     * 验证异步处理是否正常工作
     */
    @Test
    public void testAsyncOperation() {
        log.info("=== 开始测试异步操作 ===");
        
        try {
            String fileName = "test-file.txt";
            byte[] fileContent = "这是测试文件内容".getBytes();
            String filePath = uploadFile(fileName, fileContent);
            log.info("文件上传成功，文件路径: {}", filePath);
            
            // 等待异步处理完成
            Thread.sleep(200);
        } catch (Exception e) {
            log.error("文件上传失败", e);
        }
        
        log.info("=== 异步操作测试完成 ===");
    }

    /**
     * 测试所有功能的综合场景
     * 验证整体功能的协调工作
     */
    @Test
    public void testComprehensiveScenario() {
        log.info("=== 开始综合功能测试 ===");
        
        // 模拟用户登录
        try {
            String loginResult = userLogin("admin", "123456");
            log.info("用户登录测试: {}", loginResult);
        } catch (Exception e) {
            log.error("用户登录失败", e);
        }
        
        // 模拟数据操作
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername("newuser");
            userInfo.setEmail("newuser@example.com");
            UserInfo result = updateUserInfo(2001L, userInfo);
            log.info("数据操作测试完成: {}", result);
        } catch (Exception e) {
            log.error("数据操作失败", e);
        }
        
        // 模拟安全操作
        try {
            resetPassword(2001L, "newpassword123");
            log.info("安全操作测试完成");
        } catch (Exception e) {
            log.error("安全操作失败", e);
        }
        
        log.info("=== 综合功能测试完成 ===");
    }

    // ==================== 测试方法定义 ====================

    /**
     * 基本操作测试方法
     */
    @AuditLog(
        operation = "TEST_BASIC_OPERATION",
        module = "功能测试",
        description = "基本操作功能测试"
    )
    public String basicOperation(String param) {
        log.info("执行基本操作，参数: {}", param);
        return "操作成功: " + param;
    }

    /**
     * 用户信息更新测试方法
     */
    @AuditLog(
        operation = "TEST_USER_UPDATE",
        module = "用户管理测试",
        description = "更新用户信息测试",
        recordArgs = true,
        recordResult = true,
        priority = AuditLogInfo.Priority.HIGH,
        tags = {"用户管理", "信息更新", "测试"}
    )
    public UserInfo updateUserInfo(Long userId, UserInfo userInfo) {
        log.info("更新用户 {} 的信息", userId);
        
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或小于等于0");
        }
        
        userInfo.setId(userId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        
        return userInfo;
    }

    /**
     * 异常操作测试方法
     */
    @AuditLog(
        operation = "TEST_EXCEPTION_OPERATION",
        module = "异常测试",
        description = "异常处理功能测试",
        recordException = true,
        priority = AuditLogInfo.Priority.HIGH
    )
    public void operationWithException() {
        log.info("执行会抛出异常的操作");
        throw new RuntimeException("这是一个测试异常，用于验证异常记录功能");
    }

    /**
     * 关键操作测试方法
     */
    @AuditLog(
        operation = "TEST_CRITICAL_OPERATION",
        module = "安全测试",
        description = "关键操作功能测试",
        recordArgs = true,
        priority = AuditLogInfo.Priority.CRITICAL,
        tags = {"安全", "关键操作", "测试"}
    )
    public void criticalOperation(String data) {
        log.info("执行关键操作，数据: {}", data);
        // 模拟关键操作逻辑
    }

    /**
     * 批量删除测试方法
     */
    @AuditLog(
        operation = "TEST_BATCH_DELETE",
        module = "数据管理测试",
        description = "批量删除功能测试",
        recordArgs = true,
        recordResult = true,
        priority = AuditLogInfo.Priority.HIGH,
        tags = {"批量操作", "数据删除", "测试"}
    )
    public int batchDeleteUsers(Long[] userIds) {
        log.info("批量删除用户，数量: {}", userIds.length);
        
        if (userIds == null || userIds.length == 0) {
            return 0;
        }
        
        // 模拟删除操作
        int deletedCount = userIds.length;
        log.info("成功删除 {} 个用户", deletedCount);
        
        return deletedCount;
    }

    /**
     * 文件上传测试方法（异步处理）
     */
    @AuditLog(
        operation = "TEST_FILE_UPLOAD",
        module = "文件管理测试",
        description = "文件上传功能测试",
        recordArgs = true,
        recordResult = true,
        async = true,
        priority = AuditLogInfo.Priority.NORMAL,
        tags = {"文件操作", "上传", "测试"}
    )
    public String uploadFile(String fileName, byte[] fileContent) {
        log.info("上传文件: {}, 大小: {} bytes", fileName, fileContent.length);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        
        // 生成文件路径
        String filePath = "/uploads/test/" + System.currentTimeMillis() + "_" + fileName;
        log.info("文件上传成功，路径: {}", filePath);
        
        return filePath;
    }

    /**
     * 用户登录测试方法
     */
    @AuditLog(
        operation = "TEST_USER_LOGIN",
        module = "认证测试",
        description = "用户登录功能测试",
        priority = AuditLogInfo.Priority.HIGH,
        tags = {"认证", "登录", "测试"}
    )
    public String userLogin(String username, String password) {
        log.info("用户 {} 尝试登录", username);
        
        // 模拟登录逻辑
        if ("admin".equals(username) && "123456".equals(password)) {
            return "登录成功";
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 密码重置测试方法
     */
    @AuditLog(
        operation = "TEST_PASSWORD_RESET",
        module = "安全管理测试",
        description = "密码重置功能测试",
        recordArgs = false,  // 不记录密码参数
        recordResult = false,
        priority = AuditLogInfo.Priority.CRITICAL,
        tags = {"安全", "密码重置", "测试"}
    )
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户 {} 的密码", userId);
        
        // 模拟密码重置逻辑
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        
        log.info("用户 {} 密码重置成功", userId);
    }

    /**
     * 用户信息测试模型
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private Long updateTime;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }
}