package tech.msop.xingge.interceptor;

import java.util.Map;

/**
 * 数据处理器接口
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
public interface DataProcessor {

    /**
     * 获取处理器类型
     * 
     * @return 处理器类型
     */
    String getType();

    /**
     * 处理拦截数据
     * 
     * @param data 拦截数据
     * @param config 处理器配置
     */
    void process(InterceptData data, Map<String, Object> config);

    /**
     * 是否支持异步处理
     * 
     * @return true-支持异步，false-仅支持同步
     */
    default boolean supportsAsync() {
        return true;
    }

    /**
     * 获取处理器优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 初始化处理器
     * 
     * @param config 全局配置
     */
    default void initialize(Map<String, Object> config) {
        // 默认空实现
    }

    /**
     * 销毁处理器
     */
    default void destroy() {
        // 默认空实现
    }

    /**
     * 验证配置
     * 
     * @param config 处理器配置
     * @return true-配置有效，false-配置无效
     */
    default boolean validateConfig(Map<String, Object> config) {
        return true;
    }
}