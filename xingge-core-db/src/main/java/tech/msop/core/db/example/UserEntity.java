/**
 * 用户实体类示例
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.example;

import tech.msop.core.db.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户实体类示例
 * 演示如何使用表结构自动维护注解
 */
@Table(name = "sys_user", comment = "系统用户表", autoMaintain = true)
@Indexes({
    @Index(name = "idx_username", columnList = {"username"}, unique = true, comment = "用户名唯一索引"),
    @Index(name = "idx_email", columnList = {"email"}, unique = true, comment = "邮箱唯一索引"),
    @Index(name = "idx_create_time", columnList = {"create_time"}, comment = "创建时间索引")
})
public class UserEntity {
    
    /**
     * 主键ID
     */
    @Id(strategy = Id.GenerationType.AUTO)
    @Column(name = "id", comment = "主键ID", nullable = false, autoIncrement = true)
    private Long id;
    
    /**
     * 用户名
     */
    @Column(name = "username", comment = "用户名", length = 50, nullable = false, unique = true)
    private String username;
    
    /**
     * 密码
     */
    @Column(name = "password", comment = "密码", length = 100, nullable = false)
    private String password;
    
    /**
     * 邮箱
     */
    @Column(name = "email", comment = "邮箱地址", length = 100, nullable = false, unique = true)
    private String email;
    
    /**
     * 真实姓名
     */
    @Column(name = "real_name", comment = "真实姓名", length = 50)
    private String realName;
    
    /**
     * 手机号
     */
    @Column(name = "phone", comment = "手机号", length = 20)
    private String phone;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    @Column(name = "status", comment = "状态(0-禁用,1-启用)", nullable = false, defaultValue = "1")
    private Integer status;
    
    /**
     * 年龄
     */
    @Column(name = "age", comment = "年龄")
    private Integer age;
    
    /**
     * 余额
     */
    @Column(name = "balance", comment = "账户余额", precision = 10, scale = 2, defaultValue = "0.00")
    private java.math.BigDecimal balance;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @Column(name = "is_deleted", comment = "是否删除(0-未删除,1-已删除)", nullable = false, defaultValue = "0")
    private Boolean isDeleted;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", comment = "创建时间", nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time", comment = "更新时间")
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @Column(name = "remark", comment = "备注", length = 500)
    private String remark;
    
    /**
     * 头像（使用自定义列定义）
     */
    @Column(name = "avatar", comment = "头像", columnDefinition = "TEXT")
    private String avatar;
    
    // Getter和Setter方法
    
    /**
     * 获取主键ID
     * 
     * @return 主键ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * 设置主键ID
     * 
     * @param id 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 设置用户名
     * 
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * 获取密码
     * 
     * @return 密码
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * 设置密码
     * 
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 获取邮箱
     * 
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * 设置邮箱
     * 
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * 获取真实姓名
     * 
     * @return 真实姓名
     */
    public String getRealName() {
        return realName;
    }
    
    /**
     * 设置真实姓名
     * 
     * @param realName 真实姓名
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    /**
     * 获取手机号
     * 
     * @return 手机号
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * 设置手机号
     * 
     * @param phone 手机号
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * 获取状态
     * 
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }
    
    /**
     * 设置状态
     * 
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    /**
     * 获取年龄
     * 
     * @return 年龄
     */
    public Integer getAge() {
        return age;
    }
    
    /**
     * 设置年龄
     * 
     * @param age 年龄
     */
    public void setAge(Integer age) {
        this.age = age;
    }
    
    /**
     * 获取余额
     * 
     * @return 余额
     */
    public java.math.BigDecimal getBalance() {
        return balance;
    }
    
    /**
     * 设置余额
     * 
     * @param balance 余额
     */
    public void setBalance(java.math.BigDecimal balance) {
        this.balance = balance;
    }
    
    /**
     * 获取是否删除
     * 
     * @return 是否删除
     */
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    /**
     * 设置是否删除
     * 
     * @param isDeleted 是否删除
     */
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    /**
     * 设置创建时间
     * 
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    /**
     * 获取更新时间
     * 
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    /**
     * 设置更新时间
     * 
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    /**
     * 获取备注
     * 
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }
    
    /**
     * 设置备注
     * 
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    /**
     * 获取头像
     * 
     * @return 头像
     */
    public String getAvatar() {
        return avatar;
    }
    
    /**
     * 设置头像
     * 
     * @param avatar 头像
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}