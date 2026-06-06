package kasirdigital;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * DataManager versi MySQL. Menggantikan implementasi in-memory dengan operasi
 * JDBC langsung ke database kasirdigital.
 */
public class DataManager {

    private static DataManager instance;

    private DataManager() {
        // pastikan koneksi tersedia saat pertama kali dibuat
        DatabaseConnection.getConnection();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // =========================================================
    // BARANG
    // =========================================================
    /**
     * Ambil semua barang dari tabel barang
     */
    public List<Barang> getDaftarBarang() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT kode, nama, harga, stok, kategori FROM barang ORDER BY kode";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Barang(
                        rs.getString("kode"),
                        rs.getString("nama"),
                        rs.getDouble("harga"),
                        rs.getInt("stok"),
                        rs.getString("kategori")));
            }
        } catch (SQLException e) {
            showError("Gagal memuat data barang", e);
        }
        return list;
    }

    /**
     * Cari satu barang berdasarkan kode
     */
    public Barang cariBarang(String kode) {
        String sql = "SELECT kode, nama, harga, stok, kategori FROM barang WHERE kode = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Barang(
                            rs.getString("kode"),
                            rs.getString("nama"),
                            rs.getDouble("harga"),
                            rs.getInt("stok"),
                            rs.getString("kategori"));
                }
            }
        } catch (SQLException e) {
            showError("Gagal mencari barang", e);
        }
        return null;
    }

    /**
     * Insert barang baru
     */
    public void tambahBarang(Barang b) {
        String sql = "INSERT INTO barang (kode, nama, harga, stok, kategori) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getKode());
            ps.setString(2, b.getNama());
            ps.setDouble(3, b.getHarga());
            ps.setInt(4, b.getStok());
            ps.setString(5, b.getKategori());
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Gagal menambahkan barang", e);
        }
    }

    /**
     * Update data barang (nama, harga, stok, kategori)
     */
    public void updateBarang(Barang b) {
        String sql = "UPDATE barang SET nama=?, harga=?, stok=?, kategori=? WHERE kode=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getNama());
            ps.setDouble(2, b.getHarga());
            ps.setInt(3, b.getStok());
            ps.setString(4, b.getKategori());
            ps.setString(5, b.getKode());
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Gagal mengupdate barang", e);
        }
    }

    /**
     * Kurangi stok barang setelah transaksi
     */
    public void kurangiStok(String kode, int jumlah) {
        String sql = "UPDATE barang SET stok = stok - ? WHERE kode = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Gagal mengurangi stok", e);
        }
    }

    /**
     * Tambah stok barang
     */
    public void tambahStokDB(String kode, int jumlah) {
        String sql = "UPDATE barang SET stok = stok + ? WHERE kode = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Gagal menambah stok", e);
        }
    }

    /**
     * Hapus barang berdasarkan kode
     */
    public void hapusBarang(String kode) {
        String sql = "DELETE FROM barang WHERE kode = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            showError("Gagal menghapus barang", e);
        }
    }

    // =========================================================
    // TRANSAKSI
    // =========================================================
    /**
     * Generate nomor struk berikutnya. Format: TRX-XXXX, urut berdasarkan ID
     * terbesar di tabel transaksi.
     */
    public String getNomorStrukBerikutnya() {
        String sql = "SELECT MAX(id) AS maxid FROM transaksi";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int next = rs.getInt("maxid") + 1;
                return String.format("TRX-%04d", next);
            }
        } catch (SQLException e) {
            showError("Gagal membuat nomor struk", e);
        }
        return "TRX-0001";
    }

    /**
     * Simpan satu transaksi beserta semua detail item ke database. Juga
     * mengurangi stok barang secara atomik.
     */
    public void simpanTransaksi(String nomorStruk, String tanggal, double total,
            List<ItemKeranjang> items) {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Insert header transaksi
            String sqlHeader = "INSERT INTO transaksi (nomor_struk, tanggal, total, jumlah_item) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, nomorStruk);
                ps.setString(2, tanggal);
                ps.setDouble(3, total);
                ps.setInt(4, items.size());
                ps.executeUpdate();
            }

            // 2. Insert detail per item + update stok
            String sqlDetail = "INSERT INTO detail_transaksi (nomor_struk, kode_barang, nama_barang, harga, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlStok = "UPDATE barang SET stok = stok - ? WHERE kode = ?";
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail); PreparedStatement psStok = conn.prepareStatement(sqlStok)) {
                for (ItemKeranjang item : items) {
                    Barang b = item.getBarang();
                    psDetail.setString(1, nomorStruk);
                    psDetail.setString(2, b.getKode());
                    psDetail.setString(3, b.getNama());
                    psDetail.setDouble(4, b.getHarga());
                    psDetail.setInt(5, item.getJumlah());
                    psDetail.setDouble(6, item.getSubtotal());
                    psDetail.addBatch();

                    psStok.setInt(1, item.getJumlah());
                    psStok.setString(2, b.getKode());
                    psStok.addBatch();
                }
                psDetail.executeBatch();
                psStok.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showError("Gagal menyimpan transaksi", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // DATA UNTUK LAPORAN
    // =========================================================
    /**
     * Total pendapatan hari ini
     */
    public double getTotalPenjualanHariIni() {
        String tgl = getTanggalHariIni();
        String sql = "SELECT COALESCE(SUM(total),0) AS total FROM transaksi WHERE tanggal = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tgl);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            showError("Gagal mengambil total hari ini", e);
        }
        return 0.0;
    }

    /**
     * Total seluruh transaksi
     */
    public int getTotalJumlahTransaksi() {
        String sql = "SELECT COUNT(*) AS cnt FROM transaksi";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            showError("Gagal menghitung transaksi", e);
        }
        return 0;
    }

    /**
     * Total pendapatan semua waktu
     */
    public double getTotalSemuaPendapatan() {
        String sql = "SELECT COALESCE(SUM(total),0) AS total FROM transaksi";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            showError("Gagal menghitung total pendapatan", e);
        }
        return 0.0;
    }

    /**
     * Ringkasan penjualan per tanggal. Return: List of Object[]{tanggal,
     * totalPendapatan, jumlahTransaksi, totalItem}
     */
    public List<Object[]> getRingkasanPerHari() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT tanggal, "
                + "SUM(total) AS total_pendapatan, "
                + "COUNT(*) AS jumlah_transaksi, "
                + "SUM(jumlah_item) AS total_item "
                + "FROM transaksi GROUP BY tanggal ORDER BY tanggal DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("tanggal"),
                    rs.getDouble("total_pendapatan"),
                    rs.getInt("jumlah_transaksi"),
                    rs.getInt("total_item")
                });
            }
        } catch (SQLException e) {
            showError("Gagal memuat laporan harian", e);
        }
        return list;
    }

    /**
     * Jumlah hari aktif (hari yang punya minimal 1 transaksi)
     */
    public int getJumlahHariAktif() {
        String sql = "SELECT COUNT(DISTINCT tanggal) AS cnt FROM transaksi";
        try (Statement st = DatabaseConnection.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            showError("Gagal menghitung hari aktif", e);
        }
        return 0;
    }

    /**
     * Detail transaksi untuk satu tanggal tertentu. Return: List of
     * Object[]{nomor_struk, total, jumlah_item}
     */
    public List<Object[]> getDetailTransaksiPerTanggal(String tanggal) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nomor_struk, total, jumlah_item FROM transaksi WHERE tanggal = ? ORDER BY id";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tanggal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("nomor_struk"),
                        rs.getDouble("total"),
                        rs.getInt("jumlah_item")
                    });
                }
            }
        } catch (SQLException e) {
            showError("Gagal memuat detail transaksi", e);
        }
        return list;
    }

    // =========================================================
    // UTILITAS
    // =========================================================
    public String getTanggalHariIni() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void showError(String msg, SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
                msg + "\n\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
