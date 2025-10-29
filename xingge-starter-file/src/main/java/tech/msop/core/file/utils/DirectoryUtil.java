package tech.msop.core.file.utils;

import org.apache.commons.io.FileUtils;
import tech.msop.core.file.exception.FileOperationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件夹操作工具类
 * 提供文件夹创建、列表、删除等功能
 *
 * @author ruozhuliufeng
 */
public class DirectoryUtil {

    /**
     * 创建目录，如果父目录不存在则自动创建
     *
     * @param dirPath 目录路径
     * @return File对象
     */
    public static File createDirectory(String dirPath) {
        return createDirectory(new File(dirPath));
    }

    /**
     * 创建目录，如果父目录不存在则自动创建
     *
     * @param dir File对象
     * @return File对象
     */
    public static File createDirectory(File dir) {
        if (dir == null) {
            throw new FileOperationException("目录对象不能为null");
        }
        
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new FileOperationException("路径已存在但不是目录: " + dir.getAbsolutePath());
            }
            return dir;
        }
        
        if (!dir.mkdirs()) {
            throw new FileOperationException("创建目录失败: " + dir.getAbsolutePath());
        }
        
        return dir;
    }

    /**
     * 删除目录（包括所有子文件和子目录）
     *
     * @param dirPath 目录路径
     * @return 是否删除成功
     */
    public static boolean deleteDirectory(String dirPath) {
        return deleteDirectory(new File(dirPath));
    }

    /**
     * 删除目录（包括所有子文件和子目录）
     *
     * @param dir 目录对象
     * @return 是否删除成功
     */
    public static boolean deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return false;
        }
        
        if (!dir.isDirectory()) {
            throw new FileOperationException("路径不是目录: " + dir.getAbsolutePath());
        }
        
        try {
            FileUtils.deleteDirectory(dir);
            return true;
        } catch (IOException e) {
            throw new FileOperationException("删除目录失败: " + dir.getAbsolutePath(), e);
        }
    }

    /**
     * 清空目录（删除所有子文件和子目录，但保留目录本身）
     *
     * @param dirPath 目录路径
     */
    public static void cleanDirectory(String dirPath) {
        cleanDirectory(new File(dirPath));
    }

    /**
     * 清空目录（删除所有子文件和子目录，但保留目录本身）
     *
     * @param dir 目录对象
     */
    public static void cleanDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        
        if (!dir.isDirectory()) {
            throw new FileOperationException("路径不是目录: " + dir.getAbsolutePath());
        }
        
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            throw new FileOperationException("清空目录失败: " + dir.getAbsolutePath(), e);
        }
    }

    /**
     * 列出目录下的所有文件和子目录（不递归）
     *
     * @param dirPath 目录路径
     * @return 文件列表
     */
    public static List<File> listFiles(String dirPath) {
        return listFiles(new File(dirPath), false);
    }

    /**
     * 列出目录下的所有文件和子目录
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> listFiles(String dirPath, boolean recursive) {
        return listFiles(new File(dirPath), recursive);
    }

    /**
     * 列出目录下的所有文件和子目录
     *
     * @param dir       目录对象
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> listFiles(File dir, boolean recursive) {
        if (dir == null || !dir.exists()) {
            return new ArrayList<>();
        }
        
        if (!dir.isDirectory()) {
            throw new FileOperationException("路径不是目录: " + dir.getAbsolutePath());
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        
        List<File> result = new ArrayList<>();
        for (File file : files) {
            result.add(file);
            if (recursive && file.isDirectory()) {
                result.addAll(listFiles(file, true));
            }
        }
        
        return result;
    }

    /**
     * 列出目录下的所有文件（不包括子目录）
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> listFilesOnly(String dirPath, boolean recursive) {
        return listFilesOnly(new File(dirPath), recursive);
    }

    /**
     * 列出目录下的所有文件（不包括子目录）
     *
     * @param dir       目录对象
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> listFilesOnly(File dir, boolean recursive) {
        List<File> allFiles = listFiles(dir, recursive);
        return allFiles.stream()
                .filter(File::isFile)
                .collect(Collectors.toList());
    }

    /**
     * 列出目录下的所有子目录（不递归）
     *
     * @param dirPath 目录路径
     * @return 子目录列表
     */
    public static List<File> listDirectories(String dirPath) {
        return listDirectories(new File(dirPath), false);
    }

    /**
     * 列出目录下的所有子目录
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 子目录列表
     */
    public static List<File> listDirectories(String dirPath, boolean recursive) {
        return listDirectories(new File(dirPath), recursive);
    }

    /**
     * 列出目录下的所有子目录
     *
     * @param dir       目录对象
     * @param recursive 是否递归子目录
     * @return 子目录列表
     */
    public static List<File> listDirectories(File dir, boolean recursive) {
        List<File> allFiles = listFiles(dir, recursive);
        return allFiles.stream()
                .filter(File::isDirectory)
                .collect(Collectors.toList());
    }

    /**
     * 复制目录
     *
     * @param srcPath  源目录路径
     * @param destPath 目标目录路径
     */
    public static void copyDirectory(String srcPath, String destPath) {
        copyDirectory(new File(srcPath), new File(destPath));
    }

    /**
     * 复制目录
     *
     * @param srcDir  源目录
     * @param destDir 目标目录
     */
    public static void copyDirectory(File srcDir, File destDir) {
        if (!srcDir.exists()) {
            throw new FileOperationException("源目录不存在: " + srcDir.getAbsolutePath());
        }
        
        if (!srcDir.isDirectory()) {
            throw new FileOperationException("源路径不是目录: " + srcDir.getAbsolutePath());
        }
        
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            throw new FileOperationException("复制目录失败: " + srcDir.getAbsolutePath() + " -> " + destDir.getAbsolutePath(), e);
        }
    }

    /**
     * 移动目录
     *
     * @param srcPath  源目录路径
     * @param destPath 目标目录路径
     */
    public static void moveDirectory(String srcPath, String destPath) {
        moveDirectory(new File(srcPath), new File(destPath));
    }

    /**
     * 移动目录
     *
     * @param srcDir  源目录
     * @param destDir 目标目录
     */
    public static void moveDirectory(File srcDir, File destDir) {
        if (!srcDir.exists()) {
            throw new FileOperationException("源目录不存在: " + srcDir.getAbsolutePath());
        }
        
        if (!srcDir.isDirectory()) {
            throw new FileOperationException("源路径不是目录: " + srcDir.getAbsolutePath());
        }
        
        try {
            FileUtils.moveDirectory(srcDir, destDir);
        } catch (IOException e) {
            throw new FileOperationException("移动目录失败: " + srcDir.getAbsolutePath() + " -> " + destDir.getAbsolutePath(), e);
        }
    }

    /**
     * 获取目录大小
     *
     * @param dirPath 目录路径
     * @return 目录大小（字节）
     */
    public static long getDirectorySize(String dirPath) {
        return getDirectorySize(new File(dirPath));
    }

    /**
     * 获取目录大小
     *
     * @param dir 目录对象
     * @return 目录大小（字节）
     */
    public static long getDirectorySize(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return 0L;
        }
        
        return FileUtils.sizeOfDirectory(dir);
    }

    /**
     * 判断目录是否为空
     *
     * @param dirPath 目录路径
     * @return 是否为空
     */
    public static boolean isEmptyDirectory(String dirPath) {
        return isEmptyDirectory(new File(dirPath));
    }

    /**
     * 判断目录是否为空
     *
     * @param dir 目录对象
     * @return 是否为空
     */
    public static boolean isEmptyDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return true;
        }
        
        File[] files = dir.listFiles();
        return files == null || files.length == 0;
    }

    /**
     * 确保目录存在，如果不存在则创建
     *
     * @param dirPath 目录路径
     * @return File对象
     */
    public static File ensureDirectoryExists(String dirPath) {
        return createDirectory(dirPath);
    }

    /**
     * 遍历目录树
     *
     * @param dirPath 目录路径
     * @param visitor 访问器
     */
    public static void walkFileTree(String dirPath, FileVisitor<? super Path> visitor) {
        try {
            Files.walkFileTree(Paths.get(dirPath), visitor);
        } catch (IOException e) {
            throw new FileOperationException("遍历目录失败: " + dirPath, e);
        }
    }

    /**
     * 统计目录下文件数量
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 文件数量
     */
    public static int countFiles(String dirPath, boolean recursive) {
        return listFilesOnly(dirPath, recursive).size();
    }

    /**
     * 统计目录下子目录数量
     *
     * @param dirPath   目录路径
     * @param recursive 是否递归子目录
     * @return 子目录数量
     */
    public static int countDirectories(String dirPath, boolean recursive) {
        return listDirectories(dirPath, recursive).size();
    }
}
