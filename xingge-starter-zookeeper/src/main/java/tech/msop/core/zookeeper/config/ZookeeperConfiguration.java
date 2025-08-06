package tech.msop.core.zookeeper.config;

import lombok.AllArgsConstructor;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.msop.core.zookeeper.properties.ZookeeperProperties;
import tech.msop.core.zookeeper.template.ZookeeperTemplate;

/**
 * Zookeeper 配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(ZookeeperProperties.class)
@AllArgsConstructor
public class ZookeeperConfiguration {
    private final ZookeeperProperties properties;

    /**
     * 初始化Zookeeper连接
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(properties.getBaseSleepTime(), properties.getMaxRetries());
        return CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .connectionTimeoutMs(properties.getConnectionTimeout())
                .sessionTimeoutMs(properties.getSessionTimeout())
                .retryPolicy(retryPolicy)
                .build();
    }

    /**
     * Zookeeper 简化操作工具类
     */
    @Bean
    public ZookeeperTemplate zookeeperTemplate(CuratorFramework client) {
        return new ZookeeperTemplate(client);
    }
}
