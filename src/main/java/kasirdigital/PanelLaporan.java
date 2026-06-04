package kasirdigital;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

public class PanelLaporan extends JPanel {

    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color WARNING = new Color(234, 88, 12);

    private DataManager dm = DataManager.getInstance();
    private NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private DefaultTableModel modelRiwayat;
    private JTable tabelRiwayat;

    public PanelLaporan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Laporan Penjualan Harian");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG_MAIN);
        content.add(createSummaryPanel(), BorderLayout.NORTH);
        content.add(createDetailPanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setBackground(BG_MAIN);

        double hariIni = dm.getTotalPenjualanHariIni();
        int totalTransaksi = dm.getRiwayatTransaksi().size();
        double totalSemua = dm.getPenjualanHarian().values().stream().mapToDouble(Double::doubleValue).sum();
        int hariAktif = dm.getPenjualanHarian().size();

        p.add(createStatCard("💰 Pendapatan Hari Ini", rupiah.format(hariIni), SUCCESS));
        p.add(createStatCard("🧾 Total Transaksi", String.valueOf(totalTransaksi), ACCENT));
        p.add(createStatCard("📅 Hari Aktif", String.valueOf(hariAktif), WARNING));
        p.add(createStatCard("💹 Total Keseluruhan", rupiah.format(totalSemua), new Color(124, 58, 237)));
        return p;
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JPanel bar = new JPanel();
        bar.setBackground(color);
        bar.setPreferredSize(new Dimension(0, 4));
        JLabel lblIcon = new JLabel(label);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIcon.setForeground(TEXT_MUTED);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(color);
        card.add(bar, BorderLayout.NORTH);
        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblValue, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDetailPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel lbl = new JLabel("Riwayat Penjualan Per Hari  —  klik baris untuk lihat detail");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);

        // Tabel riwayat harian
        String[] cols = {"Tanggal", "Total Pendapatan", "Jumlah Transaksi", "Total Item Terjual"};
        modelRiwayat = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelRiwayat = new JTable(modelRiwayat);
        tabelRiwayat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelRiwayat.setRowHeight(32);
        tabelRiwayat.setShowGrid(false);
        tabelRiwayat.setIntercellSpacing(new Dimension(0, 0));
        tabelRiwayat.setSelectionBackground(new Color(239, 246, 255));
        tabelRiwayat.setSelectionForeground(TEXT_DARK);
        tabelRiwayat.setFillsViewportHeight(true);
        tabelRiwayat.setBackground(CARD_BG);

        JTableHeader header = tabelRiwayat.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));

        // Fix: renderer dengan warna teks yang benar
        tabelRiwayat.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (sel) {
                    setBackground(new Color(239, 246, 255));
                    setForeground(TEXT_DARK);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                    setForeground(col == 1 ? SUCCESS : TEXT_DARK);
                }
                return this;
            }
        });

        // Klik baris → tampilkan detail transaksi
        tabelRiwayat.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabelRiwayat.getSelectedRow();
                if (row < 0) {
                    return;
                }
                String tanggal = (String) modelRiwayat.getValueAt(row, 0);
                tampilkanDetailTanggal(tanggal);
            }
        });

        isiTabelRiwayat();

        JScrollPane scroll = new JScrollPane(tabelRiwayat);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        card.add(lbl, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(createStokRendahPanel(), BorderLayout.SOUTH);
        return card;
    }

    private void isiTabelRiwayat() {
        modelRiwayat.setRowCount(0);
        String sql = "SELECT t.tanggal, SUM(t.total) as total_hari, COUNT(t.id) as jml_transaksi, "
                + "SUM(t.jumlah_item) as jml_item FROM transaksi t GROUP BY t.tanggal ORDER BY t.tanggal DESC";
        try (Connection con = DatabaseConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelRiwayat.addRow(new Object[]{
                    rs.getString("tanggal"),
                    rupiah.format(rs.getDouble("total_hari")),
                    rs.getInt("jml_transaksi") + " transaksi",
                    rs.getInt("jml_item") + " item"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (modelRiwayat.getRowCount() == 0) {
            modelRiwayat.addRow(new Object[]{"—", "Belum ada transaksi", "—", "—"});
        }
    }

    private void tampilkanDetailTanggal(String tanggal) {
        // Ambil semua transaksi pada tanggal tersebut dari DB
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CARD_BG);
        panel.setPreferredSize(new Dimension(700, 450));

        JLabel lblJudul = new JLabel("Detail Transaksi — " + tanggal);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblJudul.setForeground(TEXT_DARK);
        lblJudul.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"No. Struk", "Nama Barang", "Harga Satuan", "Qty", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT t.nomor_struk, d.nama_barang, d.harga, d.jumlah, d.subtotal "
                + "FROM transaksi t JOIN detail_transaksi d ON t.nomor_struk = d.nomor_struk "
                + "WHERE t.tanggal = ? ORDER BY t.nomor_struk, d.id";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tanggal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("nomor_struk"),
                        rs.getString("nama_barang"),
                        rupiah.format(rs.getDouble("harga")),
                        rs.getInt("jumlah"),
                        rupiah.format(rs.getDouble("subtotal"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel.setRowHeight(28);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setBackground(CARD_BG);
        tabel.setFillsViewportHeight(true);

        // Fix warna teks di dialog detail
        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (sel) {
                    setBackground(new Color(239, 246, 255));
                    setForeground(TEXT_DARK);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                    setForeground(col == 4 ? SUCCESS : TEXT_DARK);
                }
                return this;
            }
        });

        JTableHeader header = tabel.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 32));

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        panel.add(lblJudul, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel,
                "📋 Detail Transaksi — " + tanggal, JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createStokRendahPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JLabel lbl = new JLabel("⚠️  Peringatan Stok Rendah (≤ 10 unit)");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(WARNING);

        StringBuilder sb = new StringBuilder();
        long count = 0;
        for (Barang b : dm.getDaftarBarang()) {
            if (b.getStok() <= 10) {
                sb.append("  • ").append(b.getNama()).append(" → stok: ").append(b.getStok()).append(" unit\n");
                count++;
            }
        }

        if (count == 0) {
            JLabel ok = new JLabel("✅ Semua stok barang dalam kondisi aman.");
            ok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            ok.setForeground(SUCCESS);
            p.add(lbl, BorderLayout.NORTH);
            p.add(ok, BorderLayout.CENTER);
        } else {
            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ta.setForeground(TEXT_DARK);
            ta.setBackground(new Color(255, 247, 237));
            ta.setEditable(false);
            ta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(253, 186, 116), 1, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            p.add(lbl, BorderLayout.NORTH);
            p.add(ta, BorderLayout.CENTER);
        }
        return p;
    }

    public void refresh() {
        removeAll();
        setLayout(new BorderLayout(12, 12));
        buildUI();
        revalidate();
        repaint();
    }
}
