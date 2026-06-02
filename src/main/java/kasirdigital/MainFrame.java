package kasirdigital;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        } catch (Exception ignored) {}

        buildLayout();
        setVisible(true);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main content
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

        // Logo / app name
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JLabel logo = new JLabel("🏪 KasirDigital");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Point of Sale System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(107, 114, 128));

        logoPanel.add(logo, BorderLayout.NORTH);
        logoPanel.add(sub, BorderLayout.CENTER);

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(new Color(31, 41, 55));
        sep.setPreferredSize(new Dimension(0, 1));

        // Navigation
        JPanel navPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(16, 8, 0, 8));

        String[][] menus = {
            {"🛒", "Kasir / Transaksi", "kasir"},
            {"📦", "Manajemen Stok", "stok"},
            {"📊", "Laporan Penjualan", "laporan"}
        };

        navBtns = new JButton[menus.length];
        for (int i = 0; i < menus.length; i++) {
            final int idx = i;
            final String panel = menus[i][2];
            JButton btn = createNavBtn(menus[i][0] + "  " + menus[i][1]);
            btn.addActionListener(e -> switchPanel(panel, idx));
            navBtns[i] = btn;
            navPanel.add(btn);
        }

        // Bottom info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(SIDEBAR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));

        JLabel version = new JLabel("v1.0 — 2025");
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
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE))
                    btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE))
                    btn.setBackground(SIDEBAR_BG);
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
        if ("laporan".equals(name)) panelLaporan.refresh();
        if ("stok".equals(name)) panelStok.refreshTabel("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}