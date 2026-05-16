package server;

import bo.PhanCongBO;
import util.JsonHelper;
import util.NetworkConfig;
import util.DatabaseUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Server phân công cán bộ coi thi.
 * 
 * GIAO THỨC (JSON text qua DataInputStream/DataOutputStream):
 * Client → Server: 1 chuỗi JSON (writeUTF) chứa request
 * Server → Client: 1 chuỗi JSON (writeUTF) chứa response
 * Nếu thành công, server gửi tiếp 2 file Excel (binary):
 * - writeInt(length) + write(bytes) cho DANHSACH PHANCONG.XLSX
 * - writeInt(length) + write(bytes) cho DANHSACH GIAMSAT.XLSX
 * Nếu thất bại, server gửi writeInt(0) + writeInt(0)
 */
public class PhanCongServer extends JFrame {

    // === THEME ===
    private static final Color BG = new Color(24, 24, 37);
    private static final Color SURFACE = new Color(36, 36, 54);
    private static final Color CARD = new Color(45, 45, 68);
    private static final Color PRIMARY = new Color(16, 185, 129);
    private static final Color STOP_CLR = new Color(239, 68, 68);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color ERROR = new Color(239, 68, 68);
    private static final Color WARN = new Color(245, 158, 11);
    private static final Color TXT = new Color(226, 232, 240);
    private static final Color TXT_M = new Color(148, 163, 184);
    private static final Color BORDER_C = new Color(55, 55, 80);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_B = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_H = new Font("Segoe UI", Font.BOLD, 20);

    private JTextArea logArea;
    private JButton btnStart, btnStop;
    private JLabel lblServerStatus, lblDbStatus, lblStats, lblStatus;
    private JTextField tfPort;

    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private Thread serverThread;
    private int requestCount = 0, clientCount = 0;
    private final PhanCongBO bo = new PhanCongBO();

    public PhanCongServer() {
        super("Ph\u00E2n C\u00F4ng C\u00E1n B\u1ED9 Coi Thi - Server");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (running)
                    stopServer();
                dispose();
                System.exit(0);
            }
        });
        setSize(750, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
        checkDatabase();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(6, 78, 59), getWidth(), 0, new Color(4, 47, 46)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setPreferredSize(new Dimension(0, 60));
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        JLabel t = new JLabel("\uD83D\uDDA5\uFE0F  PH\u00C2N C\u00D4NG C\u00C1N B\u1ED8 COI THI  \u2014  SERVER");
        t.setFont(FONT_H);
        t.setForeground(Color.WHITE);
        p.add(t);
        return p;
    }

    private JPanel createCenter() {
        JPanel main = new JPanel();
        main.setBackground(BG);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Status card
        JPanel statusCard = card("  \uD83D\uDCE1  Tr\u1EA1ng th\u00E1i");
        statusCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(3, 8, 3, 15);

        lblServerStatus = new JLabel("\u25CF  \u0110\u00E3 d\u1EEBng");
        lblServerStatus.setFont(FONT_B);
        lblServerStatus.setForeground(WARN);
        gc.gridx = 0;
        gc.gridy = 0;
        statusCard.add(lblServerStatus, gc);

        lblDbStatus = new JLabel("\uD83D\uDDC4\uFE0F  Database: \u0110ang ki\u1EC3m tra...");
        lblDbStatus.setFont(FONT);
        lblDbStatus.setForeground(TXT_M);
        gc.gridx = 0;
        gc.gridy = 1;
        statusCard.add(lblDbStatus, gc);

        lblStats = new JLabel("\uD83D\uDCCA  Request: 0  |  Client: 0");
        lblStats.setFont(FONT);
        lblStats.setForeground(TXT_M);
        gc.gridx = 0;
        gc.gridy = 2;
        statusCard.add(lblStats, gc);

        main.add(statusCard);
        main.add(Box.createVerticalStrut(8));

        // Control card
        JPanel ctrlCard = card("  \u2699\uFE0F  \u0110i\u1EC1u khi\u1EC3n");
        ctrlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        ctrlCard.add(lbl("Port:"));
        tfPort = styledField(6);
        tfPort.setText(String.valueOf(NetworkConfig.SERVER_PORT));
        ctrlCard.add(tfPort);

        btnStart = makeBtn("\u25B6  Kh\u1EDFi \u0111\u1ED9ng", SUCCESS);
        btnStart.addActionListener(e -> startServer());
        ctrlCard.add(btnStart);

        btnStop = makeBtn("\u23F9  D\u1EEBng", STOP_CLR);
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopServer());
        ctrlCard.add(btnStop);

        main.add(ctrlCard);
        main.add(Box.createVerticalStrut(8));

        // Log card
        JPanel logCard = card("  \uD83D\uDCDD  Nh\u1EADt k\u00FD ho\u1EA1t \u0111\u1ED9ng");
        logArea = new JTextArea(14, 50);
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 46));
        logArea.setForeground(TXT_M);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setCaretColor(TXT);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C));
        logCard.add(sp);
        main.add(logCard);

        return main;
    }

    private JPanel createStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(SURFACE);
        p.setBorder(new EmptyBorder(2, 10, 2, 10));
        lblStatus = new JLabel("\u25CF  S\u1EB5n s\u00E0ng");
        lblStatus.setForeground(SUCCESS);
        lblStatus.setFont(FONT);
        p.add(lblStatus);
        return p;
    }

    // === UI Helpers ===
    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
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

    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(SURFACE);
        tf.setForeground(TXT);
        tf.setCaretColor(TXT);
        tf.setFont(FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C), new EmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_B);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hover = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled())
                    b.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {
                if (b.isEnabled())
                    b.setBackground(bg);
            }
        });
        return b;
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateStats() {
        SwingUtilities.invokeLater(
                () -> lblStats.setText("\uD83D\uDCCA  Request: " + requestCount + "  |  Client: " + clientCount));
    }

    // === LOGIC ===
    private void checkDatabase() {
        new Thread(() -> {
            boolean ok = DatabaseUtil.testConnection();
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    lblDbStatus.setText("\uD83D\uDDC4\uFE0F  Database: \u2705 K\u1EBFt n\u1ED1i OK");
                    lblDbStatus.setForeground(SUCCESS);
                    log("Database k\u1EBFt n\u1ED1i th\u00E0nh c\u00F4ng.");
                } else {
                    lblDbStatus.setText(
                            "\uD83D\uDDC4\uFE0F  Database: \u274C Kh\u00F4ng k\u1EBFt n\u1ED1i \u0111\u01B0\u1EE3c");
                    lblDbStatus.setForeground(ERROR);
                    log("L\u1ED6I: Kh\u00F4ng k\u1EBFt n\u1ED1i \u0111\u01B0\u1EE3c Database!");
                }
            });
        }).start();
    }

    private void startServer() {
        int port;
        try {
            port = Integer.parseInt(tfPort.getText().trim());
        } catch (Exception e) {
            port = NetworkConfig.SERVER_PORT;
        }

        final int p = port;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        tfPort.setEnabled(false);
        running = true;

        SwingUtilities.invokeLater(() -> {
            lblServerStatus.setText("\u25CF  \u0110ANG CH\u1EA0Y  |  Port: " + p);
            lblServerStatus.setForeground(SUCCESS);
            lblStatus.setText("\u25CF  Server \u0111ang l\u1EAFng nghe...");
            lblStatus.setForeground(SUCCESS);
        });

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(p);
                log("Server kh\u1EDFi \u0111\u1ED9ng t\u1EA1i port " + p);
                log("Ch\u1EDD client k\u1EBFt n\u1ED1i...");

                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        clientCount++;
                        updateStats();
                        String addr = client.getInetAddress().getHostAddress();
                        log("Client k\u1EBFt n\u1ED1i: " + addr);

                        new Thread(() -> handleClient(client, addr)).start();
                    } catch (IOException e) {
                        if (running)
                            log("L\u1ED7i accept: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                log("L\u1ED6I: Kh\u00F4ng th\u1EC3 kh\u1EDFi \u0111\u1ED9ng server: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    tfPort.setEnabled(true);
                    lblServerStatus.setText("\u25CF  L\u1ED7i");
                    lblServerStatus.setForeground(ERROR);
                });
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        } catch (Exception ignored) {
        }
        log("Server \u0111\u00E3 d\u1EEBng.");
        SwingUtilities.invokeLater(() -> {
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            tfPort.setEnabled(true);
            lblServerStatus.setText("\u25CF  \u0110\u00E3 d\u1EEBng");
            lblServerStatus.setForeground(WARN);
            lblStatus.setText("\u25CF  Server \u0111\u00E3 d\u1EEBng");
            lblStatus.setForeground(WARN);
        });
    }

    /**
     * Xử lý client qua giao thức JSON text.
     * Protocol:
     * 1. Đọc JSON request (DataInputStream.readUTF)
     * 2. Xử lý phân công
     * 3. Gửi JSON response (DataOutputStream.writeUTF)
     * 4. Gửi file Excel: writeInt(len) + write(data) x2
     */
    private void handleClient(Socket client, String addr) {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()))) {

            // 1. Đọc int đầu tiên để detect protocol
            int firstInt = dis.readInt();
            requestCount++;
            updateStats();

            Map<String, Object> reqMap;
            boolean isBinaryMode = false;

            if (firstInt < 1000) {
                // ========== BINARY PROTOCOL (client của người khác) ==========
                isBinaryMode = true;
                log("Nh\u1EADn binary request t\u1EEB " + addr + " (soCA=" + firstInt + ")");
                reqMap = readBinaryRequest(dis, firstInt);
            } else {
                // ========== JSON PROTOCOL (client của mình) ==========
                // firstInt = chiều dài JSON bytes
                byte[] reqBytes = new byte[firstInt];
                dis.readFully(reqBytes);
                String requestJson = new String(reqBytes, "UTF-8");
                log("Nh\u1EADn JSON request t\u1EEB " + addr + " (" + firstInt + " bytes)");

                try {
                    reqMap = JsonHelper.parseObject(requestJson);
                } catch (Exception e) {
                    log("JSON kh\u00F4ng h\u1EE3p l\u1EC7 t\u1EEB " + addr);
                    sendErrorResponse(dos, "JSON request kh\u00F4ng h\u1EE3p l\u1EC7!", false);
                    return;
                }
            }

            int m = JsonHelper.getInt(reqMap, "soGiamThi", 0);
            int n = JsonHelper.getInt(reqMap, "soPhongThi", 0);
            String tenDot = JsonHelper.getString(reqMap, "tenDot", "");
            log("  m=" + m + ", n=" + n + ", tenDot=" + (tenDot.isEmpty() ? "(auto)" : tenDot));

            // 2. Xử lý phân công
            Map<String, Object> result = bo.xuLyPhanCong(reqMap);

            // 3. Gửi response tương ứng với protocol
            if (isBinaryMode) {
                sendBinaryResponse(dos, result);
            } else {
                String responseJson = JsonHelper.toJson(result);
                byte[] respBytes = responseJson.getBytes("UTF-8");
                dos.writeInt(respBytes.length);
                dos.write(respBytes);
                dos.flush();
            }

            boolean success = JsonHelper.getBool(result, "success");
            if (success) {
                String tenDotActual = JsonHelper.getString(result, "tenDot", "");
                log("\u2705 Ph\u00E2n c\u00F4ng th\u00E0nh c\u00F4ng cho " + addr + " (đợt: " + tenDotActual + ")");
            } else {
                log("\u274C Th\u1EA5t b\u1EA1i: " + JsonHelper.getString(result, "message", ""));
            }
        } catch (Exception e) {
            log("L\u1ED7i x\u1EED l\u00FD client " + addr + ": " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (Exception ignored) {
            }
            log("\u0110\u00F3ng k\u1EBFt n\u1ED1i: " + addr);
        }
    }

    private void sendErrorResponse(DataOutputStream dos, String message, boolean isBinaryMode) throws IOException {
        if (isBinaryMode) {
            dos.writeBoolean(false);
            writeStringFlex(dos, message);
            dos.flush();
        } else {
            Map<String, Object> errMap = new java.util.LinkedHashMap<>();
            errMap.put("success", false);
            errMap.put("message", message);
            byte[] errBytes = JsonHelper.toJson(errMap).getBytes("UTF-8");
            dos.writeInt(errBytes.length);
            dos.write(errBytes);
            dos.flush();
        }
    }

    /**
     * Đọc binary protocol từ client khác.
     * Format chính xác:
     * soCA(đã đọc) → m(int) → n(int) → tên ca(string × soCA)
     * → soCanBo(int) → [stt(int), maGV(str), hoTen(str), ngaySinh(long),
     * donVi(str)] × soCanBo
     * → soPhong(int) → [stt(int), tenPhong(str), diaDiem(str)] × soPhong
     */
    private Map<String, Object> readBinaryRequest(DataInputStream dis, int soCA) throws Exception {
        int m = dis.readInt();
        int n = dis.readInt();
        log("  Binary: soCA=" + soCA + ", m=" + m + ", n=" + n);

        // Đọc tên ca thi
        List<String> tenCaList = new ArrayList<>();
        for (int i = 0; i < soCA; i++) {
            tenCaList.add(readStringFlex(dis));
        }
        log("  Ca thi: " + tenCaList);

        // ===== ĐỌC DANH SÁCH CÁN BỘ =====
        int soCanBo = dis.readInt();
        log("  Số cán bộ: " + soCanBo);

        List<Map<String, Object>> canBoList = new ArrayList<>();
        for (int i = 0; i < soCanBo; i++) {
            int stt = dis.readInt();
            String maGV = readStringFlex(dis);
            String hoTen = readStringFlex(dis);
            long ngaySinh = dis.readLong(); // milliseconds, -1 nếu null
            String donVi = readStringFlex(dis);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maGV", maGV);
            item.put("hoTen", hoTen);
            item.put("donVi", donVi);
            canBoList.add(item);
        }
        log("  Đọc được " + canBoList.size() + " cán bộ");

        // ===== ĐỌC DANH SÁCH PHÒNG THI =====
        int soPhong = dis.readInt();
        log("  Số phòng thi: " + soPhong);

        List<String> phongThiList = new ArrayList<>();
        for (int i = 0; i < soPhong; i++) {
            int stt = dis.readInt();
            String tenPhong = readStringFlex(dis);
            String diaDiem = readStringFlex(dis); // đọc nhưng không cần dùng
            phongThiList.add(tenPhong);
        }
        log("  Đọc được " + phongThiList.size() + " phòng thi");

        // Build request map
        Map<String, Object> reqMap = new LinkedHashMap<>();
        reqMap.put("soGiamThi", m);
        reqMap.put("soPhongThi", n);
        reqMap.put("tenDot", tenCaList.isEmpty() ? "" : String.join("_", tenCaList));
        reqMap.put("canBo", canBoList);
        reqMap.put("phongThi", phongThiList);
        return reqMap;
    }

    private String readStringFlex(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if (len <= 0)
            return "";
        if (len > 10_000_000) {
            log("  CẢNH BÁO: String length quá lớn (" + len + "), có thể lệch protocol!");
            return "";
        }
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    /**
     * Ghi string linh hoạt: writeInt(len) + write(bytes)
     */
    private void writeStringFlex(DataOutputStream dos, String str) throws IOException {
        if (str == null)
            str = "";
        byte[] bytes = str.getBytes("UTF-8");
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    /**
     * Gửi response bằng Binary Protocol (primitive data) cho client của team khác.
     */
    @SuppressWarnings("unchecked")
    private void sendBinaryResponse(DataOutputStream dos, Map<String, Object> result) throws IOException {
        boolean success = JsonHelper.getBool(result, "success");
        dos.writeBoolean(success);
        writeStringFlex(dos, JsonHelper.getString(result, "message", ""));

        if (success) {
            writeStringFlex(dos, JsonHelper.getString(result, "tenDot", ""));
            dos.writeInt(JsonHelper.getInt(result, "tongCanBo", 0));
            dos.writeInt(JsonHelper.getInt(result, "tongGiamThi", 0));
            dos.writeInt(JsonHelper.getInt(result, "tongGiamSat", 0));
            dos.writeInt(JsonHelper.getInt(result, "tongPhong", 0));

            List<Map<String, Object>> phanCong = (List<Map<String, Object>>) result.get("phanCong");
            dos.writeInt(phanCong != null ? phanCong.size() : 0);
            if (phanCong != null) {
                for (Map<String, Object> pc : phanCong) {
                    dos.writeInt(JsonHelper.getInt(pc, "stt", 0));
                    writeStringFlex(dos, JsonHelper.getString(pc, "maGV", ""));
                    writeStringFlex(dos, JsonHelper.getString(pc, "hoTen", ""));
                    writeStringFlex(dos, JsonHelper.getString(pc, "role", ""));
                    writeStringFlex(dos, JsonHelper.getString(pc, "phong", ""));
                }
            }

            List<Map<String, Object>> giamSat = (List<Map<String, Object>>) result.get("giamSat");
            dos.writeInt(giamSat != null ? giamSat.size() : 0);
            if (giamSat != null) {
                for (Map<String, Object> gs : giamSat) {
                    dos.writeInt(JsonHelper.getInt(gs, "stt", 0));
                    writeStringFlex(dos, JsonHelper.getString(gs, "maGV", ""));
                    writeStringFlex(dos, JsonHelper.getString(gs, "hoTen", ""));
                    writeStringFlex(dos, JsonHelper.getString(gs, "phamVi", ""));
                }
            }
        }
        dos.flush();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhanCongServer::new);
    }
}