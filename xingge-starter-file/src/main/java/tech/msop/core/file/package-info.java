/**
 * XingGe文件服务工具模块
 * <p>
 * 提供完整的文件操作支持，包括：
 * <ul>
 *     <li>基础文件操作 - 创建、读取、写入、删除、复制、移动等</li>
 *     <li>文件夹操作 - 创建、列表、删除、复制、移动等</li>
 *     <li>压缩文件操作 - 支持ZIP、RAR、TAR等多种格式，支持密码保护，支持指定字符集（通过字符串名称）</li>
 *     <li>Excel文件操作 - 读取、写入、模板填充、添加图片盖章等</li>
 *     <li>PDF文件操作 - 读取、解析、切分、合并、旋转、添加图片盖章等</li>
 *     <li>文件格式转换 - Excel/Word/Markdown/HTML/PDF之间互转</li>
 * </ul>
 * <p>
 * 配置前缀: xg
 * <p>
 * 示例配置:
 * <pre>
 * xg:
 *   enabled: true
 *   default-charset: UTF-8
 *   compression:
 *     default-charset: GBK  # 支持通过字符串名称指定字符集
 * </pre>
 *
 * @author ruozhuliufeng
 * @since 0.0.4
 */
package tech.msop.core.file;
