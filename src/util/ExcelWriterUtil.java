package util;

import bean.PhanCongChiTiet;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Tiện ích xuất file Excel kết quả (.xlsx).
 * Tạo 2 file:
 *   1) DANHSACH PHANCONG.XLSX - danh sách giám thị
 *   2) DANHSACH GIAMSAT.XLSX  - danh sách giám sát hành lang
 * 
 * Mỗi sheet tối đa 24 dòng dữ liệu.
 */
public class ExcelWriterUtil {

    private static final int MAX_ROWS_PER_SHEET = 24;

    // Tiêu đề quốc hiệu
    private static final String QUOC_HIEU = "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM";
    private static final String TIEU_NGU  = "Độc Lập - Tự Do - Hạnh Phúc";

    /**
     * Xuất file DANHSACH PHANCONG.XLSX dưới dạng byte[].
     * Cột: STT | Mã GV | Họ và tên | Giám thị 1 | Giám thị 2 | Phòng thi
     */
    public static byte[] writePhanCong(List<PhanCongChiTiet> danhSach) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // Chia thành các sheet, mỗi sheet tối đa MAX_ROWS_PER_SHEET dòng
            int totalSheets = (int) Math.ceil((double) danhSach.size() / MAX_ROWS_PER_SHEET);
            if (totalSheets == 0) totalSheets = 1;

            // Tạo styles
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle subTitleStyle = createSubTitleStyle(wb);
            CellStyle dataStyle = createDataStyle(wb);
            CellStyle centerStyle = createCenterStyle(wb);

            for (int sheetIdx = 0; sheetIdx < totalSheets; sheetIdx++) {
                String sheetName = totalSheets == 1 ? "Phân công" : "Phân công " + (sheetIdx + 1);
                Sheet sheet = wb.createSheet(sheetName);

                // Set column widths
                sheet.setColumnWidth(0, 2000);  // STT
                sheet.setColumnWidth(1, 4000);  // Mã GV
                sheet.setColumnWidth(2, 8000);  // Họ và tên
                sheet.setColumnWidth(3, 3500);  // Giám thị 1
                sheet.setColumnWidth(4, 3500);  // Giám thị 2
                sheet.setColumnWidth(5, 4000);  // Phòng thi

                int rowIdx = 0;

                // Dòng 1: Quốc hiệu
                Row r1 = sheet.createRow(rowIdx++);
                Cell c1 = r1.createCell(0);
                c1.setCellValue(QUOC_HIEU);
                c1.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

                // Dòng 2: Tiêu ngữ
                Row r2 = sheet.createRow(rowIdx++);
                Cell c2 = r2.createCell(0);
                c2.setCellValue(TIEU_NGU);
                c2.setCellStyle(subTitleStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

                // Dòng 3: trống
                rowIdx++;

                // Dòng 4: Tiêu đề bảng
                Row r4 = sheet.createRow(rowIdx++);
                Cell ct = r4.createCell(0);
                ct.setCellValue("DANH SÁCH PHÂN CÔNG CÁN BỘ COI THI");
                ct.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));

                // Dòng 5: trống
                rowIdx++;

                // Dòng 6: Header
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = {"STT", "Mã GV", "Họ và tên", "Giám thị 1", "Giám thị 2", "Phòng thi"};
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // Dữ liệu
                int startIdx = sheetIdx * MAX_ROWS_PER_SHEET;
                int endIdx = Math.min(startIdx + MAX_ROWS_PER_SHEET, danhSach.size());

                for (int i = startIdx; i < endIdx; i++) {
                    PhanCongChiTiet pc = danhSach.get(i);
                    Row row = sheet.createRow(rowIdx++);

                    // STT (format 01, 02...)
                    Cell sttCell = row.createCell(0);
                    sttCell.setCellValue(String.format("%02d", i + 1));
                    sttCell.setCellStyle(centerStyle);

                    // Mã GV
                    Cell mgvCell = row.createCell(1);
                    mgvCell.setCellValue(pc.getMaGV() != null ? pc.getMaGV() : "");
                    mgvCell.setCellStyle(dataStyle);

                    // Họ và tên
                    Cell htCell = row.createCell(2);
                    htCell.setCellValue(pc.getHoTen() != null ? pc.getHoTen() : "");
                    htCell.setCellStyle(dataStyle);

                    // Giám thị 1
                    Cell gt1Cell = row.createCell(3);
                    gt1Cell.setCellValue(PhanCongChiTiet.ROLE_GIAMTHI1.equals(pc.getRole()) ? "X" : "");
                    gt1Cell.setCellStyle(centerStyle);

                    // Giám thị 2
                    Cell gt2Cell = row.createCell(4);
                    gt2Cell.setCellValue(PhanCongChiTiet.ROLE_GIAMTHI2.equals(pc.getRole()) ? "X" : "");
                    gt2Cell.setCellStyle(centerStyle);

                    // Phòng thi
                    Cell ptCell = row.createCell(5);
                    ptCell.setCellValue(pc.getTenPhong() != null ? pc.getTenPhong() : "");
                    ptCell.setCellStyle(centerStyle);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Xuất file DANHSACH GIAMSAT.XLSX dưới dạng byte[].
     * Cột: STT | Mã GV | Họ và tên | Phòng thi được giám sát
     */
    public static byte[] writeGiamSat(List<PhanCongChiTiet> danhSach) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            int totalSheets = (int) Math.ceil((double) danhSach.size() / MAX_ROWS_PER_SHEET);
            if (totalSheets == 0) totalSheets = 1;

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle subTitleStyle = createSubTitleStyle(wb);
            CellStyle dataStyle = createDataStyle(wb);
            CellStyle centerStyle = createCenterStyle(wb);

            for (int sheetIdx = 0; sheetIdx < totalSheets; sheetIdx++) {
                String sheetName = totalSheets == 1 ? "Giám sát" : "Giám sát " + (sheetIdx + 1);
                Sheet sheet = wb.createSheet(sheetName);

                sheet.setColumnWidth(0, 2000);  // STT
                sheet.setColumnWidth(1, 4000);  // Mã GV
                sheet.setColumnWidth(2, 8000);  // Họ và tên
                sheet.setColumnWidth(3, 10000); // Phòng thi được giám sát

                int rowIdx = 0;

                // Quốc hiệu
                Row r1 = sheet.createRow(rowIdx++);
                Cell c1 = r1.createCell(0);
                c1.setCellValue(QUOC_HIEU);
                c1.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

                // Tiêu ngữ
                Row r2 = sheet.createRow(rowIdx++);
                Cell c2 = r2.createCell(0);
                c2.setCellValue(TIEU_NGU);
                c2.setCellStyle(subTitleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

                rowIdx++; // Dòng trống

                // Tiêu đề
                Row r4 = sheet.createRow(rowIdx++);
                Cell ct = r4.createCell(0);
                ct.setCellValue("DANH SÁCH CÁN BỘ GIÁM SÁT HÀNH LANG");
                ct.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

                rowIdx++; // Dòng trống

                // Header
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = {"STT", "Mã GV", "Họ và tên", "Phòng thi được giám sát"};
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // Dữ liệu
                int startIdx = sheetIdx * MAX_ROWS_PER_SHEET;
                int endIdx = Math.min(startIdx + MAX_ROWS_PER_SHEET, danhSach.size());

                for (int i = startIdx; i < endIdx; i++) {
                    PhanCongChiTiet pc = danhSach.get(i);
                    Row row = sheet.createRow(rowIdx++);

                    Cell sttCell = row.createCell(0);
                    sttCell.setCellValue(String.format("%02d", i + 1));
                    sttCell.setCellStyle(centerStyle);

                    Cell mgvCell = row.createCell(1);
                    mgvCell.setCellValue(pc.getMaGV() != null ? pc.getMaGV() : "");
                    mgvCell.setCellStyle(dataStyle);

                    Cell htCell = row.createCell(2);
                    htCell.setCellValue(pc.getHoTen() != null ? pc.getHoTen() : "");
                    htCell.setCellStyle(dataStyle);

                    Cell rangeCell = row.createCell(3);
                    rangeCell.setCellValue(pc.getRangeText() != null ? pc.getRangeText() : "");
                    rangeCell.setCellStyle(dataStyle);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    // ==================== STYLE HELPERS ====================

    private static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createSubTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createCenterStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
