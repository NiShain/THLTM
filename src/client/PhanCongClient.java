package client;

import bean.CanBoCoiThi;
import bean.PhongThiNguon;
import util.ExcelReaderUtil;
import util.JsonHelper;
import util.NetworkConfig;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PhanCongClient extends JFrame {

    private static final Color BG = new Color(24, 24, 37), SURFACE = new Color(36, 36, 54),
            CARD = new Color(45, 45, 68), PRIMARY = new Color(124, 58, 237),
            PRIMARY_H = new Color(139, 92, 246), SUCCESS = new Color(16, 185, 129),
            ERROR = new Color(239, 68, 68), TXT = new Color(226, 232, 240),
            TXT_M = new Color(148, 163, 184), BORDER_C = new Color(55, 55, 80);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13),
            FONT_B = new Font("Segoe UI", Font.BOLD, 13), FONT_H = new Font("Segoe UI", Font.BOLD, 20);

    private JTextField tfM, tfN, tfTenDot, tfHost, tfPort;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JButton btnSend;
    private JLabel lblStatus, lblFileInfo;
    private List<CanBoCoiThi> danhSachCanBo;
    private List<PhongThiNguon> danhSachPhong;
    private String excelPath;

    public PhanCongClient() {
        super("Ph\u00e2n C\u00f4ng C\u00e1n B\u1ed9 Coi Thi - Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 750);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        setVisible(true);
        SwingUtilities.invokeLater(this::autoLoadExcel);
    }

    private JPanel createHeader() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(88, 28, 135), getWidth(), 0, new Color(59, 7, 100)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setPreferredSize(new Dimension(0, 60));
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        JLabel t = new JLabel("\u26a1  PH\u00c2N C\u00d4NG C\u00c1N B\u1ed8 COI THI  \u2014  CLIENT");
        t.setFont(FONT_H);
        t.setForeground(Color.WHITE);
        p.add(t);
        return p;
    }

    private JScrollPane createCenter() {
        JPanel main = new JPanel();
        main.setBackground(BG);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel fileCard = card("  \uD83D\uDCC2  Th\u00f4ng tin file Excel");
        lblFileInfo = new JLabel("\u0110ang t\u00ecm file...");
        lblFileInfo.setFont(FONT);
        lblFileInfo.setForeground(TXT_M);
        fileCard.add(lblFileInfo);
        main.add(fileCard);
        main.add(Box.createVerticalStrut(8));

        JPanel dataCard = card("  \uD83D\uDCCA  D\u1eef li\u1ec7u c\u00e1n b\u1ed9 (preview)");
        tableModel = new DefaultTableModel(
                new String[] { "TT", "M\u00e3 GV", "H\u1ecd t\u00ean", "\u0110\u01a1n v\u1ecb c\u00f4ng t\u00e1c" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, 160));
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        dataCard.add(sp);
        main.add(dataCard);
        main.add(Box.createVerticalStrut(8));

        JPanel paramCard = card("  \u2699\ufe0f  Tham s\u1ed1 ph\u00e2n c\u00f4ng");
        paramCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.anchor = GridBagConstraints.WEST;
        tfM = styledField(6);
        tfN = styledField(6);
        tfTenDot = styledField(20);
        tfHost = styledField(12);
        tfHost.setText(NetworkConfig.SERVER_HOST);
        tfPort = styledField(6);
        tfPort.setText(String.valueOf(NetworkConfig.SERVER_PORT));
        int row = 0;
        addParam(paramCard, gc, row++, "S\u1ed1 c\u00e1n b\u1ed9 (m):", tfM);
        addParam(paramCard, gc, row++, "S\u1ed1 ph\u00f2ng thi (n):", tfN);
        addParam(paramCard, gc, row++, "T\u00ean \u0111\u1ee3t:", tfTenDot);
        gc.gridy = row;
        gc.gridx = 0;
        gc.fill = GridBagConstraints.NONE;
        paramCard.add(lbl("Server:"), gc);
        JPanel serverP = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        serverP.setOpaque(false);
        serverP.add(tfHost);
        serverP.add(lbl(":"));
        serverP.add(tfPort);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        paramCard.add(serverP, gc);
        row++;
        gc.gridy = row;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(12, 8, 8, 8);
        btnSend = styledButton("\uD83D\uDE80  G\u1eeci PH\u00c2N C\u00d4NG");
        btnSend.addActionListener(e -> onSend());
        paramCard.add(btnSend, gc);
        main.add(paramCard);
        main.add(Box.createVerticalStrut(8));

        JPanel logCard = card("  \uD83D\uDCDD  Nh\u1eadt k\u00fd");
        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 46));
        logArea.setForeground(TXT_M);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setCaretColor(TXT);
        JScrollPane lsp = new JScrollPane(logArea);
        lsp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        logCard.add(lsp);
        main.add(logCard);

        JScrollPane ms = new JScrollPane(main);
        ms.setBorder(null);
        ms.getVerticalScrollBar().setUnitIncrement(16);
        ms.getViewport().setBackground(BG);
        return ms;
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(SURFACE);
        p.setBorder(new EmptyBorder(2, 10, 2, 10));
        lblStatus = new JLabel("\u25cf  S\u1eb5n s\u00e0ng");
        lblStatus.setForeground(SUCCESS);
        lblStatus.setFont(FONT);
        p.add(lblStatus);
        return p;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(10, 12, 10, 12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (title != null) {
            JLabel l = new JLabel(title);
            l.setFont(FONT_B);
            l.setForeground(TXT);
            l.setBorder(new EmptyBorder(0, 0, 6, 0));
            p.add(l);
        }
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(FONT);
        l.setForeground(TXT);
        return l;
    }

    private JTextField styledField(int c) {
        JTextField tf = new JTextField(c);
        tf.setBackground(SURFACE);
        tf.setForeground(TXT);
        tf.setCaretColor(TXT);
        tf.setFont(FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private JButton styledButton(String t) {
        JButton b = new JButton(t);
        b.setFont(FONT_B);
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 30, 10, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(PRIMARY_H);
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(PRIMARY);
            }
        });
        return b;
    }

    private void addParam(JPanel p, GridBagConstraints gc, int row, String label, JTextField tf) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.gridwidth = 1;
        p.add(lbl(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(tf, gc);
    }

    private void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TXT);
        t.setFont(FONT);
        t.setGridColor(BORDER_C);
        t.setRowHeight(26);
        t.setSelectionBackground(PRIMARY);
        JTableHeader h = t.getTableHeader();
        h.setBackground(SURFACE);
        h.setForeground(TXT);
        h.setFont(FONT_B);
        h.setBorder(BorderFactory.createLineBorder(BORDER_C));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t2, v, s, f, r, c);
                if (!s) {
                    comp.setBackground(r % 2 == 0 ? CARD : SURFACE);
                    comp.setForeground(TXT);
                }
                return comp;
            }
        });
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setStatus(String msg, Color c) {
        SwingUtilities.invokeLater(() -> {
            lblStatus.setText("\u25cf  " + msg);
            lblStatus.setForeground(c);
        });
    }

    private void autoLoadExcel() {
        File found = null;
        
        // Danh sách các đường dẫn ứng cử để tìm file Excel
        String[] pathCandidates = { 
            "docs/Danh sach can bo coi thi.xlsx",
            "ThiTHLTM/docs/Danh sach can bo coi thi.xlsx",
            "src/../docs/Danh sach can bo coi thi.xlsx"
        };
        
        // 1. Thử các đường dẫn tương đối
        for (String c : pathCandidates) {
            File f = new File(c);
            if (f.exists()) {
                found = f;
                break;
            }
        }
        
        // 2. Thử tìm trong folder docs hiện tại
        if (found == null) {
            File d = new File("docs");
            if (d.exists() && d.isDirectory()) {
                File[] files = d.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".xlsx")) {
                            found = f;
                            break;
                        }
                    }
                }
            }
        }
        
        // 3. Thử tìm folder docs ở thư mục cha
        if (found == null) {
            File d = new File("../ThiTHLTM/docs");
            if (d.exists() && d.isDirectory()) {
                File[] files = d.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".xlsx")) {
                            found = f;
                            break;
                        }
                    }
                }
            }
        }
        
        // 4. Nếu vẫn không tìm thấy, cho phép người dùng chọn file
        if (found == null) {
            lblFileInfo.setText("\u274c Kh\u00f4ng t\u00ecm th\u1ea5y file Excel - Nhấp để chọn file");
            lblFileInfo.setForeground(ERROR);
            lblFileInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblFileInfo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    chooseExcelFile();
                }
            });
            return;
        }
        excelPath = found.getAbsolutePath();
        File ff = found;
        new Thread(() -> {
            try {
                danhSachCanBo = ExcelReaderUtil.readCanBo(excelPath);
                danhSachPhong = ExcelReaderUtil.readPhongThi(excelPath);
                SwingUtilities.invokeLater(() -> {
                    lblFileInfo.setText(String.format("\u2705  %s  |  C\u00e1n b\u1ed9: %d  |  Ph\u00f2ng: %d",
                            ff.getName(), danhSachCanBo.size(), danhSachPhong.size()));
                    lblFileInfo.setForeground(SUCCESS);
                    tableModel.setRowCount(0);
                    for (int i = 0; i < Math.min(10, danhSachCanBo.size()); i++) {
                        CanBoCoiThi cb = danhSachCanBo.get(i);
                        tableModel.addRow(new Object[] { cb.getTtNguon(), cb.getMaGV(), cb.getHoTen(),
                                cb.getDonViCongTac() != null ? cb.getDonViCongTac() : "" });
                    }
                    if (danhSachCanBo.size() > 10)
                        tableModel.addRow(new Object[] { "...", "...", "...", "..." });
                });
                log("\u0110\u1ecdc th\u00e0nh c\u00f4ng: " + danhSachCanBo.size() + " c\u00e1n b\u1ed9, "
                        + danhSachPhong.size() + " ph\u00f2ng");
                setStatus("S\u1eb5n s\u00e0ng", SUCCESS);
            } catch (Exception e) {
                log("L\u1ed6I: " + e.getMessage());
                setStatus("L\u1ed7i", ERROR);
            }
        }).start();
    }

    private void onSend() {
        if (danhSachCanBo == null || danhSachCanBo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ch\u01b0a \u0111\u1ecdc d\u1eef li\u1ec7u!", "L\u1ed7i",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int m, n;
        try {
            m = Integer.parseInt(tfM.getText().trim());
            n = Integer.parseInt(tfN.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "m, n ph\u1ea3i l\u00e0 s\u1ed1!", "L\u1ed7i",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m <= 0 || n <= 0) {
            JOptionPane.showMessageDialog(this, "m, n ph\u1ea3i > 0!", "L\u1ed7i", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m < 2 * n) {
            JOptionPane.showMessageDialog(this, "m ph\u1ea3i >= 2*n (" + (2 * n) + ")", "L\u1ed7i",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m > danhSachCanBo.size()) {
            JOptionPane.showMessageDialog(this,
                    "m v\u01b0\u1ee3t qu\u00e1 s\u1ed1 c\u00e1n b\u1ed9 (" + danhSachCanBo.size() + ")", "L\u1ed7i",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        btnSend.setEnabled(false);
        setStatus("\u0110ang g\u1eedi...", TXT_M);
        String host = tfHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(tfPort.getText().trim());
        } catch (Exception e) {
            port = NetworkConfig.SERVER_PORT;
        }
        int fp = port;

        new Thread(() -> {
            // Ưu tiên Binary vì Server mới (mạng khác) đang sử dụng chuẩn này để tối ưu tốc độ
            boolean[] protocols = { true, false }; 

            for (boolean isBinary : protocols) {
                try {
                    // Build JSON request
                    Map<String, Object> req = new LinkedHashMap<>();
                    req.put("soGiamThi", m);
                    req.put("soPhongThi", n);
                    String td = tfTenDot.getText().trim();
                    req.put("tenDot", td.isEmpty() ? null : td);
                    List<Map<String, Object>> canBoList = new ArrayList<>();
                    for (CanBoCoiThi cb : danhSachCanBo) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("maGV", cb.getMaGV());
                        item.put("hoTen", cb.getHoTen());
                        item.put("donVi", cb.getDonViCongTac() != null ? cb.getDonViCongTac() : "");
                        canBoList.add(item);
                    }
                    req.put("canBo", canBoList);
                    List<String> phongList = new ArrayList<>();
                    if (danhSachPhong != null)
                        for (PhongThiNguon pt : danhSachPhong)
                            phongList.add(pt.getPhongThi());
                    req.put("phongThi", phongList);
                    String jsonReq = JsonHelper.toJson(req);

                    log("K\u1ebft n\u1ed1i " + host + ":" + fp + " (Thử chuẩn " + (isBinary ? "Binary" : "JSON")
                            + ")...");
                    try (Socket socket = new Socket(host, fp);
                            DataOutputStream dos = new DataOutputStream(
                                    new BufferedOutputStream(socket.getOutputStream()));
                            DataInputStream dis = new DataInputStream(
                                    new BufferedInputStream(socket.getInputStream()))) {
                        socket.setSoTimeout(NetworkConfig.READ_TIMEOUT);

                        if (isBinary) {
                            // ===== GỬI YÊU CẦU BINARY =====
                            dos.writeInt(1); // soCA
                            dos.writeInt(m);
                            dos.writeInt(n);
                            writeStringFlex(dos, td.isEmpty() ? "Ca1" : td); // tenDot

                            dos.writeInt(danhSachCanBo.size()); // soCanBo
                            for (int i = 0; i < danhSachCanBo.size(); i++) {
                                CanBoCoiThi cb = danhSachCanBo.get(i);
                                dos.writeInt(i + 1);
                                writeStringFlex(dos, cb.getMaGV());
                                writeStringFlex(dos, cb.getHoTen());
                                dos.writeLong(cb.getNgaySinh() != null ? cb.getNgaySinh().getTime() : -1);
                                writeStringFlex(dos, cb.getDonViCongTac() != null ? cb.getDonViCongTac() : "");
                            }

                            dos.writeInt(danhSachPhong != null ? danhSachPhong.size() : 0); // soPhong
                            if (danhSachPhong != null) {
                                for (int i = 0; i < danhSachPhong.size(); i++) {
                                    dos.writeInt(i + 1);
                                    writeStringFlex(dos, danhSachPhong.get(i).getPhongThi());
                                    writeStringFlex(dos, ""); // diaDiem
                                }
                            }
                            dos.flush();
                        } else {
                            // ===== GỬI YÊU CẦU JSON =====
                            byte[] reqBytes = jsonReq.getBytes("UTF-8");
                            dos.writeInt(reqBytes.length);
                            dos.write(reqBytes);
                            dos.flush();
                        }

                        log("Ch\u1edd k\u1ebft qu\u1ea3...");

                        Map<String, Object> tempResp = new LinkedHashMap<>();
                        dis.mark(10 * 1024 * 1024); // Đánh dấu stream (max 10MB)
                        boolean isParsed = false;

                        // 1. THỬ ĐỌC THEO CHUẨN JSON (Kèm File nếu có)
                        try {
                            int respLen = dis.readInt();
                            // Nếu là chuỗi JSON, độ dài thường vài KB đến vài MB
                            if (respLen > 0 && respLen < 5000000) {
                                byte[] respBytes = new byte[respLen];
                                dis.readFully(respBytes);
                                String jsonResp = new String(respBytes, "UTF-8");
                                if (jsonResp.trim().startsWith("{")) {
                                    tempResp = JsonHelper.parseObject(jsonResp);
                                    isParsed = true;
                                    log("Đã nhận kết quả trả về dạng JSON.");

                                    // Đọc vét file nếu server có gửi (để khỏi kẹt)
                                    try {
                                        socket.setSoTimeout(1000);
                                        if (dis.available() > 0 || true) {
                                            int l1 = dis.readInt();
                                            if (l1 > 0)
                                                dis.skipBytes(l1);
                                            int l2 = dis.readInt();
                                            if (l2 > 0)
                                                dis.skipBytes(l2);
                                        }
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                        } catch (Exception ignore) {
                        }

                        // 2. NẾU KHÔNG PHẢI JSON, THỬ ĐỌC THEO CHUẨN BINARY
                        if (!isParsed) {
                            try {
                                // Đọc theo chuẩn Binary của Project hiện tại (Cấu trúc phân tầng)
                                int status = dis.readInt();
                                if (status != 0) {
                                    throw new Exception("Server báo lỗi xử lý (status=" + status + ")");
                                }

                                int soCA = dis.readInt();
                                tempResp.put("success", true);
                                tempResp.put("message", "Nhận kết quả từ Server thành công (" + soCA + " ca)");
                                
                                List<Map<String, Object>> phanCongList = new ArrayList<>();
                                List<Map<String, Object>> giamSatList = new ArrayList<>();

                                for (int ca = 0; ca < soCA; ca++) {
                                    String caPrefix = (soCA > 1) ? "Ca " + (ca + 1) + " - " : "";
                                    
                                    // 1. Đọc Phân công phòng
                                    int numRooms = dis.readInt();
                                    for (int i = 0; i < numRooms; i++) {
                                        int rStt = dis.readInt();
                                        String ma1 = readStringFlex(dis);
                                        String ma2 = readStringFlex(dis);
                                        
                                        // GT1
                                        Map<String, Object> pc1 = new LinkedHashMap<>();
                                        pc1.put("stt", rStt);
                                        pc1.put("maGV", ma1);
                                        pc1.put("role", "GIAMTHI1");
                                        pc1.put("phong", caPrefix + "Phòng " + rStt);
                                        phanCongList.add(pc1);
                                        
                                        // GT2
                                        Map<String, Object> pc2 = new LinkedHashMap<>();
                                        pc2.put("stt", rStt);
                                        pc2.put("maGV", ma2);
                                        pc2.put("role", "GIAMTHI2");
                                        pc2.put("phong", caPrefix + "Phòng " + rStt);
                                        phanCongList.add(pc2);
                                    }

                                    // 2. Đọc Giám sát hành lang
                                    int numHalls = dis.readInt();
                                    for (int i = 0; i < numHalls; i++) {
                                        String label = readStringFlex(dis);
                                        int numMon = dis.readInt();
                                        for (int j = 0; j < numMon; j++) {
                                            String ma = readStringFlex(dis);
                                            Map<String, Object> gs = new LinkedHashMap<>();
                                            gs.put("stt", i + 1);
                                            gs.put("maGV", ma);
                                            gs.put("phamVi", caPrefix + label);
                                            giamSatList.add(gs);
                                        }
                                    }
                                }
                                
                                tempResp.put("phanCong", phanCongList);
                                tempResp.put("giamSat", giamSatList);
                                tempResp.put("tongGiamThi", phanCongList.size());
                                tempResp.put("tongGiamSat", giamSatList.size());
                                tempResp.put("tongPhong", phanCongList.size() / 2 / soCA);

                                isParsed = true;
                                log("Đã nhận kết quả dạng Binary (Cấu trúc " + soCA + " ca).");
                            } catch (Exception ex) {
                                // Bỏ qua nếu lỗi đọc Binary
                            }
                        }

                        if (isParsed) {
                            setStatus("Ho\u00e0n th\u00e0nh", SUCCESS);
                            Map<String, Object> finalResp = tempResp;
                            SwingUtilities.invokeLater(() -> showResultDialog(finalResp));
                            break; // Thành công thì thoát loop protocol
                        }
                    } catch (Exception ex) {
                        log("L\u1ed7i k\u1ebft n\u1ed1i (" + (isBinary ? "Binary" : "JSON") + "): " + ex.getMessage());
                        if (isBinary) {
                            setStatus("L\u1ed7i k\u1ebft n\u1ed1i", ERROR);
                        }
                    }
                } catch (Exception e) {
                    log("L\u1ed7i: " + e.getMessage());
                }
            }
            SwingUtilities.invokeLater(() -> btnSend.setEnabled(true));
        }).start();
    }

    private void saveFile(byte[] data, String dir, String name) throws IOException {
        if (data == null || data.length == 0)
            return;
        File f = new File(dir, name);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(data);
        }
        log("  \u0110\u00e3 l\u01b0u: " + f.getName() + " (" + data.length + " bytes)");
    }

    private String readStringFlex(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if (len <= 0)
            return "";
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    private void writeStringFlex(DataOutputStream dos, String str) throws IOException {
        if (str == null)
            str = "";
        byte[] bytes = str.getBytes("UTF-8");
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    @SuppressWarnings("unchecked")
    private void showResultDialog(Map<String, Object> resp) {
        JDialog dlg = new JDialog(this, "\uD83D\uDCCB K\u1ebft qu\u1ea3 - " + JsonHelper.getString(resp, "tenDot", ""),
                true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        info.setBackground(SURFACE);
        info.add(lbl("\u2705 " + JsonHelper.getString(resp, "message", "")));
        info.add(lbl(" | GT: " + JsonHelper.getInt(resp, "tongGiamThi", 0)));
        info.add(lbl(" | GS: " + JsonHelper.getInt(resp, "tongGiamSat", 0)));
        info.add(lbl(" | Ph\u00f2ng: " + JsonHelper.getInt(resp, "tongPhong", 0)));
        dlg.add(info, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CARD);
        tabs.setForeground(TXT);
        tabs.setFont(FONT_B);

        List<Map<String, Object>> pcList = JsonHelper.getList(resp, "phanCong");
        if (pcList != null && !pcList.isEmpty()) {
            DefaultTableModel m1 = new DefaultTableModel(
                    new String[] { "STT", "M\u00e3 GV", "H\u1ecd t\u00ean", "Vai tr\u00f2", "Ph\u00f2ng" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (Map<String, Object> pc : pcList) {
                String role = JsonHelper.getString(pc, "role", "").toUpperCase();
                String roleStr = role.contains("1") || role.contains("CHINH") ? "Giám thị 1" : "Giám thị 2";
                
                m1.addRow(new Object[] { 
                    JsonHelper.getInt(pc, "stt", 0), 
                    JsonHelper.getString(pc, "maGV", ""),
                    JsonHelper.getString(pc, "hoTen", ""), 
                    roleStr, 
                    JsonHelper.getString(pc, "phong", "") 
                });
            }
            JTable t1 = new JTable(m1);
            styleTable(t1);
            JScrollPane sp1 = new JScrollPane(t1);
            sp1.getViewport().setBackground(CARD);
            tabs.addTab("\uD83D\uDC64 Gi\u00e1m th\u1ecb (" + pcList.size() + ")", sp1);
        }
        List<Map<String, Object>> gsList = JsonHelper.getList(resp, "giamSat");
        if (gsList != null && !gsList.isEmpty()) {
            DefaultTableModel m2 = new DefaultTableModel(
                    new String[] { "STT", "M\u00e3 GV", "H\u1ecd t\u00ean", "Ph\u1ea1m vi" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (Map<String, Object> gs : gsList) {
                m2.addRow(new Object[] { JsonHelper.getInt(gs, "stt", 0), JsonHelper.getString(gs, "maGV", ""),
                        JsonHelper.getString(gs, "hoTen", ""), JsonHelper.getString(gs, "phamVi", "") });
            }
            JTable t2 = new JTable(m2);
            styleTable(t2);
            JScrollPane sp2 = new JScrollPane(t2);
            sp2.getViewport().setBackground(CARD);
            tabs.addTab("\uD83D\uDC41 Gi\u00e1m s\u00e1t (" + gsList.size() + ")", sp2);
        }
        dlg.add(tabs, BorderLayout.CENTER);

        JButton bc = styledButton("\u0110\u00f3ng");
        bc.addActionListener(e -> dlg.dispose());
        JPanel bot = new JPanel();
        bot.setBackground(BG);
        bot.add(bc);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void chooseExcelFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel Files (*.xlsx)";
            }
        });
        
        // Đặt thư mục mặc định
        File docsDir = new File("docs");
        if (docsDir.exists() && docsDir.isDirectory()) {
            fc.setCurrentDirectory(docsDir);
        }
        
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            if (selected != null && selected.exists()) {
                excelPath = selected.getAbsolutePath();
                loadExcelData(selected);
            }
        }
    }

    private void loadExcelData(File file) {
        lblFileInfo.setText("\u23f3 Đang tải...");
        lblFileInfo.setForeground(TXT_M);
        new Thread(() -> {
            try {
                danhSachCanBo = ExcelReaderUtil.readCanBo(excelPath);
                danhSachPhong = ExcelReaderUtil.readPhongThi(excelPath);
                SwingUtilities.invokeLater(() -> {
                    lblFileInfo.setText(String.format("\u2705  %s  |  Cán bộ: %d  |  Phòng: %d",
                            file.getName(), danhSachCanBo.size(), danhSachPhong.size()));
                    lblFileInfo.setForeground(SUCCESS);
                    tableModel.setRowCount(0);
                    for (int i = 0; i < Math.min(10, danhSachCanBo.size()); i++) {
                        CanBoCoiThi cb = danhSachCanBo.get(i);
                        tableModel.addRow(new Object[] { cb.getTtNguon(), cb.getMaGV(), cb.getHoTen(),
                                cb.getDonViCongTac() != null ? cb.getDonViCongTac() : "" });
                    }
                    if (danhSachCanBo.size() > 10)
                        tableModel.addRow(new Object[] { "...", "...", "...", "..." });
                });
                log("Đọc thành công: " + danhSachCanBo.size() + " cán bộ, " + danhSachPhong.size() + " phòng");
                setStatus("Sẵn sàng", SUCCESS);
            } catch (Exception e) {
                log("LỖI: " + e.getMessage());
                setStatus("Lỗi", ERROR);
                SwingUtilities.invokeLater(() -> {
                    lblFileInfo.setText("\u274c Lỗi: " + e.getMessage());
                    lblFileInfo.setForeground(ERROR);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhanCongClient::new);
    }
}