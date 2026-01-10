package tech.msop.core.file;

import tech.msop.core.file.properties.FileProperties;
import tech.msop.core.file.utils.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 文件服务，封装常用文件、压缩、Excel、PDF及格式转换操作
 *
 * @author ruozhuliufeng
 */
public class FileService {

    private final FileProperties properties;
    private final Charset defaultCharset;

    public FileService(FileProperties properties) {
        this.properties = properties;
        this.defaultCharset = resolveCharset(properties.getDefaultCharset());
    }

    // ============================ 基础文件操作 ============================

    public File createFile(String filePath) {
        return FileUtil.createFile(filePath);
    }

    public File createFile(File file) {
        return FileUtil.createFile(file);
    }

    public String readFileToString(String filePath) {
        return FileUtil.readFileToString(filePath, defaultCharset);
    }

    public String readFileToString(File file) {
        return FileUtil.readFileToString(file, defaultCharset);
    }

    public void writeStringToFile(String filePath, String content) {
        FileUtil.writeStringToFile(filePath, content, defaultCharset, false);
    }

    public void writeStringToFile(String filePath, String content, boolean append) {
        FileUtil.writeStringToFile(filePath, content, defaultCharset, append);
    }

    public void writeBytesToFile(String filePath, byte[] data) {
        FileUtil.writeByteArrayToFile(filePath, data);
    }

    public boolean deleteFile(String filePath) {
        return FileUtil.deleteFile(filePath);
    }

    public boolean exists(String filePath) {
        return FileUtil.exists(filePath);
    }

    public long getFileSize(String filePath) {
        return FileUtil.getFileSize(filePath);
    }

    // ============================ 目录操作 ============================

    public File createDirectory(String dirPath) {
        return DirectoryUtil.createDirectory(dirPath);
    }

    public void deleteDirectory(String dirPath) {
        DirectoryUtil.deleteDirectory(dirPath);
    }

    public List<File> listFiles(String dirPath, boolean recursive) {
        return DirectoryUtil.listFiles(dirPath, recursive);
    }

    public List<File> listDirectories(String dirPath, boolean recursive) {
        return DirectoryUtil.listDirectories(dirPath, recursive);
    }

    public long getDirectorySize(String dirPath) {
        return DirectoryUtil.getDirectorySize(dirPath);
    }

    // ============================ 压缩文件操作 ============================

    public void decompress(File archiveFile, File targetDir) {
        CompressionUtil.decompress(archiveFile, targetDir, null, getCompressionCharset());
    }

    public void decompress(File archiveFile, File targetDir, String password) {
        CompressionUtil.decompress(archiveFile, targetDir, password, getCompressionCharset());
    }

    public void decompressWithCharset(File archiveFile, File targetDir, String charsetName) {
        CompressionUtil.decompress(archiveFile, targetDir, null, resolveCharset(charsetName));
    }

    public void decompress(File archiveFile, File targetDir, String password, String charsetName) {
        CompressionUtil.decompress(archiveFile, targetDir, password, resolveCharset(charsetName));
    }

    public void decompressPath(String archivePath, String targetDir, String charsetName) {
        CompressionUtil.decompressWithCharset(archivePath, targetDir, null, charsetName);
    }

    public void decompressPath(String archivePath, String targetDir, String password, String charsetName) {
        CompressionUtil.decompressWithCharset(archivePath, targetDir, password, charsetName);
    }

    public void decompress(File archiveFile, File targetDir, String password, Charset charset) {
        CompressionUtil.decompress(archiveFile, targetDir, password, charset);
    }

    public boolean archiveContains(File archiveFile, String fileName) {
        return CompressionUtil.containsFile(archiveFile, fileName);
    }

    // ============================ Excel操作 ============================

    public List<List<String>> readExcel(File excelFile) {
        return ExcelUtil.readExcel(excelFile);
    }

    public void fillExcelTemplate(File templateFile, File destFile, Map<String, String> data) {
        ExcelUtil.fillTemplate(templateFile, destFile, data);
    }

    public void addImageToExcel(File excelFile, File imageFile, int sheetIndex, int row1, int col1, int row2, int col2) {
        ExcelUtil.addImageToExcel(excelFile, imageFile, sheetIndex, row1, col1, row2, col2);
    }

    // ============================ PDF操作 ============================

    public String readPdfText(File pdfFile) {
        return PdfUtil.readPdfText(pdfFile);
    }

    public String readPdfText(File pdfFile, int startPage, int endPage) {
        return PdfUtil.readPdfText(pdfFile, startPage, endPage);
    }

    public int getPdfPageCount(File pdfFile) {
        return PdfUtil.getPageCount(pdfFile);
    }

    public List<File> splitPdf(File pdfFile, File outputDir) {
        return PdfUtil.splitPdf(pdfFile, outputDir);
    }

    public void splitPdf(File pdfFile, File outputFile, int startPage, int endPage) {
        PdfUtil.splitPdfByRange(pdfFile, outputFile, startPage, endPage);
    }

    public void mergePdfs(List<File> pdfFiles, File outputFile) {
        PdfUtil.mergePdfs(pdfFiles, outputFile);
    }

    public void rotatePdf(File pdfFile, File outputFile, int rotation) {
        PdfUtil.rotatePdf(pdfFile, outputFile, rotation);
    }

    public void rotatePdfPage(File pdfFile, File outputFile, int pageIndex, int rotation) {
        PdfUtil.rotatePdfPage(pdfFile, outputFile, pageIndex, rotation);
    }

    public void addImageToPdf(File pdfFile, File outputFile, File imageFile,
                              float x, float y, float width, float height, int pageIndex) {
        PdfUtil.addImageToPdf(pdfFile, outputFile, imageFile, x, y, width, height, pageIndex);
    }

    public void addWatermark(File pdfFile, File outputFile, String watermark, float opacity, float fontSize) {
        PdfUtil.addWatermark(pdfFile, outputFile, watermark, opacity, fontSize);
    }

    public void createSimplePdf(File outputFile, String text) {
        PdfUtil.createSimplePdf(outputFile, text);
    }

    // ============================ 文件格式转换 ============================

    public void convert(File sourceFile, File targetFile, String targetType) {
        FileConvertUtil.convert(sourceFile, targetFile, targetType);
    }

    public void excelToPdf(File excelFile, File pdfFile) {
        FileConvertUtil.excelToPdf(excelFile, pdfFile);
    }

    public void excelToHtml(File excelFile, File htmlFile) {
        FileConvertUtil.excelToHtml(excelFile, htmlFile);
    }

    public void markdownToPdf(File markdownFile, File pdfFile) {
        FileConvertUtil.markdownToPdf(markdownFile, pdfFile);
    }

    public void markdownToWord(File markdownFile, File wordFile) {
        FileConvertUtil.markdownToWord(markdownFile, wordFile);
    }

    public void markdownToHtml(File markdownFile, File htmlFile) {
        FileConvertUtil.markdownToHtml(markdownFile, htmlFile);
    }

    public void wordToPdf(File wordFile, File pdfFile) {
        FileConvertUtil.wordToPdf(wordFile, pdfFile);
    }

    public void wordToHtml(File wordFile, File htmlFile) {
        FileConvertUtil.wordToHtml(wordFile, htmlFile);
    }

    public void htmlToPdf(File htmlFile, File pdfFile) {
        FileConvertUtil.htmlToPdf(htmlFile, pdfFile);
    }

    public void htmlToWord(File htmlFile, File wordFile) {
        FileConvertUtil.htmlToWord(htmlFile, wordFile);
    }

    public FileProperties getProperties() {
        return properties;
    }

    private Charset resolveCharset(String charsetName) {
        if (charsetName == null || charsetName.trim().isEmpty()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(charsetName);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private Charset getCompressionCharset() {
        String compressionCharset = properties.getCompression().getDefaultCharset();
        return resolveCharset(compressionCharset);
    }
}
