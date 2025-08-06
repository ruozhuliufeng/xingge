package tech.msop.core.mybatis.encrypt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密相关配置
 *
 * @author ruozhuliufeng
 */
@Data
@ConfigurationProperties("xg.mybatis.crypto")
public class CryptoProperties {
    /**
     * 密钥
     */
    private String key;
}
