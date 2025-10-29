package tech.msop.core.file.utils;

import org.apache.commons.io.IOUtils;
import tech.msop.core.file.exception.FileOperationException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件操作工具类
 * 提供文件创建、读取、删除、复制、移动等基础功能
 *
 * @author ruozhuliufeng
 */
public class FileUtil {

    /**
     * 创建文件，如果父目录不存在则自动创建
     *
     * @param filePath 文件路径
     * @return File对象
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        return createFile(file);
    }

    /**
     * 创建文件，如果父目录不存在则自动创建
     *
     * @param file File对象
     * @return File对象
     */
    public static File createFile(File file) {
        if (file == null) {
            throw new FileOperationException("File对象不能为null");
        }
        
        if (file.exists()) {
            return file;
        }
        
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new FileOperationException("创建父目录失败: " + parentDir.getAbsolutePath());
            }
        }
        
        try {
            if (!file.createNewFile()) {
                throw new FileOperationException("创建文件失败: " + file.getAbsolutePath());
            }
            return file;
        } catch (IOException e) {
            throw new FileOperationException("创建文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 读取文件内容为字符串（UTF-8编码）
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFileToString(String filePath) {
        return readFileToString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @param charset  字符集
     * @return 文件内容
     */
    public static String readFileToString(String filePath, Charset charset) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), charset);
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败: " + filePath, e);
        }
    }

    /**
     * 读取文件内容为字符串（指定编码）
     *
     * @param file    文件对象
     * @param charset 字符集
     * @return 文件内容
     */
    public static String readFileToString(File file, Charset charset) {
        try {
            return new String(Files.readAllBytes(file.toPath()), charset);
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param filePath 文件路径
     * @return 字节数组
     */
    public static byte[] readFileToByteArray(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败: " + filePath, e);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param file 文件对象
     * @return 字节数组
     */
    public static byte[] readFileToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 读取文件所有行（UTF-8编码）
     *
     * @param filePath 文件路径
     * @return 行列表
     */
    public static List<String> readLines(String filePath) {
        return readLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件所有行
     *
     * @param filePath 文件路径
     * @param charset  字符集
     * @return 行列表
     */
    public static List<String> readLines(String filePath, Charset charset) {
        try {
            return Files.readAllLines(Paths.get(filePath), charset);
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败: " + filePath, e);
        }
    }

    /**
     * 写入字符串到文件（UTF-8编码）
     *
     * @param filePath 文件路径
     * @param content  内容
     */
    public static void writeStringToFile(String filePath, String content) {
        writeStringToFile(filePath, content, StandardCharsets.UTF_8, false);
    }

    /**
     * 写入字符串到文件
     *
     * @param filePath 文件路径
     * @param content  内容
     * @param charset  字符集
     * @param append   是否追加
     */
    public static void writeStringToFile(String filePath, String content, Charset charset, boolean append) {
        File file = new File(filePath);
        createFile(file);
        try {
            if (append) {
                Files.write(file.toPath(), content.getBytes(charset), StandardOpenOption.APPEND);
            } else {
                Files.write(file.toPath(), content.getBytes(charset));
            }
        } catch (IOException e) {
            throw new FileOperationException("写入文件失败: " + filePath, e);
        }
    }

    /**
     * 写入字节数组到文件
     *
     * @param filePath 文件路径
     * @param data     字节数组
     */
    public static void writeByteArrayToFile(String filePath, byte[] data) {
        writeByteArrayToFile(new File(filePath), data, false);
    }

    /**
     * 写入字节数组到文件
     *
     * @param file   文件对象
     * @param data   字节数组
     * @param append 是否追加
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) {
        createFile(file);
        try {
            if (append) {
                Files.write(file.toPath(), data, StandardOpenOption.APPEND);
            } else {
                Files.write(file.toPath(), data);
            }
        } catch (IOException e) {
            throw new FileOperationException("写入文件失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 写入行列表到文件（UTF-8编码）
     *
     * @param filePath 文件路径
     * @param lines    行列表
     */
    public static void writeLines(String filePath, List<String> lines) {
        writeLines(filePath, lines, StandardCharsets.UTF_8, false);
    }

    /**
     * 写入行列表到文件
     *
     * @param filePath 文件路径
     * @param lines    行列表
     * @param charset  字符集
     * @param append   是否追加
     */
    public static void writeLines(String filePath, List<String> lines, Charset charset, boolean append) {
        File file = new File(filePath);
        createFile(file);
        try {
            if (append) {
                Files.write(file.toPath(), lines, charset, StandardOpenOption.APPEND);
            } else {
                Files.write(file.toPath(), lines, charset);
            }
        } catch (IOException e) {
            throw new FileOperationException("写入文件失败: " + filePath, e);
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        return deleteFile(new File(filePath));
    }

    /**
     * 删除文件
     *
     * @param file 文件对象
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * 复制文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     */
    public static void copyFile(String srcPath, String destPath) {
        copyFile(new File(srcPath), new File(destPath), false);
    }

    /**
     * 复制文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param replace  是否替换已存在的目标文件
     */
    public static void copyFile(File srcFile, File destFile, boolean replace) {
        if (!srcFile.exists()) {
            throw new FileOperationException("源文件不存在: " + srcFile.getAbsolutePath());
        }
        
        if (!srcFile.isFile()) {
            throw new FileOperationException("源文件不是普通文件: " + srcFile.getAbsolutePath());
        }
        
        createFile(destFile);
        
        try {
            if (replace) {
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(srcFile.toPath(), destFile.toPath());
            }
        } catch (IOException e) {
            throw new FileOperationException("复制文件失败: " + srcFile.getAbsolutePath() + " -> " + destFile.getAbsolutePath(), e);
        }
    }

    /**
     * 移动文件
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     */
    public static void moveFile(String srcPath, String destPath) {
        moveFile(new File(srcPath), new File(destPath), false);
    }

    /**
     * 移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param replace  是否替换已存在的目标文件
     */
    public static void moveFile(File srcFile, File destFile, boolean replace) {
        if (!srcFile.exists()) {
            throw new FileOperationException("源文件不存在: " + srcFile.getAbsolutePath());
        }
        
        if (!srcFile.isFile()) {
            throw new FileOperationException("源文件不是普通文件: " + srcFile.getAbsolutePath());
        }
        
        File destParent = destFile.getParentFile();
        if (destParent != null && !destParent.exists()) {
            if (!destParent.mkdirs()) {
                throw new FileOperationException("创建目标目录失败: " + destParent.getAbsolutePath());
            }
        }
        
        try {
            if (replace) {
                Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(srcFile.toPath(), destFile.toPath());
            }
        } catch (IOException e) {
            throw new FileOperationException("移动文件失败: " + srcFile.getAbsolutePath() + " -> " + destFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    public static long getFileSize(String filePath) {
        return getFileSize(new File(filePath));
    }

    /**
     * 获取文件大小
     *
     * @param file 文件对象
     * @return 文件大小（字节）
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return 0L;
        }
        return file.length();
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（不含点）
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param fileName 文件名
     * @return 文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 判断是否是文件
     *
     * @param filePath 文件路径
     * @return 是否是文件
     */
    public static boolean isFile(String filePath) {
        return Files.isRegularFile(Paths.get(filePath));
    }

    /**
     * 判断是否是目录
     *
     * @param filePath 文件路径
     * @return 是否是目录
     */
    public static boolean isDirectory(String filePath) {
        return Files.isDirectory(Paths.get(filePath));
    }

    /**
     * 使用输入流复制文件
     *
     * @param inputStream 输入流
     * @param destFile    目标文件
     */
    public static void copyInputStreamToFile(InputStream inputStream, File destFile) {
        createFile(destFile);
        try (FileOutputStream outputStream = new FileOutputStream(destFile)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new FileOperationException("复制输入流到文件失败: " + destFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取文件输入流
     *
     * @param file 文件对象
     * @return 输入流
     */
    public static InputStream getFileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new FileOperationException("文件不存在: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 获取文件输出流
     *
     * @param file 文件对象
     * @return 输出流
     */
    public static OutputStream getFileOutputStream(File file) {
        createFile(file);
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new FileOperationException("创建文件输出流失败: " + file.getAbsolutePath(), e);
        }
    }
}
