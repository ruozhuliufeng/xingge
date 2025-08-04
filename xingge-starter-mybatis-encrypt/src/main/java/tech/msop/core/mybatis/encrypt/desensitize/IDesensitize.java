package tech.msop.core.mybatis.encrypt.desensitize;

/**
 * 数据脱敏处理
 *
 * @author ruozhuliufeng
 */
public interface IDesensitize {
    /**
     * 执行脱敏处理
     *
     * @param value     要脱敏的值
     * @param fillValue 填充的符号
     * @return 脱敏后数据
     */
    String execute(String value, String fillValue);
}
