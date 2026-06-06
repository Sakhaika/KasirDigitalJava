package kasirdigital;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PanelKasir extends JPanel {

    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color HIGHLIGHT = new Color(239, 246, 255);

    private DataManager dm = DataManager.getInstance();
    private List<ItemKeranjang> keranjang = new ArrayList<>();
    private NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private JTable tabelKeranjang;
    private DefaultTableModel modelKeranjang;
    private JTable tabelBarang;
    private DefaultTableModel modelBarang;
    private JLabel lblSubtotal, lblPajak, lblDiskon, lblTotal, lblKembalian;
    private JTextField txtBayar, txtDiskonPersen, txtCariBarang;
    private JComboBox<String> cmbKategori;

    public PanelKasir() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
    }

    private void buildUI() {
        add(createHeaderBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(520);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(BG_MAIN);
        split.setLeftComponent(createPanelProduk());
        split.setRightComponent(createPanelKeranjangBayar());
        add(split, BorderLayout.CENTER);
    }

    private JPanel createHeaderBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MAIN);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("  Transaksi Kasir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);

        JLabel tanggal = new JLabel(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  |  HH:mm", new Locale("id"))));
        tanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tanggal.setForeground(TEXT_MUTED);

        p.add(title, BorderLayout.WEST);
        p.add(tanggal, BorderLayout.EAST);
        return p;
    }

    private JPanel createPanelProduk() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_MAIN);

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(BG_MAIN);

        txtCariBarang = createStyledTextField("Cari nama / kode barang...");
        cmbKategori = new JComboBox<>(new String[]{"Semua", "Makanan", "Minuman", "Kebersihan", "Sembako"});
        cmbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        searchBar.add(txtCariBarang, BorderLayout.CENTER);
        searchBar.add(cmbKategori, BorderLayout.EAST);

        String[] cols = {"Kode", "Nama Barang", "Harga", "Stok", "Kategori"};
        modelBarang = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelBarang = createStyledTable(modelBarang);
        tabelBarang.getColumnModel().getColumn(0).setPreferredWidth(75);
        tabelBarang.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabelBarang.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabelBarang.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabelBarang.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scroll = styledScroll(tabelBarang);
        refreshTabelBarang("");

        JPanel addPanel = new JPanel(new BorderLayout(8, 0));
        addPanel.setBackground(BG_MAIN);
        JSpinner spnJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnJumlah.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton btnTambah = createButton("+ Tambah ke Keranjang", ACCENT, Color.WHITE);
        addPanel.add(new JLabel("  Jumlah: "), BorderLayout.WEST);
        addPanel.add(spnJumlah, BorderLayout.CENTER);
        addPanel.add(btnTambah, BorderLayout.EAST);

        txtCariBarang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshTabelBarang(txtCariBarang.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshTabelBarang(txtCariBarang.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });
        cmbKategori.addActionListener(e -> refreshTabelBarang(txtCariBarang.getText()));

        btnTambah.addActionListener(e -> {
            int row = tabelBarang.getSelectedRow();
            if (row < 0) {
                showWarning("Pilih barang terlebih dahulu!");
                return;
            }
            String kode = (String) modelBarang.getValueAt(row, 0);
            // ambil data terbaru dari DB
            Barang barang = dm.cariBarang(kode);
            int jml = (int) spnJumlah.getValue();
            if (barang == null) {
                return;
            }
            if (barang.getStok() < jml) {
                showWarning("Stok tidak mencukupi! Stok tersisa: " + barang.getStok());
                return;
            }
            tambahKeKeranjang(barang, jml);
        });

        tabelBarang.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btnTambah.doClick();
                }
            }
        });

        JPanel card = createCard("Daftar Produk");
        card.add(searchBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(addPanel, BorderLayout.SOUTH);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel createPanelKeranjangBayar() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_MAIN);

        String[] cols = {"Nama Barang", "Harga", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelKeranjang = createStyledTable(modelKeranjang);
        JScrollPane scrollKeranjang = styledScroll(tabelKeranjang);

        JButton btnHapusItem = createButton("Hapus Item", DANGER, Color.WHITE);
        btnHapusItem.addActionListener(e -> {
            int row = tabelKeranjang.getSelectedRow();
            if (row < 0) {
                showWarning("Pilih item yang ingin dihapus!");
                return;
            }
            keranjang.remove(row);
            refreshKeranjang();
        });

        JButton btnKosongkan = createButton("Kosongkan", new Color(156, 163, 175), Color.WHITE);
        btnKosongkan.addActionListener(e -> {
            keranjang.clear();
            refreshKeranjang();
        });

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setBackground(BG_MAIN);
        btnPanel.add(btnHapusItem);
        btnPanel.add(btnKosongkan);

        JPanel cartCard = createCard("Keranjang Belanja");
        cartCard.add(scrollKeranjang, BorderLayout.CENTER);
        cartCard.add(btnPanel, BorderLayout.SOUTH);

        p.add(cartCard, BorderLayout.CENTER);
        p.add(createCalcPanel(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createCalcPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JPanel rincian = new JPanel(new GridLayout(0, 2, 8, 6));
        rincian.setBackground(CARD_BG);

        lblSubtotal = createLblValue("Rp 0");
        lblPajak = createLblValue("Rp 0");
        lblDiskon = createLblValue("Rp 0");
        lblTotal = createLblValue("Rp 0");
        lblKembalian = createLblValue("Rp 0");

        txtDiskonPersen = new JTextField("0");
        txtDiskonPersen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDiskonPersen.setHorizontalAlignment(JTextField.RIGHT);
        txtBayar = new JTextField("0");
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtBayar.setHorizontalAlignment(JTextField.RIGHT);

        addRow(rincian, "Subtotal:", lblSubtotal);
        addRow(rincian, "PPN (11%):", lblPajak);
        addRow(rincian, "Diskon (%):", txtDiskonPersen);
        addRow(rincian, "Potongan:", lblDiskon);
        addRow(rincian, "TOTAL:", lblTotal);
        addRow(rincian, "Bayar (Rp):", txtBayar);
        addRow(rincian, "Kembalian:", lblKembalian);

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(ACCENT);

        JButton btnBayar = new JButton("BAYAR / CHECKOUT");
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBayar.setBackground(SUCCESS);
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setFocusPainted(false);
        btnBayar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBayar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnBayar.setOpaque(true);
        btnBayar.addActionListener(e -> prosesCheckout());

        txtDiskonPersen.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                hitungTotal();
            }
        });
        txtBayar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                hitungKembalian();
            }
        });

        card.add(rincian, BorderLayout.CENTER);
        card.add(btnBayar, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================
    // LOGIKA KERANJANG
    // =========================================================
    private void tambahKeKeranjang(Barang barang, int jumlah) {
        for (ItemKeranjang item : keranjang) {
            if (item.getBarang().getKode().equals(barang.getKode())) {
                int totalBaru = item.getJumlah() + jumlah;
                if (barang.getStok() < totalBaru) {
                    showWarning("Stok tidak mencukupi untuk jumlah ini!");
                    return;
                }
                item.setJumlah(totalBaru);
                refreshKeranjang();
                return;
            }
        }
        keranjang.add(new ItemKeranjang(barang, jumlah));
        refreshKeranjang();
    }

    private void refreshKeranjang() {
        modelKeranjang.setRowCount(0);
        for (ItemKeranjang item : keranjang) {
            modelKeranjang.addRow(new Object[]{
                item.getBarang().getNama(),
                rupiah.format(item.getBarang().getHarga()),
                item.getJumlah(),
                rupiah.format(item.getSubtotal())
            });
        }
        hitungTotal();
    }

    private double getSubtotalValue() {
        return keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
    }

    private void hitungTotal() {
        double subtotal = getSubtotalValue();
        double pajak = subtotal * 0.11;
        double diskonPct = parseDiskon();
        double diskon = subtotal * (diskonPct / 100.0);
        double total = subtotal + pajak - diskon;

        lblSubtotal.setText(rupiah.format(subtotal));
        lblPajak.setText(rupiah.format(pajak));
        lblDiskon.setText(rupiah.format(diskon));
        lblTotal.setText(rupiah.format(total));
        hitungKembalian();
    }

    private void hitungKembalian() {
        try {
            double bayar = Double.parseDouble(txtBayar.getText().replace(".", "").trim());
            double total = getTotal();
            double kembalian = bayar - total;
            lblKembalian.setText(rupiah.format(Math.max(0, kembalian)));
            lblKembalian.setForeground(kembalian >= 0 ? SUCCESS : DANGER);
        } catch (Exception ignored) {
        }
    }

    private double getTotal() {
        double subtotal = getSubtotalValue();
        double pajak = subtotal * 0.11;
        double diskonPct = parseDiskon();
        return subtotal + pajak - (subtotal * (diskonPct / 100.0));
    }

    private double parseDiskon() {
        try {
            double d = Double.parseDouble(txtDiskonPersen.getText().trim());
            return Math.max(0, Math.min(100, d));
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================================================
    // CHECKOUT — simpan ke MySQL
    // =========================================================
    private void prosesCheckout() {
        if (keranjang.isEmpty()) {
            showWarning("Keranjang masih kosong!");
            return;
        }

        double bayar = 0;
        try {
            bayar = Double.parseDouble(txtBayar.getText().replace(".", "").trim());
        } catch (Exception e) {
            showWarning("Masukkan nominal pembayaran yang valid!");
            return;
        }
        double total = getTotal();
        if (bayar < total) {
            showWarning("Pembayaran kurang! Kurang: " + rupiah.format(total - bayar));
            return;
        }

        String nomorStruk = dm.getNomorStrukBerikutnya();
        String tanggal = dm.getTanggalHariIni();

        // Simpan ke database (termasuk update stok)
        dm.simpanTransaksi(nomorStruk, tanggal, total, keranjang);

        // Buat teks struk
        StringBuilder struk = new StringBuilder();
        struk.append("===================================\n");
        struk.append("        KASIR DIGITAL STORE\n");
        struk.append("===================================\n");
        struk.append("No. Struk : ").append(nomorStruk).append("\n");
        struk.append("Tanggal   : ").append(tanggal).append("\n");
        struk.append("Waktu     : ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        struk.append("-----------------------------------\n");

        double subtotal = getSubtotalValue();
        double pajak = subtotal * 0.11;
        double diskon = subtotal * (parseDiskon() / 100.0);

        for (ItemKeranjang item : keranjang) {
            struk.append(String.format("%-20s\n  %d x %-10s = %s\n",
                    item.getBarang().getNama(), item.getJumlah(),
                    rupiah.format(item.getBarang().getHarga()),
                    rupiah.format(item.getSubtotal())));
        }
        struk.append("-----------------------------------\n");
        struk.append(String.format("Subtotal  : %s\n", rupiah.format(subtotal)));
        struk.append(String.format("PPN 11%%   : %s\n", rupiah.format(pajak)));
        if (diskon > 0) {
            struk.append(String.format("Diskon    : -%s\n", rupiah.format(diskon)));
        }
        struk.append(String.format("TOTAL     : %s\n", rupiah.format(total)));
        struk.append(String.format("Bayar     : %s\n", rupiah.format(bayar)));
        struk.append(String.format("Kembalian : %s\n", rupiah.format(bayar - total)));
        struk.append("===================================\n");
        struk.append("      Terima kasih! Selamat\n");
        struk.append("         berbelanja kembali\n");
        struk.append("===================================");

        // Reset UI
        keranjang.clear();
        txtBayar.setText("0");
        txtDiskonPersen.setText("0");
        refreshKeranjang();
        refreshTabelBarang(""); // refresh stok dari DB

        // Dialog struk
        showDialogStruk(struk.toString(), nomorStruk);
    }

    private void showDialogStruk(String isiStruk, String noStruk) {
        JTextArea ta = new JTextArea(isiStruk);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBackground(new Color(252, 252, 252));
        ta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(400, 380));

        JButton btnTxt = createButton("Simpan TXT", ACCENT, Color.WHITE);
        JButton btnPdf = createButton("Cetak / PDF", DANGER, Color.WHITE);
        JButton btnTutup = new JButton("Tutup");
        btnTutup.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        btnPanel.add(btnTxt);
        btnPanel.add(btnPdf);
        btnPanel.add(btnTutup);

        JPanel dialogPanel = new JPanel(new BorderLayout(0, 8));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        dialogPanel.add(sp, BorderLayout.CENTER);
        dialogPanel.add(btnPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Transaksi Berhasil - " + noStruk, true);
        dialog.add(dialogPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnTxt.addActionListener(ev -> simpanStrukTXT(isiStruk, noStruk));
        btnPdf.addActionListener(ev -> cetakStrukPDF(isiStruk, noStruk));
        btnTutup.addActionListener(ev -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void simpanStrukTXT(String isi, String noStruk) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Struk sebagai TXT");
        fc.setSelectedFile(new java.io.File("Struk_" + noStruk + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".txt")) {
            file = new java.io.File(file.getAbsolutePath() + ".txt");
        }
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
            pw.print(isi);
            JOptionPane.showMessageDialog(this, "Struk disimpan!\n" + file.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cetakStrukPDF(String isi, String noStruk) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk " + noStruk);
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
                JOptionPane.showMessageDialog(this, "Struk berhasil dicetak!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Gagal cetak: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    // REFRESH TABEL DARI DB
    // =========================================================
    public void refreshTabelBarang(String filter) {
        modelBarang.setRowCount(0);
        String kategori = cmbKategori != null ? (String) cmbKategori.getSelectedItem() : "Semua";
        String q = filter.toLowerCase().trim().replace("cari nama / kode barang...", "").trim();
        for (Barang b : dm.getDaftarBarang()) {
            if (!"Semua".equals(kategori) && !b.getKategori().equals(kategori)) {
                continue;
            }
            if (!q.isEmpty() && !b.getNama().toLowerCase().contains(q) && !b.getKode().toLowerCase().contains(q)) {
                continue;
            }
            modelBarang.addRow(new Object[]{b.getKode(), b.getNama(), rupiah.format(b.getHarga()), b.getStok(), b.getKategori()});
        }
    }

    // =========================================================
    // HELPER UI
    // =========================================================
    private JTable createStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(HIGHLIGHT);
        t.setSelectionForeground(TEXT_DARK);
        t.setFillsViewportHeight(true);
        t.setBackground(CARD_BG);
        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return t;
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(CARD_BG);
        return sp;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_MUTED);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_DARK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(TEXT_MUTED);
                }
            }
        });
        return tf;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JLabel createLblValue(String val) {
        JLabel lbl = new JLabel(val);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_DARK);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    private void addRow(JPanel p, String label, Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl);
        p.add(comp);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }
}
