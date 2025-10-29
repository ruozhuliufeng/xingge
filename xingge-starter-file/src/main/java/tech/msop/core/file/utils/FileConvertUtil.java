package tech.msop.core.file.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.poi.ss.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import tech.msop.core.file.exception.FileOperationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.StringWriter;

/**
 * 文件格式转换工具类
 * 支持Excel、Word、Markdown、PDF、HTML等格式互转
 *
 * @author ruozhuliufeng
 */
public class FileConvertUtil {

    private FileConvertUtil() {
    }

    /**
     * Excel转HTML
     *
     * @param excelFile Excel文件
     * @param htmlFile  HTML文件
     */
    public static void excelToHtml(File excelFile, File htmlFile) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n");
            html.append("<meta charset=\"UTF-8\">\n");
            html.append("<style>\n");
            html.append("table { border-collapse: collapse; width: 100%; }\n");
            html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            html.append("th { background-color: #f2f2f2; }\n");
            html.append("</style>\n</head>\n<body>\n");

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                html.append("<h2>").append(sheet.getSheetName()).append("</h2>\n");
                html.append("<table>\n");

                DataFormatter formatter = new DataFormatter();
                for (Row row : sheet) {
                    html.append("<tr>\n");
                    for (Cell cell : row) {
                        html.append("<td>").append(formatter.formatCellValue(cell)).append("</td>\n");
                    }
                    html.append("</tr>\n");
                }

                html.append("</table>\n<br/>\n");
            }

            html.append("</body>\n</html>");

            FileUtil.writeStringToFile(htmlFile.getAbsolutePath(), html.toString(), StandardCharsets.UTF_8, false);
        } catch (Exception e) {
            throw new FileOperationException("Excel转HTML失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * Excel转PDF
     *
     * @param excelFile Excel文件
     * @param pdfFile   PDF文件
     */
    public static void excelToPdf(File excelFile, File pdfFile) {
        File tempHtml = null;
        try {
            tempHtml = File.createTempFile("excel_", ".html");
            excelToHtml(excelFile, tempHtml);
            htmlToPdf(tempHtml, pdfFile);
        } catch (Exception e) {
            throw new FileOperationException("Excel转PDF失败: " + excelFile.getAbsolutePath(), e);
        } finally {
            if (tempHtml != null && tempHtml.exists()) {
                tempHtml.delete();
            }
        }
    }

    /**
     * Markdown转HTML
     *
     * @param markdownFile Markdown文件
     * @param htmlFile     HTML文件
     */
    public static void markdownToHtml(File markdownFile, File htmlFile) {
        try {
            String markdown = FileUtil.readFileToString(markdownFile.getAbsolutePath(), StandardCharsets.UTF_8);

            MutableDataSet options = new MutableDataSet();
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();

            Node document = parser.parse(markdown);
            String html = renderer.render(document);

            StringBuilder fullHtml = new StringBuilder();
            fullHtml.append("<!DOCTYPE html>\n<html>\n<head>\n");
            fullHtml.append("<meta charset=\"UTF-8\">\n");
            fullHtml.append("<style>\n");
            fullHtml.append("body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n");
            fullHtml.append("h1, h2, h3, h4, h5, h6 { margin-top: 20px; }\n");
            fullHtml.append("code { background-color: #f4f4f4; padding: 2px 4px; }\n");
            fullHtml.append("pre { background-color: #f4f4f4; padding: 10px; overflow-x: auto; }\n");
            fullHtml.append("</style>\n</head>\n<body>\n");
            fullHtml.append(html);
            fullHtml.append("\n</body>\n</html>");

            FileUtil.writeStringToFile(htmlFile.getAbsolutePath(), fullHtml.toString(), StandardCharsets.UTF_8, false);
        } catch (Exception e) {
            throw new FileOperationException("Markdown转HTML失败: " + markdownFile.getAbsolutePath(), e);
        }
    }

    /**
     * Markdown转PDF
     *
     * @param markdownFile Markdown文件
     * @param pdfFile      PDF文件
     */
    public static void markdownToPdf(File markdownFile, File pdfFile) {
        File tempHtml = null;
        try {
            tempHtml = File.createTempFile("markdown_", ".html");
            markdownToHtml(markdownFile, tempHtml);
            htmlToPdf(tempHtml, pdfFile);
        } catch (Exception e) {
            throw new FileOperationException("Markdown转PDF失败: " + markdownFile.getAbsolutePath(), e);
        } finally {
            if (tempHtml != null && tempHtml.exists()) {
                tempHtml.delete();
            }
        }
    }

    /**
     * Markdown转Word
     *
     * @param markdownFile Markdown文件
     * @param wordFile     Word文件
     */
    public static void markdownToWord(File markdownFile, File wordFile) {
        File tempHtml = null;
        try {
            tempHtml = File.createTempFile("markdown_", ".html");
            markdownToHtml(markdownFile, tempHtml);
            htmlToWord(tempHtml, wordFile);
        } catch (Exception e) {
            throw new FileOperationException("Markdown转Word失败: " + markdownFile.getAbsolutePath(), e);
        } finally {
            if (tempHtml != null && tempHtml.exists()) {
                tempHtml.delete();
            }
        }
    }

    /**
     * HTML转PDF
     *
     * @param htmlFile HTML文件
     * @param pdfFile  PDF文件
     */
    public static void htmlToPdf(File htmlFile, File pdfFile) {
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            String html = FileUtil.readFileToString(htmlFile.getAbsolutePath(), StandardCharsets.UTF_8);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, htmlFile.toURI().toString());
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new FileOperationException("HTML转PDF失败: " + htmlFile.getAbsolutePath(), e);
        }
    }

    /**
     * HTML转Word
     *
     * @param htmlFile HTML文件
     * @param wordFile Word文件
     */
    public static void htmlToWord(File htmlFile, File wordFile) {
        try {
            String html = FileUtil.readFileToString(htmlFile.getAbsolutePath(), StandardCharsets.UTF_8);
            
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
            XHTMLImporterImpl importer = new XHTMLImporterImpl(wordMLPackage);
            wordMLPackage.getMainDocumentPart().getContent().addAll(
                    importer.convert(html, null)
            );

            FileUtil.createFile(wordFile);
            wordMLPackage.save(wordFile);
        } catch (Exception e) {
            throw new FileOperationException("HTML转Word失败: " + htmlFile.getAbsolutePath(), e);
        }
    }

    /**
     * Word转PDF (使用docx4j)
     *
     * @param wordFile Word文件
     * @param pdfFile  PDF文件
     */
    public static void wordToPdf(File wordFile, File pdfFile) {
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile);
            
            FileUtil.createFile(pdfFile);
            try (OutputStream os = new FileOutputStream(pdfFile)) {
                Docx4J.toPDF(wordMLPackage, os);
            }
        } catch (Exception e) {
            throw new FileOperationException("Word转PDF失败: " + wordFile.getAbsolutePath(), e);
        }
    }

    /**
     * Word转HTML
     *
     * @param wordFile Word文件
     * @param htmlFile HTML文件
     */
    public static void wordToHtml(File wordFile, File htmlFile) {
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile);
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setWmlPackage(wordMLPackage);

            StringWriter stringWriter = new StringWriter();
            Docx4J.toHTML(htmlSettings, stringWriter, Docx4J.FLAG_EXPORT_PREFER_XSL);

            FileUtil.writeStringToFile(htmlFile.getAbsolutePath(), stringWriter.toString(), StandardCharsets.UTF_8, false);
        } catch (Exception e) {
            throw new FileOperationException("Word转HTML失败: " + wordFile.getAbsolutePath(), e);
        }
    }

    /**
     * 通用文件格式转换方法
     *
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param targetType 目标格式（pdf、html、docx等）
     */
    public static void convert(File sourceFile, File targetFile, String targetType) {
        String sourceExt = FileUtil.getFileExtension(sourceFile.getName()).toLowerCase();
        String targetExt = targetType.toLowerCase();

        if (sourceExt.equals("xlsx") || sourceExt.equals("xls")) {
            if (targetExt.equals("pdf")) {
                excelToPdf(sourceFile, targetFile);
            } else if (targetExt.equals("html")) {
                excelToHtml(sourceFile, targetFile);
            } else {
                throw new FileOperationException("不支持的转换: " + sourceExt + " -> " + targetExt);
            }
        } else if (sourceExt.equals("md") || sourceExt.equals("markdown")) {
            if (targetExt.equals("pdf")) {
                markdownToPdf(sourceFile, targetFile);
            } else if (targetExt.equals("html")) {
                markdownToHtml(sourceFile, targetFile);
            } else if (targetExt.equals("docx")) {
                markdownToWord(sourceFile, targetFile);
            } else {
                throw new FileOperationException("不支持的转换: " + sourceExt + " -> " + targetExt);
            }
        } else if (sourceExt.equals("docx") || sourceExt.equals("doc")) {
            if (targetExt.equals("pdf")) {
                wordToPdf(sourceFile, targetFile);
            } else if (targetExt.equals("html")) {
                wordToHtml(sourceFile, targetFile);
            } else {
                throw new FileOperationException("不支持的转换: " + sourceExt + " -> " + targetExt);
            }
        } else if (sourceExt.equals("html") || sourceExt.equals("htm")) {
            if (targetExt.equals("pdf")) {
                htmlToPdf(sourceFile, targetFile);
            } else if (targetExt.equals("docx")) {
                htmlToWord(sourceFile, targetFile);
            } else {
                throw new FileOperationException("不支持的转换: " + sourceExt + " -> " + targetExt);
            }
        } else {
            throw new FileOperationException("不支持的源文件格式: " + sourceExt);
        }
    }
}
