package kasirdigital;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BG = new Color(17, 24, 39);
    private static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);
    private static final Color SIDEBAR_HOVER = new Color(31, 41, 55);
    private static final Color SIDEBAR_TEXT = new Color(209, 213, 219);
    private static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton[] navBtns;

    private PanelKasir panelKasir;
    private PanelStok panelStok;
    private PanelLaporan panelLaporan;

    public MainFrame() {
        setTitle("Kasir Digital & Stok Barang");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        buildLayout();
        setVisible(true);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250));

        panelKasir = new PanelKasir();
        panelStok = new PanelStok();
        panelLaporan = new PanelLaporan();

        contentPanel.add(panelKasir, "kasir");
        contentPanel.add(panelStok, "stok");
        contentPanel.add(panelLaporan, "laporan");

        add(contentPanel, BorderLayout.CENTER);
        switchPanel("kasir", 0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));

        JPanel logoPanel = new JPanel(new BorderLayout(10, 0));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));

        // --- Gambar logo: dicoba dari beberapa lokasi ---
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(44, 44));
        Image logoImg = loadLogo();
        if (logoImg != null) {
            imgLabel.setIcon(new ImageIcon(logoImg));
        }

        // --- Teks di kanan gambar ---
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(SIDEBAR_BG);

        JLabel logo = new JLabel("SiNiGer");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Point of Sale System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(107, 114, 128));

        textPanel.add(logo, BorderLayout.NORTH);
        textPanel.add(sub, BorderLayout.CENTER);

        logoPanel.add(imgLabel, BorderLayout.WEST);
        logoPanel.add(textPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(16, 8, 0, 8));

        String[][] menus = {
            {"[Kasir]", "Kasir / Transaksi", "kasir"},
            {"[Stok]", "Manajemen Stok", "stok"},
            {"[Lap.]", "Laporan Penjualan", "laporan"}
        };

        navBtns = new JButton[menus.length];
        for (int i = 0; i < menus.length; i++) {
            final int idx = i;
            final String panel = menus[i][2];
            JButton btn = createNavBtn(menus[i][1]);
            btn.addActionListener(e -> switchPanel(panel, idx));
            navBtns[i] = btn;
            navPanel.add(btn);
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(SIDEBAR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
        JLabel version = new JLabel("v1.0 - 2025");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(75, 85, 99));
        bottomPanel.add(version, BorderLayout.SOUTH);

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });
        return btn;
    }

    private void switchPanel(String name, int idx) {
        cardLayout.show(contentPanel, name);
        for (int i = 0; i < navBtns.length; i++) {
            if (i == idx) {
                navBtns[i].setBackground(SIDEBAR_ACTIVE);
                navBtns[i].setForeground(SIDEBAR_TEXT_ACTIVE);
                navBtns[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                navBtns[i].setBackground(SIDEBAR_BG);
                navBtns[i].setForeground(SIDEBAR_TEXT);
                navBtns[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }
        if ("laporan".equals(name)) {
            panelLaporan.refresh();
        }
        if ("stok".equals(name)) {
            panelStok.refreshTabel("");
        }
    }

    /**
     * Coba muat logo dari beberapa lokasi secara berurutan. Letakkan logo.jpg
     * di salah satu tempat berikut: 1. Folder yang sama dengan MainFrame.java
     * (src/kasirdigital/logo.jpg) 2. Folder root project
     * (kasirdigital_mysql/logo.jpg) 3. Classpath resource (via
     * getResourceAsStream)
     */
    private Image loadLogo() {
        // kandidat path — urutan prioritas
        String[] candidates = {
            // 1. Relatif dari working directory (saat run dari NetBeans/IntelliJ)
            "src/kasirdigital/logo.jpg",
            "src\\kasirdigital\\logo.jpg",
            // 2. Di samping file .class (setelah compile)
            "build/classes/kasirdigital/logo.jpg",
            "out/production/kasirdigital/kasirdigital/logo.jpg",
            // 3. Root project
            "logo.jpg",
            // 4. Folder kasirdigital di root
            "kasirdigital/logo.jpg",};

        for (String path : candidates) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    BufferedImage raw = ImageIO.read(f);
                    if (raw != null) {
                        return raw.getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 5. Terakhir coba via classpath resource
        try {
            InputStream is = MainFrame.class.getResourceAsStream("logo.jpg");
            if (is == null) {
                is = MainFrame.class.getResourceAsStream("/kasirdigital/logo.jpg");
            }
            if (is != null) {
                BufferedImage raw = ImageIO.read(is);
                is.close();
                if (raw != null) {
                    return raw.getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                }
            }
        } catch (Exception ignored) {
        }

        return null; // tidak ditemukan, sidebar tetap tampil tanpa gambar
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
