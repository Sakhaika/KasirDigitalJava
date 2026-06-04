package kasirdigital;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

public class PanelStok extends JPanel {

    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color WARNING = new Color(234, 88, 12);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color HIGHLIGHT = new Color(239, 246, 255);

    private DataManager dm = DataManager.getInstance();
    private NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private DefaultTableModel modelStok;
    private JTable tabelStok;

    private JTextField txtKode, txtNama, txtHarga, txtStok, txtCari;
    private JComboBox<String> cmbKategoriForm;

    public PanelStok() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
        refreshTabel("");
    }

    private void buildUI() {
        JLabel title = new JLabel("Manajemen Stok Barang");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(580);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setLeftComponent(createPanelTabel());
        split.setRightComponent(createPanelForm());
        add(split, BorderLayout.CENTER);
    }

    private JPanel createPanelTabel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel lbl = new JLabel("Daftar Barang");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);

        txtCari = new JTextField("🔍  Cari barang...");
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCari.setForeground(TEXT_MUTED);
        txtCari.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtCari.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtCari.getText().startsWith("🔍")) {
                    txtCari.setText("");
                    txtCari.setForeground(TEXT_DARK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (txtCari.getText().isEmpty()) {
                    txtCari.setText("🔍  Cari barang...");
                    txtCari.setForeground(TEXT_MUTED);
                }
            }
        });
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshTabel(txtCari.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshTabel(txtCari.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(CARD_BG);
        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(txtCari, BorderLayout.EAST);

        String[] cols = {"Kode", "Nama Barang", "Harga", "Stok", "Kategori", "Status"};
        modelStok = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabelStok = new JTable(modelStok);
        tabelStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelStok.setRowHeight(30);
        tabelStok.setShowGrid(false);
        tabelStok.setIntercellSpacing(new Dimension(0, 0));
        tabelStok.setSelectionBackground(HIGHLIGHT);
        tabelStok.setSelectionForeground(TEXT_DARK);
        tabelStok.setFillsViewportHeight(true);
        tabelStok.setBackground(CARD_BG);

        JTableHeader header = tabelStok.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));

        tabelStok.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                }
                if (col == 5 && value != null) {
                    String v = value.toString();
                    if (v.contains("Aman")) {
                        setForeground(SUCCESS);
                    } else if (v.contains("Menipis")) {
                        setForeground(WARNING);
                    } else {
                        setForeground(DANGER);
                    }
                } else {
                    setForeground(!isSelected ? TEXT_DARK : Color.WHITE);
                }
                return c;
            }
        });

        tabelStok.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabelStok.getSelectedRow();
                if (row < 0) {
                    return;
                }
                String kode = (String) modelStok.getValueAt(row, 0);
                Barang b = dm.cariBarang(kode);
                if (b != null) {
                    isiForm(b);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabelStok);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        card.add(topBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPanelForm() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel lbl = new JLabel("Form Data Barang");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        txtKode = createFormField();
        txtNama = createFormField();
        txtHarga = createFormField();
        txtStok = createFormField();
        cmbKategoriForm = new JComboBox<>(new String[]{"Makanan", "Minuman", "Kebersihan", "Sembako", "Lainnya"});
        cmbKategoriForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addFormRow(form, gbc, 0, "Kode Barang:", txtKode);
        addFormRow(form, gbc, 1, "Nama Barang:", txtNama);
        addFormRow(form, gbc, 2, "Harga (Rp):", txtHarga);
        addFormRow(form, gbc, 3, "Stok:", txtStok);
        addFormRow(form, gbc, 4, "Kategori:", cmbKategoriForm);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton btnTambah = createBtn("➕ Tambah", SUCCESS);
        JButton btnUpdate = createBtn("✏️ Update", ACCENT);
        JButton btnHapus = createBtn("🗑 Hapus", DANGER);
        JButton btnReset = createBtn("🔄 Reset", new Color(107, 114, 128));
        JButton btnTambahStok = createBtn("📈 Tambah Stok", new Color(124, 58, 237));

        btnTambah.addActionListener(e -> tambahBarang());
        btnUpdate.addActionListener(e -> updateBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnReset.addActionListener(e -> resetForm());
        btnTambahStok.addActionListener(e -> tambahStok());

        btnPanel.add(btnTambah);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnHapus);
        btnPanel.add(btnReset);

        JPanel mainForm = new JPanel(new BorderLayout(0, 0));
        mainForm.setBackground(CARD_BG);
        mainForm.add(form, BorderLayout.NORTH);
        mainForm.add(btnPanel, BorderLayout.CENTER);
        mainForm.add(btnTambahStok, BorderLayout.SOUTH);

        card.add(lbl, BorderLayout.NORTH);
        card.add(mainForm, BorderLayout.CENTER);
        card.add(createInfoPanel(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createInfoPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        long total = dm.getDaftarBarang().size();
        long rendah = dm.getDaftarBarang().stream().filter(b -> b.getStok() <= 10).count();
        long habis = dm.getDaftarBarang().stream().filter(b -> b.getStok() == 0).count();
        p.add(createInfoCard("Total Produk", String.valueOf(total), ACCENT));
        p.add(createInfoCard("Stok Menipis", String.valueOf(rendah), WARNING));
        p.add(createInfoCard("Stok Habis", String.valueOf(habis), DANGER));
        p.add(createInfoCard("Aktif", String.valueOf(total - habis), SUCCESS));
        return p;
    }

    private JPanel createInfoCard(String label, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(color.brighter().brighter().brighter());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.brighter(), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(color.darker());
        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblName.setForeground(color.darker());
        p.add(lblVal, BorderLayout.CENTER);
        p.add(lblName, BorderLayout.SOUTH);
        return p;
    }

    // ===================== CRUD ke DB =====================
    private void tambahBarang() {
        try {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            double harga = Double.parseDouble(txtHarga.getText().trim());
            int stok = Integer.parseInt(txtStok.getText().trim());
            String kat = (String) cmbKategoriForm.getSelectedItem();
            if (kode.isEmpty() || nama.isEmpty()) {
                warn("Kode dan nama wajib diisi!");
                return;
            }
            if (dm.cariBarang(kode) != null) {
                warn("Kode barang sudah ada!");
                return;
            }
            dm.tambahBarang(new Barang(kode, nama, harga, stok, kat));
            refreshTabel("");
            resetForm();
            JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            warn("Harga dan stok harus berupa angka!");
        }
    }

    private void updateBarang() {
        try {
            String kode = txtKode.getText().trim();
            Barang b = dm.cariBarang(kode);
            if (b == null) {
                warn("Barang tidak ditemukan!");
                return;
            }
            b.setNama(txtNama.getText().trim());
            b.setHarga(Double.parseDouble(txtHarga.getText().trim()));
            b.setStok(Integer.parseInt(txtStok.getText().trim()));
            b.setKategori((String) cmbKategoriForm.getSelectedItem());
            dm.updateBarang(b);   // simpan ke DB
            refreshTabel("");
            JOptionPane.showMessageDialog(this, "Data barang berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            warn("Harga dan stok harus berupa angka!");
        }
    }

    private void hapusBarang() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) {
            warn("Pilih barang yang ingin dihapus!");
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) {
            return;
        }
        dm.hapusBarang(kode);
        refreshTabel("");
        resetForm();
    }

    private void tambahStok() {
        String kode = txtKode.getText().trim();
        Barang b = dm.cariBarang(kode);
        if (b == null) {
            warn("Pilih barang terlebih dahulu!");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                "Tambah stok untuk: " + b.getNama() + "\nStok saat ini: " + b.getStok(),
                "Tambah Stok", JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        try {
            int jml = Integer.parseInt(input.trim());
            if (jml <= 0) {
                warn("Jumlah harus lebih dari 0!");
                return;
            }
            dm.tambahStokBarang(kode, jml);           // update ke DB
            Barang bUpdated = dm.cariBarang(kode);    // ambil stok terbaru
            txtStok.setText(String.valueOf(bUpdated.getStok()));
            refreshTabel("");
        } catch (NumberFormatException ex) {
            warn("Masukkan angka yang valid!");
        }
    }

    private void isiForm(Barang b) {
        txtKode.setText(b.getKode());
        txtNama.setText(b.getNama());
        txtHarga.setText(String.valueOf((int) b.getHarga()));
        txtStok.setText(String.valueOf(b.getStok()));
        cmbKategoriForm.setSelectedItem(b.getKategori());
    }

    private void resetForm() {
        txtKode.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        cmbKategoriForm.setSelectedIndex(0);
    }

    public void refreshTabel(String filter) {
        modelStok.setRowCount(0);
        String q = filter.toLowerCase().replace("🔍", "").replace("  cari barang...", "").trim();
        for (Barang b : dm.getDaftarBarang()) {
            if (!q.isEmpty() && !b.getNama().toLowerCase().contains(q) && !b.getKode().toLowerCase().contains(q)) {
                continue;
            }
            String status = b.getStok() == 0 ? "❌ Habis" : b.getStok() <= 10 ? "⚠️ Menipis" : "✅ Aman";
            modelStok.addRow(new Object[]{
                b.getKode(), b.getNama(), rupiah.format(b.getHarga()), b.getStok(), b.getKategori(), status
            });
        }
    }

    private JTextField createFormField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        form.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(comp, gbc);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }
}
