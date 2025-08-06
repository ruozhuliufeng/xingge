package tech.msop.core.redis.lock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置
 *
 * @author ruozhuliufeng
 */
@Getter
@Setter
@ConfigurationProperties(MsLockProperties.PREFIX)
public class MsLockProperties {
    public static final String PREFIX = "ms.lock";
    /**
     * 是否开启，默认为false
     */
    private Boolean enabled = Boolean.FALSE;
    /**
     * 分布式锁类型：REDIS
     */
    private String lockerType = "REDIS";
    /**
     * 单机配置：redis服务地址
     */
    private String address = "redis://127.0.0.1:6379";
    /**
     * 密码配置
     */
    private String password;
    /**
     * 数据库配置
     */
    private Integer database = 0;
    /**
     * 连接池大小
     */
    private Integer poolSize = 20;
    /**
     * 最小空闲连接数
     */
    private Integer idleSize = 5;
    /**
     * 连接空闲超时，单位：毫秒
     */
    private Integer idleTimeout = 60000;
    /**
     * 连接超时，单位：毫秒
     */
    private Integer connectTimeout = 3000;
    /**
     * 命令等待超时，单位：毫秒
     */
    private Integer timeout = 10000;
    /**
     * 集群模式，单机：single，主从：master，哨兵：sentinel，集群：cluster
     */
    private Mode mode = Mode.single;
    /**
     * 主从模式：主地址
     */
    private String masterAddress;
    /**
     * 主从模式：从地址
     */
    private String[] slaveAddress;
    /**
     * 哨兵模式：主名称
     */
    private String masterName;
    /**
     * 哨兵模式地址
     */
    private String[] sentinelAddress;
    /**
     * 集群模式节点地址
     */
    private String[] nodeAddress;

    public enum Mode {
        /**
         * 模式:<br>
         * 单机：single<br>
         * 主从：master<br>
         * 哨兵：sentinel<br>
         * 集群：cluster<br>
         */
        single,
        master,
        sentinel,
        cluster
    }
}
