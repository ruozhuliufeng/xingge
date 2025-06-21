package tech.msop.core.tool.jackson;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jackson 配置
 */
@Getter
@Setter
@ConfigurationProperties("ms.jackson")
public class MsJacksonProperties {
    /**
     * null 转为 空,字符串转为 "",数组转为[]，对象转为{} 数字转为-1
     */
    private Boolean nullToEmpty = Boolean.TRUE;

    /**
     * 响应到前端，大数值自动写出为String，避免经度丢失
     */
    private Boolean bigNumToString = Boolean.TRUE;
    /**
     * 支持MediatType text/plain 用于和加密一起使用
     */
    private Boolean supportTextPlain = Boolean.FALSE;
}
