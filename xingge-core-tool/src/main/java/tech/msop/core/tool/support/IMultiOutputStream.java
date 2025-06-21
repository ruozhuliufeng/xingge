package tech.msop.core.tool.support;

import java.io.OutputStream;

/**
 * 创建文件输出流
 *
 * @author ruozhuliufeng
 */
public interface IMultiOutputStream {

    /**
     * 创建输出流
     * @param params 参数
     * @return 输出流
     */
    OutputStream buildOutputStream(Integer... params);
}
