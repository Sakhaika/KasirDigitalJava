package kasirdigital;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final Color DANGER = new Color(220, 38, 38);

    private DataManager dm = DataManager.getInstance();
    private NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private JTable tabelHarian;
    private DefaultTableModel modelHarian;

    public PanelLaporan() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_MAIN);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel("Laporan Penjualan Harian");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);
        headerRow.add(title, BorderLayout.WEST);
        add(headerRow, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG_MAIN);
        content.add(createSummaryPanel(), BorderLayout.NORTH);
        content.add(createDetailPanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // ===================== RINGKASAN =====================
    private JPanel createSummaryPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setBackground(BG_MAIN);

        double hariIni = dm.getTotalPenjualanHariIni();
        int totalTransaksi = dm.getTotalJumlahTransaksi();
        double totalSemua = dm.getTotalSemuaPendapatan();
        int hariAktif = dm.getJumlahHariAktif();

        p.add(createStatCard("Pendapatan Hari Ini", rupiah.format(hariIni), SUCCESS));
        p.add(createStatCard("Total Transaksi", String.valueOf(totalTransaksi), ACCENT));
        p.add(createStatCard("Hari Aktif", String.valueOf(hariAktif), WARNING));
        p.add(createStatCard("Total Keseluruhan", rupiah.format(totalSemua), new Color(124, 58, 237)));
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
        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setForeground(TEXT_MUTED);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(color);
        card.add(bar, BorderLayout.NORTH);
        card.add(lblName, BorderLayout.CENTER);
        card.add(lblValue, BorderLayout.SOUTH);
        return card;
    }

    // ===================== DETAIL =====================
    private JPanel createDetailPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(CARD_BG);
        JLabel lbl = new JLabel("Riwayat Penjualan Per Hari    klik baris untuk cetak/export");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setBackground(CARD_BG);
        JButton btnPreview = createBtn("Preview", new Color(22, 163, 74));
        JButton btnTxt = createBtn("Export TXT", new Color(37, 99, 235));
        JButton btnPdf = createBtn("Cetak / PDF", new Color(220, 38, 38));
        btnPreview.addActionListener(e -> aksiPerTanggal("preview"));
        btnTxt.addActionListener(e -> aksiPerTanggal("txt"));
        btnPdf.addActionListener(e -> aksiPerTanggal("pdf"));
        btnPanel.add(new JLabel("Pilih tanggal lalu:"));
        btnPanel.add(btnPreview);
        btnPanel.add(btnTxt);
        btnPanel.add(btnPdf);
        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Tanggal", "Total Pendapatan", "Jumlah Transaksi", "Total Item Terjual"};
        modelHarian = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelHarian = new JTable(modelHarian);
        tabelHarian.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelHarian.setRowHeight(32);
        tabelHarian.setShowGrid(false);
        tabelHarian.setIntercellSpacing(new Dimension(0, 0));
        tabelHarian.setSelectionBackground(new Color(239, 246, 255));
        tabelHarian.setFillsViewportHeight(true);
        tabelHarian.setBackground(CARD_BG);

        JTableHeader header = tabelHarian.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));

        tabelHarian.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                    setForeground(col == 1 ? SUCCESS : TEXT_DARK);
                }
                return c;
            }
        });

        isiTabel();

        JScrollPane scroll = new JScrollPane(tabelHarian);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        card.add(topBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(createStokRendahPanel(), BorderLayout.SOUTH);
        return card;
    }

    // ===================== ISI TABEL DARI DB =====================
    private void isiTabel() {
        modelHarian.setRowCount(0);
        for (Object[] row : dm.getRingkasanPerHari()) {
            String tanggal = (String) row[0];
            double total = (Double) row[1];
            int jumlahTrx = (Integer) row[2];
            int totalItem = (Integer) row[3];
            modelHarian.addRow(new Object[]{
                tanggal,
                rupiah.format(total),
                jumlahTrx + " transaksi",
                totalItem + " item"
            });
        }
        if (modelHarian.getRowCount() == 0) {
            modelHarian.addRow(new Object[]{"--", "Belum ada transaksi", "--", "--"});
        }
    }

    // ===================== AKSI PER TANGGAL =====================
    private void aksiPerTanggal(String mode) {
        int row = tabelHarian.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan klik/pilih baris tanggal terlebih dahulu!", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tanggal = (String) modelHarian.getValueAt(row, 0);
        if ("--".equals(tanggal)) {
            return;
        }
        String isi = generateLaporanPerTanggal(tanggal);
        switch (mode) {
            case "preview":
                previewTanggal(tanggal, isi);
                break;
            case "txt":
                exportTXT(tanggal, isi);
                break;
            case "pdf":
                exportPDF(tanggal, isi);
                break;
        }
    }

    // ===================== GENERATE LAPORAN =====================
    private String generateLaporanPerTanggal(String tanggal) {
        StringBuilder sb = new StringBuilder();
        String waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        java.util.List<Object[]> detailTrx = dm.getDetailTransaksiPerTanggal(tanggal);
        double totalPendapatan = detailTrx.stream().mapToDouble(r -> (Double) r[1]).sum();
        long totalItem = detailTrx.stream().mapToLong(r -> (Integer) r[2]).sum();

        sb.append("============================================================\n");
        sb.append("              LAPORAN PENJUALAN HARIAN\n");
        sb.append("                   KASIR DIGITAL STORE\n");
        sb.append("============================================================\n");
        sb.append(String.format("Tanggal Laporan : %s\n", tanggal));
        sb.append(String.format("Dicetak pada    : %s\n", waktu));
        sb.append("------------------------------------------------------------\n\n");
        sb.append("RINGKASAN HARI INI\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-30s : %s\n", "Total Pendapatan", rupiah.format(totalPendapatan)));
        sb.append(String.format("%-30s : %d transaksi\n", "Jumlah Transaksi", detailTrx.size()));
        sb.append(String.format("%-30s : %d item\n", "Total Item Terjual", totalItem));
        sb.append("\nDETAIL TRANSAKSI\n");
        sb.append("------------------------------------------------------------\n");

        if (detailTrx.isEmpty()) {
            sb.append("  (Tidak ada transaksi pada tanggal ini)\n");
        } else {
            int no = 1;
            for (Object[] r : detailTrx) {
                sb.append(String.format("  %d. %s — Total: %-20s | %d item\n",
                        no++, r[0], rupiah.format((Double) r[1]), (Integer) r[2]));
            }
        }

        sb.append("\nKONDISI STOK SAAT INI\n");
        sb.append("------------------------------------------------------------\n");
        boolean adaRendah = false;
        for (Barang b : dm.getDaftarBarang()) {
            if (b.getStok() <= 10) {
                sb.append(String.format("  * %-25s stok: %d unit\n", b.getNama(), b.getStok()));
                adaRendah = true;
            }
        }
        if (!adaRendah) {
            sb.append("  Semua stok barang dalam kondisi aman.\n");
        }

        sb.append("\n============================================================\n");
        sb.append("           -- Terima kasih, Kasir Digital Store --\n");
        sb.append("============================================================\n");
        return sb.toString();
    }

    // ===================== PREVIEW / TXT / PDF =====================
    private void previewTanggal(String tanggal, String isi) {
        JTextArea ta = new JTextArea(isi);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBackground(new Color(252, 252, 252));
        ta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(580, 480));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(sp, BorderLayout.CENTER);
        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton bTxt = new JButton("Simpan TXT");
        JButton bCetak = new JButton("Cetak / PDF");
        bTxt.addActionListener(e -> exportTXT(tanggal, isi));
        bCetak.addActionListener(e -> exportPDF(tanggal, isi));
        btnPnl.add(bTxt);
        btnPnl.add(bCetak);
        panel.add(btnPnl, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Preview Laporan - " + tanggal, JOptionPane.PLAIN_MESSAGE);
    }

    private void exportTXT(String tanggal, String isi) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan TXT - " + tanggal);
        fc.setSelectedFile(new File("Laporan_" + tanggal.replace("/", "-") + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".txt")) {
            file = new File(file.getAbsolutePath() + ".txt");
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(isi);
            JOptionPane.showMessageDialog(this, "Laporan berhasil disimpan!\n" + file.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPDF(String tanggal, String isi) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Laporan " + tanggal);
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(Color.BLACK);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight(), yPos = lineH;
            for (String line : isi.split("\n")) {
                g2.drawString(line, 0, yPos);
                yPos += lineH;
                if (yPos > pageFormat.getImageableHeight()) {
                    break;
                }
            }
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Berhasil dicetak!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Gagal cetak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===================== STOK RENDAH =====================
    private JPanel createStokRendahPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        JLabel lbl = new JLabel("Peringatan Stok Rendah (<= 10 unit)");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(WARNING);
        p.add(lbl, BorderLayout.NORTH);

        StringBuilder sb = new StringBuilder();
        long count = 0;
        for (Barang b : dm.getDaftarBarang()) {
            if (b.getStok() <= 10) {
                sb.append("  * ").append(b.getNama()).append(" -> stok: ").append(b.getStok()).append(" unit\n");
                count++;
            }
        }
        if (count == 0) {
            JLabel ok = new JLabel("Semua stok barang dalam kondisi aman.");
            ok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            ok.setForeground(SUCCESS);
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
            p.add(ta, BorderLayout.CENTER);
        }
        return p;
    }

    private JButton createBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    public void refresh() {
        removeAll();
        setLayout(new BorderLayout(12, 12));
        buildUI();
        revalidate();
        repaint();
    }
}
