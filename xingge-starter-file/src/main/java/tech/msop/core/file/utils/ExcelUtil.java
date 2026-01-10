package tech.msop.core.file.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tech.msop.core.file.exception.FileOperationException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel操作工具类
 * 提供Excel文件读取、写入、模板填充、添加图片等功能
 *
 * @author ruozhuliufeng
 */
public class ExcelUtil {

    private ExcelUtil() {
    }

    /**
     * 读取Excel文件为二维列表
     *
     * @param excelFile Excel文件
     * @return 二维列表，每个元素代表一行，每行包含多个单元格值
     */
    public static List<List<String>> readExcel(File excelFile) {
        return readExcel(excelFile, 0);
    }

    /**
     * 读取Excel指定Sheet为二维列表
     *
     * @param excelFile  Excel文件
     * @param sheetIndex Sheet索引（从0开始）
     * @return 二维列表
     */
    public static List<List<String>> readExcel(File excelFile, int sheetIndex) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {
            return readSheet(workbook, sheetIndex);
        } catch (Exception e) {
            throw new FileOperationException("读取Excel文件失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * 读取Excel所有Sheet
     *
     * @param excelFile Excel文件
     * @return Map，key为Sheet名称，value为二维列表
     */
    public static Map<String, List<List<String>>> readAllSheets(File excelFile) {
        Map<String, List<List<String>>> result = new HashMap<>();
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                result.put(sheet.getSheetName(), readSheet(workbook, i));
            }
            return result;
        } catch (Exception e) {
            throw new FileOperationException("读取Excel文件失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * 读取Sheet内容
     */
    private static List<List<String>> readSheet(Workbook workbook, int sheetIndex) {
        List<List<String>> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            return result;
        }

        DataFormatter dataFormatter = new DataFormatter();
        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (Cell cell : row) {
                rowData.add(dataFormatter.formatCellValue(cell));
            }
            result.add(rowData);
        }
        return result;
    }

    /**
     * 创建简单的Excel文件
     *
     * @param destFile 目标文件
     * @param data     数据（二维列表）
     */
    public static void createExcel(File destFile, List<List<String>> data) {
        createExcel(destFile, data, "Sheet1");
    }

    /**
     * 创建简单的Excel文件
     *
     * @param destFile  目标文件
     * @param data      数据（二维列表）
     * @param sheetName Sheet名称
     */
    public static void createExcel(File destFile, List<List<String>> data, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                List<String> rowData = data.get(rowIndex);
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(rowData.get(colIndex));
                }
            }

            FileUtil.createFile(destFile);
            try (OutputStream os = new FileOutputStream(destFile)) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new FileOperationException("创建Excel文件失败: " + destFile.getAbsolutePath(), e);
        }
    }

    /**
     * 创建带样式的Excel文件
     *
     * @param destFile   目标文件
     * @param data       数据（二维列表）
     * @param headers    表头
     * @param sheetName  Sheet名称
     */
    public static void createExcelWithStyle(File destFile, List<List<String>> data, List<String> headers, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行
            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                List<String> rowData = data.get(rowIndex);
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(rowData.get(colIndex));
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            FileUtil.createFile(destFile);
            try (OutputStream os = new FileOutputStream(destFile)) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new FileOperationException("创建Excel文件失败: " + destFile.getAbsolutePath(), e);
        }
    }

    /**
     * 向Excel添加图片（如盖章）
     *
     * @param excelFile   Excel文件
     * @param imageFile   图片文件
     * @param sheetIndex  Sheet索引
     * @param row1        起始行
     * @param col1        起始列
     * @param row2        结束行
     * @param col2        结束列
     */
    public static void addImageToExcel(File excelFile, File imageFile, int sheetIndex, int row1, int col1, int row2, int col2) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            
            // 读取图片
            byte[] imageBytes = FileUtil.readFileToByteArray(imageFile);
            int pictureIndex = workbook.addPicture(imageBytes, getPictureType(imageFile.getName()));

            // 创建绘图对象
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            
            // 设置图片位置
            anchor.setCol1(col1);
            anchor.setRow1(row1);
            anchor.setCol2(col2);
            anchor.setRow2(row2);

            // 插入图片
            Picture picture = drawing.createPicture(anchor, pictureIndex);
            picture.resize();

            // 保存修改
            try (OutputStream os = new FileOutputStream(excelFile)) {
                workbook.write(os);
            }
        } catch (Exception e) {
            throw new FileOperationException("向Excel添加图片失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * 模板填充（简单的键值对替换）
     *
     * @param templateFile 模板文件
     * @param destFile     目标文件
     * @param data         数据映射（key为占位符，value为实际值）
     */
    public static void fillTemplate(File templateFile, File destFile, Map<String, String> data) {
        try (InputStream is = new FileInputStream(templateFile);
             Workbook workbook = WorkbookFactory.create(is)) {

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING) {
                            String cellValue = cell.getStringCellValue();
                            for (Map.Entry<String, String> entry : data.entrySet()) {
                                String placeholder = "${" + entry.getKey() + "}";
                                if (cellValue.contains(placeholder)) {
                                    cellValue = cellValue.replace(placeholder, entry.getValue());
                                    cell.setCellValue(cellValue);
                                }
                            }
                        }
                    }
                }
            }

            FileUtil.createFile(destFile);
            try (OutputStream os = new FileOutputStream(destFile)) {
                workbook.write(os);
            }
        } catch (Exception e) {
            throw new FileOperationException("填充Excel模板失败: " + templateFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取图片类型
     */
    private static int getPictureType(String fileName) {
        String extension = FileUtil.getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return Workbook.PICTURE_TYPE_JPEG;
            case "png":
                return Workbook.PICTURE_TYPE_PNG;
            case "emf":
                return Workbook.PICTURE_TYPE_EMF;
            case "wmf":
                return Workbook.PICTURE_TYPE_WMF;
            case "pict":
                return Workbook.PICTURE_TYPE_PICT;
            case "dib":
                return Workbook.PICTURE_TYPE_DIB;
            default:
                return Workbook.PICTURE_TYPE_PNG;
        }
    }

    /**
     * 合并单元格
     *
     * @param excelFile  Excel文件
     * @param sheetIndex Sheet索引
     * @param firstRow   起始行
     * @param lastRow    结束行
     * @param firstCol   起始列
     * @param lastCol    结束列
     */
    public static void mergeCells(File excelFile, int sheetIndex, int firstRow, int lastRow, int firstCol, int lastCol) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
            sheet.addMergedRegion(cellRangeAddress);

            try (OutputStream os = new FileOutputStream(excelFile)) {
                workbook.write(os);
            }
        } catch (Exception e) {
            throw new FileOperationException("合并Excel单元格失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取Sheet数量
     *
     * @param excelFile Excel文件
     * @return Sheet数量
     */
    public static int getSheetCount(File excelFile) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {
            return workbook.getNumberOfSheets();
        } catch (Exception e) {
            throw new FileOperationException("获取Sheet数量失败: " + excelFile.getAbsolutePath(), e);
        }
    }

    /**
     * 获取指定Sheet的行数
     *
     * @param excelFile  Excel文件
     * @param sheetIndex Sheet索引
     * @return 行数
     */
    public static int getRowCount(File excelFile, int sheetIndex) {
        try (InputStream is = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            return sheet.getLastRowNum() + 1;
        } catch (Exception e) {
            throw new FileOperationException("获取Sheet行数失败: " + excelFile.getAbsolutePath(), e);
        }
    }
}
