package tech.msop.xingge.interceptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 拦截数据模型
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterceptData {

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 拦截时间
     */
    private LocalDateTime interceptTime;

    /**
     * 拦截类型：request、response
     */
    private String interceptType;

    /**
     * 拦截范围：incoming、outgoing
     */
    private String interceptScope;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 查询参数
     */
    private String queryString;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求体
     */
    private String requestBody;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;

    /**
     * 响应体
     */
    private String responseBody;

    /**
     * 处理耗时（毫秒）
     */
    private Long duration;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 服务实例ID
     */
    private String instanceId;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}