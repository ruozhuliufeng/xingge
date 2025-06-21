package tech.msop.core.tool.constant;

/**
 * 通用常量
 *
 * @author ruozhuliufeng
 */
public interface MsConstant {

    /**
     * 锁前缀
     */
    String LOCK_KEY_PREFIX = "LOCK_KEY";
    /**
     * 编码
     */
    String UTF_8 = "UTF-8";
    /**
     * contentType
     */
    String CONTENT_TYPE_NAME = "Content-type";

    /**
     * JSON 资源请求
     */
    String CONTENT_TYPE = "application/json;charset=utf-8";
    /**
     * 上下文键值
     */
    String CONTEXT_KEY = "msContext";

    /**
     * mdc request id key
     */
    String MDC_REQUEST_ID_KEY = "requestId";

    /**
     * mdc account id key
     */
    String MDC_ACCOUNT_ID_KEY = "accountId";

    /**
     * mdc tenant id key
     */
    String MDC_TENANT_ID_KEY = "tenantId";
    /**
     * 租户字段名
     */
    String DB_TENANT_KEY = "tenantId";
    /**
     * 租户字段get方法
     */
    String DB_TENANT_KEY_GET_METHOD = "getTenantId";

    /**
     * 租户字段set方法
     */
    String DB_TENANT_KEY_SET_METHOD = "setTenantId";

    /**
     * Security 角色前缀
     */
    String SECURITY_ROLE_PREFIX = "ROLE_";

    /**
     * 数据库主键字段名
     */
    String DB_PRIMARY_KEY = "id";

    /**
     * 业务状态[1：正常]
     */
    Integer STATUS_NORMAL = 1;

    /**
     * 删除状态 [0:正常 1:删除]
     */
    Integer DB_NOT_DELETED = 0;
    Integer DB_IS_DELETED = 1;

    /**
     * 用户锁定状态:[0:正常 1:锁定]
     */
    Integer DB_ADMIN_NON_LOCKED = 0;
    Integer DB_ADMIN_LOCKED = 1;

    /**
     * 顶级父节点ID
     */
    Long TOP_PARENT_ID = 0L;

    /**
     * 顶级父节点名称
     */
    String TOP_PARENT_NAME = "顶级";

    /**
     * 管理员对应的租户ID
     */
    String ADMIN_TENANT_ID = "000000";

    /**
     * 日志默认状态
     */
    String LOG_NORMAL_TYPE = "1";

    /**
     * 默认为空数据
     */
    String DEFAULT_NULL_MESSAGE = "暂无数据";

    /**
     * 默认成功消息
     */
    String DEFAULT_SUCCESS_MESSAGE = "操作成功";

    /**
     * 默认失败消息
     */
    String DEFAULT_FAILURE_MESSAGE = "操作失败";
    /**
     * 默认未授权消息
     */
    String DEFAULT_UNAUTHORIZED_MESSAGE = "签名认证失败";
    /**
     * 超级管理员用户名
     */
    String ADMIN_USER_NAME = "admin";

    /**
     * 租户信息头(应用)
     */
    String TENANT_HEADER = "x-tenant-header";
    /**
     * 用户ID信息头
     */
    String USER_ID_HEADER = "x-userid-header";

    /**
     * 用户信息头
     */
    String USER_HEADER = "x-user-header";
    /**
     * 认证请求头
     */
    String BASIC_HEADER_KEY = "Auhorization";
    /**
     * 类型
     */
    String BEARER_TYPE = "Bearer";
    /**
     * 主键字段你get方法
     */
    String DB_PRIMARY_KEY_METHOD = "getId";
}
