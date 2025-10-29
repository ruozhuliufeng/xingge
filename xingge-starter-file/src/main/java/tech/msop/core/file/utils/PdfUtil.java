package tech.msop.core.file.utils;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;
import tech.msop.core.file.exception.FileOperationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF操作工具类
 * 提供PDF读取、解析、旋转、切分、合并、添加图片等功能
 * 依赖Apache PDFBox实现
 *
 * @author
 */
public class PdfUtil {

    private PdfUtil() {
    }

    /**
     * 读取PDF文本内容
     *
     * @param pdfFile PDF文件
     * @return 文本内容
     */
    public static String readPdfText(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new FileOperationException("读取PDF文本失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 读取PDF指定页范围文本
     *
     * @param pdfFile   PDF文件
     * @param startPage 起始页（从1开始）
     * @param endPage   结束页
     * @return 文本内容
     */
    public static String readPdfText(File pdfFile, int startPage, int endPage) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new FileOperationException("读取PDF文本失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取PDF页数
     *
     * @param pdfFile PDF文件
     * @return 页数
     */
    public static int getPageCount(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new FileOperationException("获取PDF页数失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 切分PDF每一页为独立文件
     *
     * @param pdfFile   源PDF
     * @param outputDir 输出目录
     * @return 切分后的文件列表
     */
    public static List<File> splitPdf(File pdfFile, File outputDir) {
        DirectoryUtil.createDirectory(outputDir);
        List<File> result = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            Splitter splitter = new Splitter();
            List<PDDocument> documents = splitter.split(document);
            int index = 1;
            for (PDDocument pdDocument : documents) {
                String name = FileUtil.getFileNameWithoutExtension(pdfFile.getName()) + "_page_" + index + ".pdf";
                File out = new File(outputDir, name);
                pdDocument.save(out);
                pdDocument.close();
                result.add(out);
                index++;
            }
        } catch (IOException e) {
            throw new FileOperationException("切分PDF失败: " + pdfFile.getAbsolutePath(), e);
        }
        return result;
    }

    /**
     * 根据页码范围切分PDF
     *
     * @param pdfFile   源文件
     * @param outputFile 输出文件
     * @param startPage 起始页（从1开始）
     * @param endPage   结束页
     */
    public static void splitPdfByRange(File pdfFile, File outputFile, int startPage, int endPage) {
        try (PDDocument document = PDDocument.load(pdfFile);
             PDDocument output = new PDDocument()) {
            if (startPage < 1 || endPage > document.getNumberOfPages() || startPage > endPage) {
                throw new FileOperationException("页数范围无效: " + startPage + "-" + endPage);
            }
            for (int i = startPage - 1; i < endPage; i++) {
                output.addPage(document.getPage(i));
            }
            FileUtil.createFile(outputFile);
            output.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("切分PDF失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 合并多个PDF文件
     *
     * @param pdfFiles   PDF文件列表
     * @param outputFile 输出文件
     */
    public static void mergePdfs(List<File> pdfFiles, File outputFile) {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            throw new FileOperationException("PDF文件列表不能为空");
        }
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        pdfFiles.forEach(file -> {
            if (!file.exists()) {
                throw new FileOperationException("PDF文件不存在: " + file.getAbsolutePath());
            }
            mergerUtility.addSource(file);
        });
        mergerUtility.setDestinationFileName(outputFile.getAbsolutePath());
        try {
            mergerUtility.mergeDocuments(null);
        } catch (IOException e) {
            throw new FileOperationException("合并PDF失败", e);
        }
    }

    /**
     * 旋转PDF所有页面
     *
     * @param pdfFile    PDF文件
     * @param outputFile 输出文件
     * @param rotation   旋转角度（90/180/270）
     */
    public static void rotatePdf(File pdfFile, File outputFile, int rotation) {
        if (rotation % 90 != 0) {
            throw new FileOperationException("旋转角度必须为90的倍数");
        }
        try (PDDocument document = PDDocument.load(pdfFile)) {
            for (PDPage page : document.getPages()) {
                page.setRotation(page.getRotation() + rotation);
            }
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("旋转PDF失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 旋转PDF指定页面
     *
     * @param pdfFile    PDF文件
     * @param outputFile 输出文件
     * @param pageIndex  页面索引（从0开始）
     * @param rotation   旋转角度
     */
    public static void rotatePdfPage(File pdfFile, File outputFile, int pageIndex, int rotation) {
        if (rotation % 90 != 0) {
            throw new FileOperationException("旋转角度必须为90的倍数");
        }
        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new FileOperationException("页面索引无效: " + pageIndex);
            }
            PDPage page = document.getPage(pageIndex);
            page.setRotation(page.getRotation() + rotation);
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("旋转PDF页面失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 向PDF添加图片（盖章）
     *
     * @param pdfFile    PDF文件
     * @param outputFile 输出文件
     * @param imageFile  图片文件
     * @param x          X坐标
     * @param y          Y坐标
     * @param width      宽度
     * @param height     高度
     * @param pageIndex  页面索引（-1表示所有页面）
     */
    public static void addImageToPdf(File pdfFile, File outputFile, File imageFile,
                                     float x, float y, float width, float height, int pageIndex) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), document);
            int totalPages = document.getNumberOfPages();
            int start = pageIndex < 0 ? 0 : pageIndex;
            int end = pageIndex < 0 ? totalPages : pageIndex + 1;
            for (int i = start; i < end; i++) {
                if (i >= totalPages) {
                    break;
                }
                PDPage page = document.getPage(i);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.drawImage(image, x, y, width, height);
                }
            }
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("向PDF添加图片失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 为PDF添加文本水印
     *
     * @param pdfFile    PDF文件
     * @param outputFile 输出文件
     * @param watermark  水印文本
     * @param opacity    透明度（0-1）
     * @param fontSize   字体大小
     */
    public static void addWatermark(File pdfFile, File outputFile, String watermark, float opacity, float fontSize) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(opacity);
            graphicsState.setStrokingAlphaConstant(opacity);

            PDType0Font cjkFont = containsNonAscii(watermark) ? loadEmbeddedCjkFont(document) : null;

            for (PDPage page : document.getPages()) {
                PDRectangle pageSize = page.getMediaBox();
                float x = pageSize.getWidth() / 2;
                float y = pageSize.getHeight() / 2;

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.saveGraphicsState();
                    contentStream.setGraphicsStateParameters(graphicsState);
                    contentStream.beginText();

                    contentStream.setNonStrokingColor(200, 200, 200);
                    contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45), x, y));

                    if (cjkFont != null) {
                        contentStream.setFont(cjkFont, fontSize);
                    } else {
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    }
                    contentStream.showText(watermark);
                    contentStream.endText();
                    contentStream.restoreGraphicsState();
                }
            }
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("为PDF添加水印失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    /**
     * 创建简单的PDF文档
     *
     * @param outputFile 输出文件
     * @param text       文本内容
     */
    public static void createSimplePdf(File outputFile, String text) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDType0Font cjkFont = containsNonAscii(text) ? loadEmbeddedCjkFont(document) : null;
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 700);
                if (cjkFont != null) {
                    contentStream.setFont(cjkFont, 12);
                } else {
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                }
                contentStream.showText(text);
                contentStream.endText();
            }
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("创建PDF失败: " + outputFile.getAbsolutePath(), e);
        }
    }

    /**
     * 提取指定页
     */
    public static void extractPage(File pdfFile, File outputFile, int pageNumber) {
        splitPdfByRange(pdfFile, outputFile, pageNumber, pageNumber);
    }

    /**
     * 删除指定页
     */
    public static void deletePages(File pdfFile, File outputFile, List<Integer> pageNumbers) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            pageNumbers.sort((a, b) -> b - a);
            for (Integer pageNumber : pageNumbers) {
                if (pageNumber > 0 && pageNumber <= document.getNumberOfPages()) {
                    document.removePage(pageNumber - 1);
                }
            }
            FileUtil.createFile(outputFile);
            document.save(outputFile);
        } catch (IOException e) {
            throw new FileOperationException("删除PDF页面失败: " + pdfFile.getAbsolutePath(), e);
        }
    }

    private static boolean containsNonAscii(String text) {
        if (text == null) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if (c > 127) {
                return true;
            }
        }
        return false;
    }

    private static PDType0Font loadEmbeddedCjkFont(PDDocument document) {
        try (InputStream stream = PdfUtil.class.getResourceAsStream("/fonts/NotoSansCJKsc-Regular.otf")) {
            if (stream != null) {
                return PDType0Font.load(document, stream);
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}

