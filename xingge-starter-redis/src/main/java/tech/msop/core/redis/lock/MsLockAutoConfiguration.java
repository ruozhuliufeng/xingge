package tech.msop.core.redis.lock;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.msop.core.tool.lock.DistributedLock;
import tech.msop.core.tool.lock.LockAspect;
import tech.msop.core.tool.utils.StringUtil;

/**
 * 分布式锁自动化配置
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(MsLockProperties.class)
@ConditionalOnProperty(value = "ms.lock.enabled", havingValue = "true")
public class MsLockAutoConfiguration {

    private static Config singleConfig(MsLockProperties properties){
        Config config = new Config();
        SingleServerConfig serverConfig =config.useSingleServer();
        serverConfig.setAddress(properties.getAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)){
            serverConfig.setPassword(password);
        }
        serverConfig.setDatabase(properties.getDatabase());
        serverConfig.setConnectionPoolSize(properties.getPoolSize());
        serverConfig.setConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setIdleConnectionTimeout(properties.getConnectTimeout());
        serverConfig.setConnectTimeout(properties.getConnectTimeout());
        serverConfig.setTimeout(properties.getTimeout());
        return config;
    }
    private static Config masterConfig(MsLockProperties properties){
        Config config = new Config();
        MasterSlaveServersConfig serverConfig =config.useMasterSlaveServers();
        serverConfig.setMasterAddress(properties.getMasterAddress());
        serverConfig.addSlaveAddress(properties.getSlaveAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)){
            serverConfig.setPassword(password);
        }
        serverConfig.setDatabase(properties.getDatabase());
        serverConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setIdleConnectionTimeout(properties.getConnectTimeout());
        serverConfig.setConnectTimeout(properties.getConnectTimeout());
        serverConfig.setTimeout(properties.getTimeout());
        return config;
    }
    private static Config sentinelConfig(MsLockProperties properties){
        Config config = new Config();
        SentinelServersConfig serverConfig =config.useSentinelServers();
        serverConfig.setMasterName(properties.getMasterName());
        serverConfig.addSentinelAddress(properties.getSentinelAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)){
            serverConfig.setPassword(password);
        }
        serverConfig.setDatabase(properties.getDatabase());
        serverConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setIdleConnectionTimeout(properties.getConnectTimeout());
        serverConfig.setConnectTimeout(properties.getConnectTimeout());
        serverConfig.setTimeout(properties.getTimeout());
        return config;
    }
    private static Config clusterConfig(MsLockProperties properties){
        Config config = new Config();
        ClusterServersConfig serverConfig =config.useClusterServers();
        serverConfig.addNodeAddress(properties.getNodeAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)){
            serverConfig.setPassword(password);
        }
        serverConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serverConfig.setIdleConnectionTimeout(properties.getConnectTimeout());
        serverConfig.setConnectTimeout(properties.getConnectTimeout());
        serverConfig.setTimeout(properties.getTimeout());
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockAspect redisLockAspect(DistributedLock lock){
        return new LockAspect(lock);
    }



    public static RedissonClient redissonClient(MsLockProperties properties){
        MsLockProperties.Mode mode = properties.getMode();
        Config config;
        switch (mode){
            case sentinel:
                config = sentinelConfig(properties);
                break;
            case cluster:
                config = clusterConfig(properties);
                break;
            case master:
                config = masterConfig(properties);
                break;
            case single:
                config = singleConfig(properties);
                break;
            default:
                config = new Config();
                break;
        }
        return Redisson.create(config);
    }
}
