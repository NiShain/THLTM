package client;

import bean.CanBoCoiThi;
import bean.PhanCongChiTiet;
import bean.PhongThiNguon;
import util.ExcelReaderUtil;
import util.ExcelWriterUtil;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PhanCongClient extends JFrame {

    private static final Color BG = new Color(248, 250, 252), // Slate 50
            SURFACE = new Color(255, 255, 255), // White
            CARD = new Color(255, 255, 255), // White
            PRIMARY = new Color(79, 70, 229), // Indigo 600
            PRIMARY_H = new Color(67, 56, 202), // Indigo 700
            SUCCESS = new Color(22, 163, 74), // Green 600
            ERROR = new Color(220, 38, 38), // Red 600
            TXT = new Color(15, 23, 42), // Slate 900
            TXT_M = new Color(71, 85, 105), // Slate 600
            BORDER_C = new Color(226, 232, 240); // Slate 200
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
        setSize(980, 720);
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
                super.paintComponent(g);
                g.setColor(BORDER_C);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        p.setBackground(SURFACE);
        p.setPreferredSize(new Dimension(0, 60));
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        JLabel t = new JLabel("PH\u00c2N C\u00d4NG C\u00c1N B\u1ed9 COI THI  \u2014  CLIENT");
        t.setFont(FONT_H);
        t.setForeground(TXT);
        p.add(t);
        return p;
    }

    private JPanel createCenter() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG);

        // --- LEFT COLUMN: CONFIGURATION PANEL ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BG);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_C),
                new EmptyBorder(12, 15, 12, 15)));
        leftPanel.setPreferredSize(new Dimension(340, 0));

        // 1. File Info Card
        JPanel fileCard = card("T\u1ebfp d\u1eef li\u1ec7u Excel");
        lblFileInfo = new JLabel("\u0110ang t\u00ecm file...");
        lblFileInfo.setFont(FONT);
        lblFileInfo.setForeground(TXT_M);

        JButton btnBrowse = new JButton("Ch\u1ecdn file...");
        btnBrowse.setFont(FONT_B);
        btnBrowse.setForeground(PRIMARY);
        btnBrowse.setBackground(Color.WHITE);
        btnBrowse.setFocusPainted(false);
        btnBrowse.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(6, 12, 6, 12)));
        btnBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBrowse.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnBrowse.setBackground(new Color(241, 245, 249));
                btnBrowse.setBorder(BorderFactory.createLineBorder(PRIMARY));
            }

            public void mouseExited(MouseEvent e) {
                btnBrowse.setBackground(Color.WHITE);
                btnBrowse.setBorder(BorderFactory.createLineBorder(BORDER_C));
            }
        });
        btnBrowse.addActionListener(e -> chooseExcelFile());

        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setOpaque(false);
        fileRow.add(lblFileInfo, BorderLayout.CENTER);
        fileRow.add(btnBrowse, BorderLayout.EAST);
        fileCard.add(fileRow, BorderLayout.CENTER);

        leftPanel.add(fileCard);
        leftPanel.add(Box.createVerticalStrut(10));

        // 2. Parameters Card
        JPanel paramCard = card("Tham s\u1ed1 ph\u00e2n c\u00f4ng");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(createFormField("S\u1ed1 c\u00e1n b\u1ed9 (m)", tfM = styledField(6)));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormField("S\u1ed1 ph\u00f2ng thi (n)", tfN = styledField(6)));
        form.add(Box.createVerticalStrut(10));
        form.add(createFormField("T\u00ean \u0111\u1ee3t thi", tfTenDot = styledField(20)));
        form.add(Box.createVerticalStrut(10));

        JPanel serverPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        serverPanel.setOpaque(false);
        tfHost = styledField(12);
        tfHost.setText(NetworkConfig.SERVER_HOST);
        tfPort = styledField(6);
        tfPort.setText(String.valueOf(NetworkConfig.SERVER_PORT));

        serverPanel.add(createFormField("Server Host", tfHost));
        serverPanel.add(createFormField("Server Port", tfPort));
        form.add(serverPanel);

        form.add(Box.createVerticalStrut(20));
        btnSend = styledButton("G\u1eecI Y\u00caU C\u1ea6U PH\u00c2N C\u00d4NG");
        btnSend.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnSend.getPreferredSize().height));
        btnSend.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSend.addActionListener(e -> onSend());
        form.add(btnSend);

        paramCard.add(form, BorderLayout.CENTER);
        leftPanel.add(paramCard);

        centerPanel.add(leftPanel, BorderLayout.WEST);

        // --- RIGHT COLUMN: PREVIEW & LOGGER PANELS ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BG);
        rightPanel.setBorder(new EmptyBorder(12, 10, 12, 15));

        GridBagConstraints rgc = new GridBagConstraints();
        rgc.gridx = 0;
        rgc.fill = GridBagConstraints.BOTH;
        rgc.weightx = 1.0;

        // 3. Preview JTable Card
        JPanel dataCard = card("D\u1eef li\u1ec7u c\u00e1n b\u1ed9 (preview)");
        tableModel = new DefaultTableModel(
                new String[] { "TT", "M\u00e3 GV", "H\u1edd t\u00ean", "\u0110\u01a1n v\u1ecb c\u00f4ng t\u00e1c" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        dataCard.add(sp, BorderLayout.CENTER);

        rgc.gridy = 0;
        rgc.weighty = 0.55;
        rgc.insets = new Insets(0, 0, 10, 0);
        rightPanel.add(dataCard, rgc);

        // 4. Log console Card
        JPanel logCard = card("Nh\u1eadt k\u00fd ho\u1ea1t \u0111\u1ed9ng");
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(241, 245, 249));
        logArea.setForeground(TXT);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setCaretColor(TXT);
        logArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane lsp = new JScrollPane(logArea);
        lsp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        logCard.add(lsp, BorderLayout.CENTER);

        rgc.gridy = 1;
        rgc.weighty = 0.45;
        rgc.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(logCard, rgc);

        centerPanel.add(rightPanel, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(2, 10, 2, 10)));
        lblStatus = new JLabel("\u25cf  S\u1eb5n s\u00e0ng");
        lblStatus.setForeground(SUCCESS);
        lblStatus.setFont(FONT);
        p.add(lblStatus);
        return p;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(12, 16, 12, 16)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (title != null) {
            JLabel l = new JLabel(title);
            l.setFont(FONT_B);
            l.setForeground(TXT);
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    private JPanel createFormField(String labelText, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_B);
        lbl.setForeground(TXT_M);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        p.add(lbl);
        p.add(field);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

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

    private void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TXT);
        t.setFont(FONT);
        t.setGridColor(BORDER_C);
        t.setRowHeight(26);
        t.setSelectionBackground(PRIMARY);
        t.setSelectionForeground(Color.WHITE);
        JTableHeader h = t.getTableHeader();
        h.setBackground(SURFACE);
        h.setForeground(TXT);
        h.setFont(FONT_B);
        h.setBorder(BorderFactory.createLineBorder(BORDER_C));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t2, v, s, f, r, c);
                if (!s) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
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
            lblFileInfo.setText("Kh\u00f4ng t\u00ecm th\u1ea5y file Excel - Nh\u1ea5p \u0111\u1ec3 ch\u1ecdn file");
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
                    lblFileInfo.setText(String.format("%s  |  C\u00e1n b\u1ed9: %d  |  Ph\u00f2ng: %d",
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
            String tdRaw = tfTenDot.getText().trim();
            String td = tdRaw.isEmpty() ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                    : tdRaw;
            String[] formats = {
                    "JSON_LEN", "BINARY", "JSON_RAW", "JAVA_SER",
                    "BINARY_SIMPLE", "BASIC", "UTF_FIRST",
                    "BINARY_UTF", "BASIC_UTF_LIST", "UTF_FIRST_LIST"
            };
            boolean successTotal = false;

            for (String fmt : formats) {
                try {
                    log("K\u1ebft n\u1ed1i " + host + ":" + fp + " (Format: " + fmt + ")...");
                    try (Socket socket = new Socket(host, fp);
                            OutputStream os = socket.getOutputStream();
                            InputStream is = socket.getInputStream()) {

                        socket.setSoTimeout(NetworkConfig.READ_TIMEOUT);
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

                        // ===== GỬI YÊU CẦU =====
                        if ("BINARY_SIMPLE".equals(fmt)) {
                            dos.writeInt(1); // soCA
                            dos.writeInt(m);
                            dos.writeInt(n);
                            dos.writeUTF(td);
                            dos.flush();
                        } else if ("BASIC".equals(fmt)) {
                            dos.writeInt(m);
                            dos.writeInt(n);
                            dos.writeUTF(td);
                            dos.flush();
                        } else if ("UTF_FIRST".equals(fmt)) {
                            dos.writeUTF(td);
                            dos.writeInt(m);
                            dos.writeInt(n);
                            dos.flush();
                        } else if ("BINARY_UTF".equals(fmt) || "BASIC_UTF_LIST".equals(fmt)
                                || "UTF_FIRST_LIST".equals(fmt)) {
                            if ("BINARY_UTF".equals(fmt)) {
                                dos.writeInt(1);
                                dos.writeInt(m);
                                dos.writeInt(n);
                                dos.writeUTF(td);
                            } else if ("BASIC_UTF_LIST".equals(fmt)) {
                                dos.writeInt(m);
                                dos.writeInt(n);
                                dos.writeUTF(td);
                            } else if ("UTF_FIRST_LIST".equals(fmt)) {
                                dos.writeUTF(td);
                                dos.writeInt(m);
                                dos.writeInt(n);
                            }

                            dos.writeInt(danhSachCanBo.size());
                            for (CanBoCoiThi cb : danhSachCanBo) {
                                dos.writeUTF(cb.getMaGV() != null ? cb.getMaGV() : "");
                                dos.writeUTF(cb.getHoTen() != null ? cb.getHoTen() : "");
                                dos.writeLong(cb.getNgaySinh() != null ? cb.getNgaySinh().getTime() : -1);
                                dos.writeUTF(cb.getDonViCongTac() != null ? cb.getDonViCongTac() : "");
                            }

                            dos.writeInt(danhSachPhong != null ? danhSachPhong.size() : 0);
                            if (danhSachPhong != null) {
                                for (PhongThiNguon pt : danhSachPhong) {
                                    dos.writeUTF(pt.getPhongThi() != null ? pt.getPhongThi() : "");
                                    dos.writeUTF("");
                                }
                            }
                            dos.flush();
                        } else if ("BINARY".equals(fmt)) {
                            dos.writeInt(1); // soCA
                            dos.writeInt(m);
                            dos.writeInt(n);
                            writeStringFlex(dos, td); // tenDot

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
                            // Tạo Map cho JSON / Java Ser
                            Map<String, Object> reqMap = new LinkedHashMap<>();
                            reqMap.put("soGiamThi", m);
                            reqMap.put("soPhongThi", n);
                            reqMap.put("tenDot", td);
                            List<Map<String, Object>> cbList = new ArrayList<>();
                            for (CanBoCoiThi cb : danhSachCanBo) {
                                Map<String, Object> c = new LinkedHashMap<>();
                                c.put("maGV", cb.getMaGV());
                                c.put("hoTen", cb.getHoTen());
                                c.put("donVi", cb.getDonViCongTac());
                                cbList.add(c);
                            }
                            reqMap.put("canBo", cbList);
                            List<String> ptList = new ArrayList<>();
                            if (danhSachPhong != null) {
                                for (PhongThiNguon pt : danhSachPhong) {
                                    ptList.add(pt.getPhongThi());
                                }
                            }
                            reqMap.put("phongThi", ptList);

                            if ("JAVA_SER".equals(fmt)) {
                                ObjectOutputStream oos = new ObjectOutputStream(os);
                                oos.writeObject(reqMap);
                                oos.flush();
                            } else {
                                String jsonStr = JsonHelper.toJson(reqMap);
                                byte[] jsonBytes = jsonStr.getBytes("UTF-8");

                                if ("JSON_LEN".equals(fmt)) {
                                    dos.writeInt(jsonBytes.length);
                                    dos.write(jsonBytes);
                                } else if ("JSON_RAW".equals(fmt)) {
                                    dos.write(jsonBytes);
                                    dos.write('\n'); // Ký hiệu kết thúc cho scanner
                                }
                                dos.flush();
                            }
                        }

                        log("Đã gửi dữ liệu, chờ kết quả...");

                        // ===== NHẬN KẾT QUẢ =====
                        DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
                        Map<String, Object> tempResp = new LinkedHashMap<>();
                        boolean isParsed = false;

                        dis.mark(10 * 1024 * 1024);

                        // --- THỬ 1: Format server của mình (writeBoolean + writeStringFlex) ---
                        try {
                            int firstByte = dis.read(); // 1 byte: writeBoolean
                            if (firstByte == 0 || firstByte == 1) {
                                boolean success = (firstByte == 1);
                                String message = readStringFlex(dis);

                                if (!success && (message == null || message.isEmpty())) {
                                    dis.reset();
                                    throw new Exception("Fallback");
                                }
                                if (!success) {
                                    throw new RuntimeException("SERVER_BUSINESS_ERROR:" + message);
                                }

                                String tenDotResp = readStringFlex(dis);
                                int tongCanBo = dis.readInt();
                                int tongGiamThi = dis.readInt();
                                int tongGiamSat = dis.readInt();
                                int tongPhong = dis.readInt();

                                tempResp.put("success", true);
                                tempResp.put("message", message);
                                tempResp.put("tenDot", tenDotResp);
                                tempResp.put("tongCanBo", tongCanBo);
                                tempResp.put("tongGiamThi", tongGiamThi);
                                tempResp.put("tongGiamSat", tongGiamSat);
                                tempResp.put("tongPhong", tongPhong);

                                int pcCount = dis.readInt();
                                List<Map<String, Object>> phanCongList = new ArrayList<>();
                                for (int i = 0; i < pcCount; i++) {
                                    int stt = dis.readInt();
                                    String maGV = readStringFlex(dis);
                                    String hoTen = readStringFlex(dis);
                                    String role = readStringFlex(dis);
                                    String phong = readStringFlex(dis);
                                    Map<String, Object> pc = new LinkedHashMap<>();
                                    pc.put("stt", stt);
                                    pc.put("maGV", maGV);
                                    pc.put("hoTen", hoTen);
                                    pc.put("role", role);
                                    pc.put("phong", phong);
                                    phanCongList.add(pc);
                                }
                                tempResp.put("phanCong", phanCongList);

                                int gsCount = dis.readInt();
                                List<Map<String, Object>> giamSatList = new ArrayList<>();
                                for (int i = 0; i < gsCount; i++) {
                                    int stt = dis.readInt();
                                    String maGV = readStringFlex(dis);
                                    String hoTen = readStringFlex(dis);
                                    String phamVi = readStringFlex(dis);
                                    Map<String, Object> gs = new LinkedHashMap<>();
                                    gs.put("stt", stt);
                                    gs.put("maGV", maGV);
                                    gs.put("hoTen", hoTen);
                                    gs.put("phamVi", phamVi);
                                    giamSatList.add(gs);
                                }
                                tempResp.put("giamSat", giamSatList);

                                isParsed = true;
                                log("Đã nhận kết quả Binary (server mình): " + pcCount + " GT, " + gsCount + " GS.");
                            } else {
                                dis.reset();
                            }
                        } catch (Exception e1) {
                            if (e1 instanceof RuntimeException && e1.getMessage() != null
                                    && e1.getMessage().startsWith("SERVER_BUSINESS_ERROR:")) {
                                throw e1; // Ném thẳng ra ngoài để dừng loop
                            }
                            try {
                                dis.reset();
                            } catch (Exception ig) {
                            }
                            tempResp.clear();
                        }

                        // --- THỬ 2: Format server khác (int status + int soCA + rooms/halls) ---
                        if (!isParsed) {
                            try {
                                dis.mark(10 * 1024 * 1024);
                                int status = dis.readInt();
                                if (status == 0) {
                                    int soCA = dis.readInt();
                                    tempResp.put("success", true);
                                    tempResp.put("message", "Nhận kết quả từ Server thành công (" + soCA + " ca)");

                                    List<Map<String, Object>> phanCongList = new ArrayList<>();
                                    List<Map<String, Object>> giamSatList = new ArrayList<>();

                                    for (int ca = 0; ca < soCA; ca++) {
                                        String caPrefix = (soCA > 1) ? "Ca " + (ca + 1) + " - " : "";

                                        int numRooms = dis.readInt();
                                        for (int i = 0; i < numRooms; i++) {
                                            int rStt = dis.readInt();
                                            String ma1 = readStringFlex(dis);
                                            String ma2 = readStringFlex(dis);

                                            Map<String, Object> pc1 = new LinkedHashMap<>();
                                            pc1.put("stt", rStt);
                                            pc1.put("maGV", ma1);
                                            pc1.put("role", "GIAMTHI1");
                                            pc1.put("phong", caPrefix + "Phòng " + rStt);
                                            phanCongList.add(pc1);

                                            Map<String, Object> pc2 = new LinkedHashMap<>();
                                            pc2.put("stt", rStt);
                                            pc2.put("maGV", ma2);
                                            pc2.put("role", "GIAMTHI2");
                                            pc2.put("phong", caPrefix + "Phòng " + rStt);
                                            phanCongList.add(pc2);
                                        }

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
                                    tempResp.put("tongPhong", phanCongList.size() / 2 / Math.max(soCA, 1));

                                    isParsed = true;
                                    log("Đã nhận kết quả Binary (server khác): " + soCA + " ca.");
                                } else {
                                    dis.reset();
                                }
                            } catch (Exception e2) {
                                try {
                                    dis.reset();
                                } catch (Exception ig) {
                                }
                                tempResp.clear();
                            }
                        }

                        // --- THỬ 3: JSON ---
                        if (!isParsed) {
                            try {
                                int respLen = dis.readInt();
                                if (respLen > 0 && respLen < 5000000) {
                                    byte[] respBytes = new byte[respLen];
                                    dis.readFully(respBytes);
                                    String jsonResp = new String(respBytes, "UTF-8");
                                    if (jsonResp.trim().startsWith("{")) {
                                        tempResp = JsonHelper.parseObject(jsonResp);
                                        isParsed = true;
                                        log("Đã nhận kết quả dạng JSON.");
                                    }
                                }
                            } catch (Exception e3) {
                                log("Không thể đọc kết quả JSON: " + e3.getMessage());
                            }
                        }

                        if (isParsed) {
                            setStatus("Ho\u00e0n th\u00e0nh", SUCCESS);
                            Map<String, Object> finalResp = tempResp;
                            List<PhanCongChiTiet> dsGiamThi = buildPhanCongList(finalResp);
                            List<PhanCongChiTiet> dsGiamSat = buildGiamSatList(finalResp);
                            String tenDot = td;
                            SwingUtilities.invokeLater(() -> showResultDialog(finalResp, dsGiamThi, dsGiamSat, tenDot));
                            successTotal = true;
                            break; // Dừng vòng lặp vì đã gửi và nhận thành công
                        } else {
                            throw new Exception("Không đọc được dữ liệu trả về hợp lệ");
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof RuntimeException && ex.getMessage() != null
                            && ex.getMessage().startsWith("SERVER_BUSINESS_ERROR:")) {
                        log("Lỗi: Server báo: " + ex.getMessage().replace("SERVER_BUSINESS_ERROR:", ""));
                        setStatus("Lỗi từ server", ERROR);
                        break; // Lỗi logic server, không retry format khác
                    }
                    log("  -> Gửi format " + fmt + " thất bại: " + ex.getMessage());
                }
            } // end for

            if (!successTotal) {
                log("Không thể kết nối hoặc giao tiếp với server.");
                setStatus("Lỗi giao tiếp", ERROR);
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

    // ==================== BUILD & EXPORT ====================

    /**
     * Tạo Map maGV → hoTen từ danh sách cán bộ gốc để tra cứu khi server không gửi
     * tên
     */
    private Map<String, String> buildMaGvToHoTenMap() {
        Map<String, String> map = new HashMap<>();
        if (danhSachCanBo != null) {
            for (CanBoCoiThi cb : danhSachCanBo) {
                if (cb.getMaGV() != null && cb.getHoTen() != null) {
                    map.put(cb.getMaGV(), cb.getHoTen());
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<PhanCongChiTiet> buildPhanCongList(Map<String, Object> resp) {
        Map<String, String> lookup = buildMaGvToHoTenMap();
        List<PhanCongChiTiet> result = new ArrayList<>();
        List<Map<String, Object>> pcList = JsonHelper.getList(resp, "phanCong");
        if (pcList == null)
            return result;
        for (Map<String, Object> pc : pcList) {
            PhanCongChiTiet item = new PhanCongChiTiet();
            String maGV = JsonHelper.getString(pc, "maGV", "");
            item.setMaGV(maGV);
            String hoTen = JsonHelper.getString(pc, "hoTen", "");
            if (hoTen.isEmpty())
                hoTen = lookup.getOrDefault(maGV, "");
            item.setHoTen(hoTen);
            String role = JsonHelper.getString(pc, "role", "").toUpperCase();
            item.setRole(role.contains("1") || role.contains("CHINH") ? PhanCongChiTiet.ROLE_GIAMTHI1
                    : PhanCongChiTiet.ROLE_GIAMTHI2);
            item.setTenPhong(JsonHelper.getString(pc, "phong", ""));
            result.add(item);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PhanCongChiTiet> buildGiamSatList(Map<String, Object> resp) {
        Map<String, String> lookup = buildMaGvToHoTenMap();
        List<PhanCongChiTiet> result = new ArrayList<>();
        List<Map<String, Object>> gsList = JsonHelper.getList(resp, "giamSat");
        if (gsList == null)
            return result;
        for (Map<String, Object> gs : gsList) {
            PhanCongChiTiet item = new PhanCongChiTiet();
            String maGV = JsonHelper.getString(gs, "maGV", "");
            item.setMaGV(maGV);
            String hoTen = JsonHelper.getString(gs, "hoTen", "");
            if (hoTen.isEmpty())
                hoTen = lookup.getOrDefault(maGV, "");
            item.setHoTen(hoTen);
            item.setRole(PhanCongChiTiet.ROLE_GIAMSAT);
            item.setRangeText(JsonHelper.getString(gs, "phamVi", ""));
            result.add(item);
        }
        return result;
    }

    private void exportExcelFiles(List<PhanCongChiTiet> dsGiamThi, List<PhanCongChiTiet> dsGiamSat, String tenDot) {
        new Thread(() -> {
            try {
                // Tên folder: dùng tên đợt thi, đã được set mặc định nếu trống
                String folderName = tenDot.replaceAll("[\\\\/:*?\"<>|]", "_"); // sanitize

                // Tạo thư mục
                File outputDir = new File(folderName);
                if (!outputDir.exists())
                    outputDir.mkdirs();

                // Xuất DANHSACH PHANCONG.XLSX
                byte[] phanCongBytes = ExcelWriterUtil.writePhanCong(dsGiamThi);
                saveFile(phanCongBytes, outputDir.getAbsolutePath(), "DANHSACH PHANCONG.XLSX");

                // Xuất DANHSACH GIAMSAT.XLSX
                byte[] giamSatBytes = ExcelWriterUtil.writeGiamSat(dsGiamSat);
                saveFile(giamSatBytes, outputDir.getAbsolutePath(), "DANHSACH GIAMSAT.XLSX");

                log("\u0110\u00e3 xu\u1ea5t 2 file Excel v\u00e0o: " + outputDir.getAbsolutePath());
                setStatus("\u0110\u00e3 xu\u1ea5t Excel th\u00e0nh c\u00f4ng", SUCCESS);

                // Mở thư mục output
                try {
                    Desktop.getDesktop().open(outputDir);
                } catch (Exception ignore) {
                }

            } catch (Exception e) {
                log("LỖI xuất Excel: " + e.getMessage());
                setStatus("Lỗi xuất Excel", ERROR);
                SwingUtilities
                        .invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi xuất Excel: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void showResultDialog(Map<String, Object> resp,
            List<PhanCongChiTiet> dsGiamThi, List<PhanCongChiTiet> dsGiamSat, String tenDot) {
        JDialog dlg = new JDialog(this, "K\u1ebft qu\u1ea3 - " + JsonHelper.getString(resp, "tenDot", ""),
                true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        info.setBackground(SURFACE);
        info.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C));
        info.add(lbl(JsonHelper.getString(resp, "message", "")));
        info.add(lbl(" | GT: " + JsonHelper.getInt(resp, "tongGiamThi", 0)));
        info.add(lbl(" | GS: " + JsonHelper.getInt(resp, "tongGiamSat", 0)));
        info.add(lbl(" | Ph\u00f2ng: " + JsonHelper.getInt(resp, "tongPhong", 0)));
        dlg.add(info, BorderLayout.NORTH);

        // Lookup map tên từ dữ liệu Excel gốc
        Map<String, String> lookupHoTen = buildMaGvToHoTenMap();

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
                String roleStr = role.contains("1") || role.contains("CHINH") ? "Gi\u00e1m th\u1ecb 1"
                        : "Gi\u00e1m th\u1ecb 2";
                String maGV = JsonHelper.getString(pc, "maGV", "");
                String hoTen = JsonHelper.getString(pc, "hoTen", "");
                if (hoTen.isEmpty())
                    hoTen = lookupHoTen.getOrDefault(maGV, "");

                m1.addRow(new Object[] {
                        JsonHelper.getInt(pc, "stt", 0),
                        maGV, hoTen, roleStr,
                        JsonHelper.getString(pc, "phong", "")
                });
            }
            JTable t1 = new JTable(m1);
            styleTable(t1);
            JScrollPane sp1 = new JScrollPane(t1);
            sp1.getViewport().setBackground(CARD);
            tabs.addTab("Gi\u00e1m th\u1ecb (" + pcList.size() + ")", sp1);
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
                String maGV = JsonHelper.getString(gs, "maGV", "");
                String hoTen = JsonHelper.getString(gs, "hoTen", "");
                if (hoTen.isEmpty())
                    hoTen = lookupHoTen.getOrDefault(maGV, "");
                m2.addRow(new Object[] { JsonHelper.getInt(gs, "stt", 0), maGV,
                        hoTen, JsonHelper.getString(gs, "phamVi", "") });
            }
            JTable t2 = new JTable(m2);
            styleTable(t2);
            JScrollPane sp2 = new JScrollPane(t2);
            sp2.getViewport().setBackground(CARD);
            tabs.addTab("Gi\u00e1m s\u00e1t (" + gsList.size() + ")", sp2);
        }
        dlg.add(tabs, BorderLayout.CENTER);

        // Nút Xuất Excel + Đóng
        JButton btnExport = styledButton("Xu\u1ea5t Excel");
        btnExport.addActionListener(e -> {
            exportExcelFiles(dsGiamThi, dsGiamSat, tenDot);
            btnExport.setEnabled(false);
            btnExport.setText("\u0110\u00e3 xu\u1ea5t th\u00e0nh c\u00f4ng");
        });
        JButton bc = new JButton("\u0110\u00f3ng");
        bc.setFont(FONT_B);
        bc.setForeground(TXT);
        bc.setBackground(Color.WHITE);
        bc.setFocusPainted(false);
        bc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(10, 30, 10, 30)));
        bc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bc.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                bc.setBackground(new Color(241, 245, 249));
            }

            public void mouseExited(MouseEvent e) {
                bc.setBackground(Color.WHITE);
            }
        });
        bc.addActionListener(e -> dlg.dispose());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        bot.setBackground(BG);
        bot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        bot.add(btnExport);
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
        lblFileInfo.setText("\u0110ang t\u1ea3i...");
        lblFileInfo.setForeground(TXT_M);
        new Thread(() -> {
            try {
                danhSachCanBo = ExcelReaderUtil.readCanBo(excelPath);
                danhSachPhong = ExcelReaderUtil.readPhongThi(excelPath);
                SwingUtilities.invokeLater(() -> {
                    lblFileInfo.setText(String.format("%s  |  C\u00e1n b\u1ed9: %d  |  Ph\u00f2ng: %d",
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
                log("\u0110\u1ecdc th\u00e0nh c\u00f4ng: " + danhSachCanBo.size() + " c\u00e1n b\u1ed9, "
                        + danhSachPhong.size() + " ph\u00f2ng");
                setStatus("S\u1eb5n s\u00e0ng", SUCCESS);
            } catch (Exception e) {
                log("L\u1ed6I: " + e.getMessage());
                setStatus("L\u1ed7i", ERROR);
                SwingUtilities.invokeLater(() -> {
                    lblFileInfo.setText("L\u1ed7i: " + e.getMessage());
                    lblFileInfo.setForeground(ERROR);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhanCongClient::new);
    }
}