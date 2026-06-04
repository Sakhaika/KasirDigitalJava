package kasirdigital;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.nio.file.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PanelKasir extends JPanel {

    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color ACCENT_DARK = new Color(29, 78, 216);
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

    private JTable tabelKeranjang, tabelBarang;
    private DefaultTableModel modelKeranjang, modelBarang;
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
        JLabel title = new JLabel("Transaksi Kasir");
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
        styleCombo(cmbKategori);
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
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            void filter() {
                refreshTabelBarang(txtCariBarang.getText());
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
        card.add(styledScroll(tabelBarang), BorderLayout.CENTER);
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

        JButton btnHapusItem = createButton("\uD83D\uDDD1 Hapus Item", DANGER, Color.WHITE);
        btnHapusItem.addActionListener(e -> {
            int row = tabelKeranjang.getSelectedRow();
            if (row < 0) {
                showWarning("Pilih item yang ingin dihapus!");
                return;
            }
            keranjang.remove(row);
            refreshKeranjang();
        });

        JButton btnKosongkan = createButton("\uD83E\uDDF9 Kosongkan", new Color(156, 163, 175), Color.WHITE);
        btnKosongkan.addActionListener(e -> {
            keranjang.clear();
            refreshKeranjang();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnPanel.setBackground(BG_MAIN);
        btnHapusItem.setPreferredSize(new Dimension(160, 36));
        btnKosongkan.setPreferredSize(new Dimension(160, 36));
        btnPanel.add(btnHapusItem);
        btnPanel.add(btnKosongkan);

        JPanel cartCard = createCard("Keranjang Belanja");
        cartCard.add(styledScroll(tabelKeranjang), BorderLayout.CENTER);
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

        JButton btnBayar = new JButton("\u2705  BAYAR / CHECKOUT");
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

    // ===================== LOGIKA =====================
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
        double diskonPersen = 0;
        try {
            diskonPersen = Double.parseDouble(txtDiskonPersen.getText().trim());
        } catch (Exception ignored) {
        }
        diskonPersen = Math.max(0, Math.min(100, diskonPersen));
        double diskonNominal = subtotal * (diskonPersen / 100.0);
        double total = subtotal + pajak - diskonNominal;
        lblSubtotal.setText(rupiah.format(subtotal));
        lblPajak.setText(rupiah.format(pajak));
        lblDiskon.setText(rupiah.format(diskonNominal));
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
        double diskonPersen = 0;
        try {
            diskonPersen = Double.parseDouble(txtDiskonPersen.getText().trim());
        } catch (Exception ignored) {
        }
        diskonPersen = Math.max(0, Math.min(100, diskonPersen));
        return subtotal + pajak - (subtotal * (diskonPersen / 100.0));
    }

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

        // Simpan ke database (transaksi + detail + kurangi stok)
        dm.simpanTransaksi(nomorStruk, dm.getTanggalHariIni(), total, keranjang);

        // Buat struk
        StringBuilder struk = new StringBuilder();
        struk.append("===================================\n");
        struk.append("        KASIR DIGITAL STORE\n");
        struk.append("===================================\n");
        struk.append("No. Struk : ").append(nomorStruk).append("\n");
        struk.append("Tanggal   : ").append(dm.getTanggalHariIni()).append("\n");
        struk.append("Waktu     : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        struk.append("-----------------------------------\n");
        for (ItemKeranjang item : keranjang) {
            struk.append(String.format("%-20s\n  %d x %-10s = %s\n",
                    item.getBarang().getNama(), item.getJumlah(),
                    rupiah.format(item.getBarang().getHarga()),
                    rupiah.format(item.getSubtotal())));
        }
        struk.append("-----------------------------------\n");
        double subtotal = getSubtotalValue();
        double pajak = subtotal * 0.11;
        double diskonPersen = 0;
        try {
            diskonPersen = Double.parseDouble(txtDiskonPersen.getText().trim());
        } catch (Exception ignored) {
        }
        double diskon = subtotal * (diskonPersen / 100.0);
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

        keranjang.clear();
        txtBayar.setText("0");
        txtDiskonPersen.setText("0");
        refreshKeranjang();
        refreshTabelBarang("");

        tampilkanStruk(struk.toString(), nomorStruk);
    }

    private void tampilkanStruk(String isiStruk, String nomorStruk) {
        JTextArea ta = new JTextArea(isiStruk);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setBackground(new Color(252, 252, 252));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(380, 420));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        JButton btnTxt = new JButton("\uD83D\uDCBE Simpan .TXT");
        JButton btnPdf = new JButton("\uD83D\uDCC4 Simpan .PDF");
        JButton btnTutup = new JButton("\u2716 Tutup");

        btnTxt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTxt.setBackground(new Color(37, 99, 235));
        btnTxt.setForeground(Color.WHITE);
        btnTxt.setFocusPainted(false);
        btnTxt.setBorderPainted(false);
        btnTxt.setOpaque(true);

        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPdf.setBackground(new Color(220, 38, 38));
        btnPdf.setForeground(Color.WHITE);
        btnPdf.setFocusPainted(false);
        btnPdf.setBorderPainted(false);
        btnPdf.setOpaque(true);

        btnTutup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTutup.setFocusPainted(false);

        btnPanel.add(btnTxt);
        btnPanel.add(btnPdf);
        btnPanel.add(btnTutup);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainPanel.add(sp, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "\u2705 Transaksi Berhasil - " + nomorStruk);
        dialog.setModal(true);
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnTutup.addActionListener(e -> dialog.dispose());
        btnTxt.addActionListener(e -> simpanStrukTxt(isiStruk, nomorStruk, dialog));
        btnPdf.addActionListener(e -> simpanStrukPdf(isiStruk, nomorStruk, dialog));

        dialog.setVisible(true);
    }

    private void simpanStrukTxt(String isiStruk, String nomorStruk, JDialog parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Struk-" + nomorStruk + ".txt"));
        fc.setDialogTitle("Simpan Struk sebagai TXT");
        int result = fc.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".txt")) {
            file = new File(file.getAbsolutePath() + ".txt");
        }
        try {
            Files.writeString(file.toPath(), isiStruk);
            JOptionPane.showMessageDialog(parent,
                    "Struk berhasil disimpan!\n" + file.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Gagal menyimpan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanStrukPdf(String isiStruk, String nomorStruk, JDialog parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Struk-" + nomorStruk + ".pdf"));
        fc.setDialogTitle("Simpan Struk sebagai PDF");
        int result = fc.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        String[] lines = isiStruk.split("\n");
        final File finalFile = file;
        final String[] finalLines = lines;

        try (FileOutputStream fos = new FileOutputStream(finalFile)) {
            // Bangun content stream PDF
            StringBuilder stream = new StringBuilder();
            stream.append("BT\n");
            stream.append("/F1 10 Tf\n");
            stream.append("50 780 Td\n");
            stream.append("12 TL\n");
            for (String line : finalLines) {
                // Escape karakter khusus PDF
                String escaped = line
                        .replace("\\", "\\\\")
                        .replace("(", "\\(")
                        .replace(")", "\\)")
                        .replace("\r", "");
                stream.append("(").append(escaped).append(") Tj T*\n");
            }
            stream.append("ET\n");
            String streamStr = stream.toString();
            byte[] streamBytes = streamStr.getBytes("UTF-8");

            // Bangun objek PDF
            StringBuilder pdf = new StringBuilder();
            pdf.append("%PDF-1.4\n");

            // Catat offset tiap objek untuk xref
            int[] offsets = new int[6];

            offsets[1] = pdf.length();
            pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            offsets[2] = pdf.length();
            pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            offsets[3] = pdf.length();
            pdf.append("3 0 obj\n");
            pdf.append("<< /Type /Page /Parent 2 0 R\n");
            pdf.append("   /MediaBox [0 0 420 841]\n");
            pdf.append("   /Contents 4 0 R\n");
            pdf.append("   /Resources << /Font << /F1 5 0 R >> >> >>\n");
            pdf.append("endobj\n");

            offsets[4] = pdf.length();
            pdf.append("4 0 obj\n");
            pdf.append("<< /Length ").append(streamBytes.length).append(" >>\n");
            pdf.append("stream\n");
            pdf.append(streamStr);
            pdf.append("endstream\nendobj\n");

            offsets[5] = pdf.length();
            pdf.append("5 0 obj\n");
            pdf.append("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\n");
            pdf.append("endobj\n");

            int xrefOffset = pdf.length();
            pdf.append("xref\n0 6\n");
            pdf.append("0000000000 65535 f \n");
            for (int i = 1; i <= 5; i++) {
                pdf.append(String.format("%010d 00000 n \n", offsets[i]));
            }
            pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
            pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");

            fos.write(pdf.toString().getBytes("UTF-8"));

            JOptionPane.showMessageDialog(parent,
                    "Struk berhasil disimpan sebagai PDF!\n" + finalFile.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Gagal menyimpan PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshTabelBarang(String filter) {
        modelBarang.setRowCount(0);
        String kategori = (String) (cmbKategori != null ? cmbKategori.getSelectedItem() : "Semua");
        String q = filter.toLowerCase().trim()
                .replace("cari nama / kode barang...", "").trim();
        for (Barang b : dm.getDaftarBarang()) {
            if (!"Semua".equals(kategori) && !b.getKategori().equals(kategori)) {
                continue;
            }
            if (!q.isEmpty() && !b.getNama().toLowerCase().contains(q) && !b.getKode().toLowerCase().contains(q)) {
                continue;
            }
            modelBarang.addRow(new Object[]{
                b.getKode(), b.getNama(), rupiah.format(b.getHarga()), b.getStok(), b.getKategori()
            });
        }
    }

    // ===================== HELPERS =====================
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
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
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

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(CARD_BG);
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
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
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
