package tech.msop.core.log.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 行歌-日志配置参数类
 */
@ConfigurationProperties(prefix = "xg.log")
@Data
public class XingGeLogProperty {
    /**
     * 是否启用
     */
    private Boolean enabled;
}
