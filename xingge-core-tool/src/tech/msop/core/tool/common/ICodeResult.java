package tech.msop.core.tool.common;

/**
 * 异常响应码
 *
 * @author ruozhuliufeng
 */
public interface ICodeResult {
    /**
     * 获取响应码
     *
     * @return 响应码
     */
    Integer getCode();

    /**
     * 获取响应信息
     *
     * @return 响应信息
     */
    String getMessage();
}
