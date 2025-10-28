package tech.msop.core.file.utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.IOUtils;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.volume.FileVolumeManager;
import tech.msop.core.file.exception.FileOperationException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 压缩文件操作工具类
 * 支持ZIP、RAR、TAR等多种压缩格式，支持密码解压及指定字符集
 *
 * @author ruozhuliufeng
 */
public class CompressionUtil {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private CompressionUtil() {
    }

    /**
     * 解压压缩文件到指定目录（默认UTF-8编码，无密码）
     *
     * @param archivePath 压缩文件路径
     * @param targetDir   目标目录
     */
    public static void decompress(String archivePath, String targetDir) {
        decompress(archivePath, targetDir, null, DEFAULT_CHARSET);
    }

    /**
     * 解压压缩文件到指定目录（默认UTF-8编码）
     *
     * @param archivePath 压缩文件路径
     * @param targetDir   目标目录
     * @param password    密码（可选）
     */
    public static void decompress(String archivePath, String targetDir, String password) {
        decompress(archivePath, targetDir, password, DEFAULT_CHARSET);
    }

    /**
     * 解压压缩文件到指定目录
     *
     * @param archivePath 压缩文件路径
     * @param targetDir   目标目录
     * @param password    密码（可选）
     * @param charset     字符集
     */
    public static void decompress(String archivePath, String targetDir, String password, Charset charset) {
        if (archivePath == null || archivePath.trim().isEmpty()) {
            throw new FileOperationException("压缩文件路径不能为空");
        }
        if (targetDir == null || targetDir.trim().isEmpty()) {
            throw new FileOperationException("目标目录不能为空");
        }
        File archiveFile = new File(archivePath);
        File destination = new File(targetDir);
        decompress(archiveFile, destination, password, charset);
    }

    /**
     * 解压压缩文件到指定目录
     *
     * @param archiveFile 压缩文件
     * @param targetDir   目标目录
     * @param password    密码（可选）
     * @param charset     字符集
     */
    public static void decompress(File archiveFile, File targetDir, String password, Charset charset) {
        if (archiveFile == null || !archiveFile.exists()) {
            throw new FileOperationException("压缩文件不存在: " + (archiveFile == null ? "null" : archiveFile.getAbsolutePath()));
        }
        if (targetDir == null) {
            throw new FileOperationException("目标目录不能为空");
        }
        
        // 创建目标目录
        DirectoryUtil.createDirectory(targetDir);
        
        String fileName = archiveFile.getName().toLowerCase();
        try {
            if (fileName.endsWith(".zip")) {
                decompressZip(archiveFile, targetDir, password, charset);
            } else if (fileName.endsWith(".rar")) {
                decompressRar(archiveFile, targetDir, password);
            } else if (fileName.endsWith(".tar") || fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")
                    || fileName.endsWith(".tar.bz2") || fileName.endsWith(".tbz2")
                    || fileName.endsWith(".tar.xz") || fileName.endsWith(".txz")) {
                decompressTar(archiveFile, targetDir, charset);
            } else {
                throw new FileOperationException("暂不支持的压缩格式: " + archiveFile.getName());
            }
        } catch (IOException e) {
            throw new FileOperationException("解压压缩文件失败: " + archiveFile.getAbsolutePath(), e);
        }
    }

    /**
     * 使用Zip4j解压ZIP文件
     */
    private static void decompressZip(File archiveFile, File targetDir, String password, Charset charset) {
        try {
            ZipFile zipFile = new ZipFile(archiveFile);
            zipFile.setCharset(charset == null ? DEFAULT_CHARSET : charset);
            if (zipFile.isEncrypted()) {
                if (password == null) {
                    throw new FileOperationException("ZIP文件已加密，必须提供密码");
                }
                zipFile.setPassword(password.toCharArray());
            } else if (password != null) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(targetDir.getAbsolutePath());
        } catch (ZipException e) {
            throw new FileOperationException("解压ZIP文件失败: " + archiveFile.getAbsolutePath(), e);
        }
    }

    /**
     * 使用Junrar解压RAR文件
     */
    private static void decompressRar(File archiveFile, File targetDir, String password) {
        try (Archive archive = password == null ? new Archive(archiveFile) : new Archive(new FileVolumeManager(archiveFile), password)) {
            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                String fileName = fileHeader.getFileNameW();
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = fileHeader.getFileNameString();
                }
                if (fileName == null) {
                    continue;
                }
                File destFile = new File(targetDir, fileName.trim());
                if (fileHeader.isDirectory()) {
                    DirectoryUtil.createDirectory(destFile);
                } else {
                    File parent = destFile.getParentFile();
                    if (parent != null) {
                        DirectoryUtil.createDirectory(parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        archive.extractFile(fileHeader, fos);
                    }
                }
            }
        } catch (Exception e) {
            throw new FileOperationException("解压RAR文件失败: " + archiveFile.getAbsolutePath(), e);
        }
    }

    /**
     * 使用Commons Compress解压TAR系列压缩文件
     */
    private static void decompressTar(File archiveFile, File targetDir, Charset charset) throws IOException {
        Charset actualCharset = charset == null ? DEFAULT_CHARSET : charset;
        String fileName = archiveFile.getName().toLowerCase();

        try (InputStream fis = Files.newInputStream(archiveFile.toPath());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStream compressorInput = wrapCompressorStream(bis, fileName);
             TarArchiveInputStream tais = new TarArchiveInputStream(
                     compressorInput == null ? bis : compressorInput,
                     actualCharset.name())) {

            TarArchiveEntry entry;
            while ((entry = tais.getNextTarEntry()) != null) {
                File destFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    DirectoryUtil.createDirectory(destFile);
                } else {
                    File parent = destFile.getParentFile();
                    if (parent != null) {
                        DirectoryUtil.createDirectory(parent);
                    }
                    try (OutputStream os = new FileOutputStream(destFile)) {
                        IOUtils.copy(tais, os);
                    }
                    destFile.setLastModified(entry.getLastModifiedDate().getTime());
                }
            }
        }
    }

    /**
     * 根据文件扩展名包装压缩流
     */
    private static InputStream wrapCompressorStream(BufferedInputStream bis, String fileName) throws IOException {
        if (fileName.endsWith(".gz") || fileName.endsWith(".tgz")) {
            return new GzipCompressorInputStream(bis);
        }
        if (fileName.endsWith(".bz2") || fileName.endsWith(".tbz") || fileName.endsWith(".tbz2")) {
            return new BZip2CompressorInputStream(bis);
        }
        if (fileName.endsWith(".xz") || fileName.endsWith(".txz")) {
            return new XZCompressorInputStream(bis);
        }
        return null;
    }

    /**
     * 校验压缩文件是否包含指定文件
     *
     * @param archiveFile 压缩文件
     * @param fileName    文件名
     * @return 是否存在
     */
    public static boolean containsFile(File archiveFile, String fileName) {
        if (archiveFile == null || !archiveFile.exists()) {
            return false;
        }
        String lowerName = archiveFile.getName().toLowerCase();
        try {
            if (lowerName.endsWith(".zip")) {
                ZipFile zipFile = new ZipFile(archiveFile);
                return zipFile.getFileHeaders().stream()
                        .map(FileHeader::getFileName)
                        .anyMatch(name -> name.equalsIgnoreCase(fileName));
            } else if (lowerName.endsWith(".rar")) {
                try (Archive archive = new Archive(archiveFile)) {
                    FileHeader fileHeader;
                    while ((fileHeader = archive.nextFileHeader()) != null) {
                        String headerName = fileHeader.getFileNameW();
                        if (headerName == null || headerName.trim().isEmpty()) {
                            headerName = fileHeader.getFileNameString();
                        }
                        if (headerName != null && headerName.equalsIgnoreCase(fileName)) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                throw new FileOperationException("暂不支持的压缩格式: " + archiveFile.getName());
            }
        } catch (Exception e) {
            throw new FileOperationException("检查压缩文件内容失败: " + archiveFile.getAbsolutePath(), e);
        }
    }
}
