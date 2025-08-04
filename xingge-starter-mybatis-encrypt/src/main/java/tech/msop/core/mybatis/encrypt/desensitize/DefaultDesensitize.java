package tech.msop.core.mybatis.encrypt.desensitize;

import lombok.extern.slf4j.Slf4j;
import tech.msop.core.mybatis.encrypt.utils.DesensitizeUtil;

/**
 * 默认脱敏处理器
 *
 * @author ruozhuliufeng
 */
@Slf4j
public class DefaultDesensitize implements IDesensitize {
    /**
     * 执行脱敏处理
     *
     * @param value     要脱敏的值
     * @param fillValue 填充的符号
     * @return 脱敏后数据
     */
    @Override
    public String execute(String value, String fillValue) {
        if (value == null || value.length() == 0
                || fillValue == null || fillValue.length() == 0) {
            return "";
        }
        String sensitiveInfo = DesensitizeUtil.encryptSensitiveInfo(value, fillValue);
        log.debug("脱敏前的值:{}", value);
        log.debug("脱敏后的值:{}", sensitiveInfo);
        return sensitiveInfo;
    }
}
