package util;

import bean.CanBoCoiThi;
import bean.PhongThiNguon;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// LƯU Ý QUAN TRỌNG:
// File này sử dụng Apache POI để đọc/ghi Excel .xlsx
// Bạn CẦN thêm các JAR sau vào Build Path của Eclipse:
//   - poi-5.x.x.jar
//   - poi-ooxml-5.x.x.jar  
//   - poi-ooxml-lite-5.x.x.jar
//   - commons-collections4-4.x.jar
//   - commons-compress-1.x.jar
//   - commons-io-2.x.jar
//   - xmlbeans-5.x.x.jar
//   - log4j-api-2.x.jar
//   - commons-math3-3.x.jar
//
// Tải tại: https://poi.apache.org/download.html
// ============================================================

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Tiện ích đọc file Excel nguồn (.xlsx)
 * - Đọc sheet "Danh sách cán bộ" → List<CanBoCoiThi>
 * - Đọc sheet "Danh sách phòng thi" → List<PhongThiNguon>
 */
public class ExcelReaderUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Đọc danh sách cán bộ coi thi từ file Excel.
     * Tự động tìm sheet có chứa dữ liệu cán bộ dựa trên header.
     * 
     * Cột kỳ vọng: TT | Mã GV | Họ Tên | Ngày sinh | Đơn vị công tác
     */
    public static List<CanBoCoiThi> readCanBo(String filePath) throws Exception {
        List<CanBoCoiThi> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = findSheetContaining(wb, "Mã GV", "Họ");
            if (sheet == null) {
                // Thử sheet đầu tiên
                sheet = wb.getSheetAt(0);
            }

            // Tìm dòng header
            int headerRow = findHeaderRow(sheet, "Mã GV");
            if (headerRow == -1) {
                throw new Exception("Không tìm thấy dòng tiêu đề chứa 'Mã GV' trong file Excel!");
            }

            // Map cột
            Row header = sheet.getRow(headerRow);
            int colTT = findCol(header, "TT");
            int colMaGV = findCol(header, "Mã GV", "MaGV", "Ma GV");
            int colHoTen = findCol(header, "Họ Tên", "Họ và tên", "Ho Ten", "Họ tên");
            int colNgaySinh = findCol(header, "Ngày sinh", "Ngay sinh", "NS");
            int colDonVi = findCol(header, "Đơn vị", "Đơn vị công tác", "Don vi");

            if (colMaGV == -1 || colHoTen == -1) {
                throw new Exception("Không tìm thấy cột 'Mã GV' hoặc 'Họ Tên' trong file Excel!");
            }

            // Đọc dữ liệu
            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String maGV = getCellString(row, colMaGV).trim();
                String hoTen = getCellString(row, colHoTen).trim();

                // Bỏ qua dòng trống
                if (maGV.isEmpty() && hoTen.isEmpty()) continue;
                if (maGV.isEmpty()) continue; // Mã GV bắt buộc

                CanBoCoiThi cb = new CanBoCoiThi();
                cb.setMaGV(maGV);
                cb.setHoTen(hoTen);
                cb.setSourceRow(i + 1); // 1-indexed cho user

                // TT (có thể không có)
                if (colTT >= 0) {
                    try {
                        cb.setTtNguon((int) row.getCell(colTT).getNumericCellValue());
                    } catch (Exception e) {
                        cb.setTtNguon(result.size() + 1);
                    }
                } else {
                    cb.setTtNguon(result.size() + 1);
                }

                // Ngày sinh
                if (colNgaySinh >= 0) {
                    cb.setNgaySinh(getCellDate(row, colNgaySinh));
                }

                // Đơn vị công tác
                if (colDonVi >= 0) {
                    cb.setDonViCongTac(getCellString(row, colDonVi).trim());
                }

                result.add(cb);
            }
        }

        System.out.println("[ExcelReader] Đọc được " + result.size() + " cán bộ từ file.");
        return result;
    }

    /**
     * Đọc danh sách phòng thi từ file Excel.
     * Cột kỳ vọng: STT | Phòng thi | Ghi chú
     */
    public static List<PhongThiNguon> readPhongThi(String filePath) throws Exception {
        List<PhongThiNguon> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            // Tìm sheet chứa phòng thi (thường là sheet khác với sheet cán bộ)
            Sheet sheet = findSheetContaining(wb, "Phòng thi", "phòng thi");
            if (sheet == null) {
                // Thử sheet cuối cùng hoặc sheet thứ 2
                sheet = wb.getNumberOfSheets() > 1 ? wb.getSheetAt(1) : wb.getSheetAt(0);
            }

            int headerRow = findHeaderRow(sheet, "Phòng thi", "phòng thi", "Phong thi");
            if (headerRow == -1) {
                // Nếu không có sheet phòng thi riêng, tạo danh sách mặc định
                System.out.println("[ExcelReader] Không tìm thấy sheet phòng thi, sẽ dùng danh sách mặc định.");
                return result;
            }

            Row header = sheet.getRow(headerRow);
            int colSTT = findCol(header, "STT", "TT");
            int colPhong = findCol(header, "Phòng thi", "phòng thi", "Phong thi", "Phòng");
            int colGhiChu = findCol(header, "Ghi chú", "Ghi Chú", "ghi chú", "Ghi chu");

            if (colPhong == -1) {
                throw new Exception("Không tìm thấy cột 'Phòng thi' trong file Excel!");
            }

            for (int i = headerRow + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String phongThi = getCellString(row, colPhong).trim();
                if (phongThi.isEmpty()) continue;

                PhongThiNguon pt = new PhongThiNguon();
                pt.setPhongThi(phongThi);
                pt.setSourceRow(i + 1);

                if (colSTT >= 0) {
                    try {
                        pt.setSttNguon((int) row.getCell(colSTT).getNumericCellValue());
                    } catch (Exception e) {
                        pt.setSttNguon(result.size() + 1);
                    }
                } else {
                    pt.setSttNguon(result.size() + 1);
                }

                if (colGhiChu >= 0) {
                    pt.setGhiChu(getCellString(row, colGhiChu).trim());
                }

                result.add(pt);
            }
        }

        System.out.println("[ExcelReader] Đọc được " + result.size() + " phòng thi từ file.");
        return result;
    }

    // ==================== HELPER METHODS ====================

    private static Sheet findSheetContaining(Workbook wb, String... keywords) {
        for (int s = 0; s < wb.getNumberOfSheets(); s++) {
            Sheet sheet = wb.getSheetAt(s);
            // Kiểm tra tên sheet
            String sheetName = sheet.getSheetName().toLowerCase();
            for (String kw : keywords) {
                if (sheetName.contains(kw.toLowerCase())) return sheet;
            }
            // Kiểm tra nội dung 10 dòng đầu
            for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (Cell cell : row) {
                    String val = getCellStringValue(cell).toLowerCase();
                    for (String kw : keywords) {
                        if (val.contains(kw.toLowerCase())) return sheet;
                    }
                }
            }
        }
        return null;
    }

    private static int findHeaderRow(Sheet sheet, String... keywords) {
        for (int i = 0; i <= Math.min(15, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            for (Cell cell : row) {
                String val = getCellStringValue(cell);
                for (String kw : keywords) {
                    if (val.contains(kw)) return i;
                }
            }
        }
        return -1;
    }

    private static int findCol(Row header, String... names) {
        if (header == null) return -1;
        for (Cell cell : header) {
            String val = getCellStringValue(cell).trim();
            for (String name : names) {
                if (val.equalsIgnoreCase(name) || val.contains(name)) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1;
    }

    private static String getCellString(Row row, int col) {
        if (col < 0) return "";
        Cell cell = row.getCell(col);
        return getCellStringValue(cell);
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FMT.format(cell.getDateCellValue());
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return String.valueOf((long) d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); }
                catch (Exception e) {
                    try { return String.valueOf(cell.getNumericCellValue()); }
                    catch (Exception e2) { return ""; }
                }
            default: return "";
        }
    }

    private static Date getCellDate(Row row, int col) {
        if (col < 0) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                java.util.Date d = cell.getDateCellValue();
                return new Date(d.getTime());
            } else if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (!s.isEmpty()) {
                    java.util.Date d = DATE_FMT.parse(s);
                    return new Date(d.getTime());
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
}
