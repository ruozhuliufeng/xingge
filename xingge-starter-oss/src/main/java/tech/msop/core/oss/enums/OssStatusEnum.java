package tech.msop.core.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OSS 状态枚举
 *
 * @author ruozhuliufeng
 */
@Getter
@AllArgsConstructor
public enum OssStatusEnum {

    /**
     * 关闭
     */
    DISABLE(1),

    /**
     * 启用
     */
    ENABLE(2),
    ;

    final Integer num;

}
