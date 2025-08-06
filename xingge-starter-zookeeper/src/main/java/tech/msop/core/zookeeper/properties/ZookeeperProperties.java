package tech.msop.core.zookeeper.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zookeeper配置类
 *
 * @author ruozhuliufeng
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ms.zookeeper")
public class ZookeeperProperties {
    /**
     * ZK连接集群，多个用逗号隔开
     */
    private String connectString;
    /**
     * 会话超时时间(毫秒)
     */
    private int sessionTimeout = 15000;
    /**
     * 连接超时时间(毫秒)
     */
    private int connectionTimeout = 15000;
    /**
     * 初始重试等待时间(毫秒)
     */
    private int baseSleepTime = 2000;
    /**
     * 重试最大次数
     */
    private int maxRetries = 10;
}
