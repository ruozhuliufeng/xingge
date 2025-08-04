package tech.msop.core.example;

import org.springframework.stereotype.Service;
import tech.msop.core.annotation.AuditLog;

/**
 * 审计日志使用示例
 * 展示如何在业务代码中使用@AuditLog注解
 *
 * @author 星歌
 * @since 1.0.0
 */
@Service
public class AuditLogExample {

    /**
     * 基本使用示例
     * 记录用户登录操作
     */
    @AuditLog(
        operation = "USER_LOGIN",
        module = "用户管理",
        description = "用户登录系统"
    )
    public String userLogin(String username, String password) {
        System.out.println("用户 " + username + " 尝试登录");
        
        // 模拟登录逻辑
        if ("admin".equals(username) && "123456".equals(password)) {
            return "登录成功";
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 详细配置示例
     * 记录用户信息更新操作
     */
    @AuditLog(
        operation = "USER_UPDATE",
        module = "用户管理",
        description = "更新用户信息",
        includeArgs = true,
        includeResult = true,
        includeException = true,
        priority = 3,
        tags = {"用户管理", "信息更新"},
        async = false  // 同步处理，确保数据一致性
    )
    public UserInfo updateUserInfo(Long userId, UserInfo userInfo) {
        System.out.println("更新用户 " + userId + " 的信息");
        
        // 模拟更新逻辑
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或小于等于0");
        }
        
        userInfo.setId(userId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        
        return userInfo;
    }

    /**
     * 条件启用示例
     * 只在生产环境记录敏感操作
     */
    @AuditLog(
        operation = "PASSWORD_RESET",
        module = "安全管理",
        description = "重置用户密码",
        includeArgs = false,  // 不记录密码参数
        includeResult = false,
        priority = 4,
        tags = {"安全", "密码重置"}
    )
    public void resetPassword(Long userId, String newPassword) {
        System.out.println("重置用户 " + userId + " 的密码");
        
        // 模拟密码重置逻辑
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        
        // 执行密码重置
    }

    /**
     * 批量操作示例
     * 记录批量删除操作
     */
    @AuditLog(
        operation = "BATCH_DELETE",
        module = "数据管理",
        description = "批量删除数据",
        includeArgs = true,
        includeResult = true,
        priority = 3,
        tags = {"批量操作", "数据删除"}
    )
    public int batchDeleteUsers(Long[] userIds) {
        System.out.println("批量删除用户，数量: " + userIds.length);
        
        // 模拟批量删除逻辑
        if (userIds == null || userIds.length == 0) {
            return 0;
        }
        
        // 执行删除操作
        int deletedCount = userIds.length;
        
        return deletedCount;
    }

    /**
     * 异步处理示例
     * 记录文件上传操作（异步处理以提高性能）
     */
    @AuditLog(
        operation = "FILE_UPLOAD",
        module = "文件管理",
        description = "上传文件",
        includeArgs = true,
        includeResult = true,
        async = true,  // 异步处理
        priority = 2,
        tags = {"文件操作", "上传"}
    )
    public String uploadFile(String fileName, byte[] fileContent) {
        System.out.println("上传文件: " + fileName + ", 大小: " + fileContent.length + " bytes");
        
        // 模拟文件上传逻辑
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        
        // 生成文件路径
        String filePath = "/uploads/" + System.currentTimeMillis() + "_" + fileName;
        
        return filePath;
    }

    /**
     * 用户信息模型
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