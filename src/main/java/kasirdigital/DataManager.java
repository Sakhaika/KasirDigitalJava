package kasirdigital;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataManager {

    private static DataManager instance;
    private int nomorStruk = 1;

    private DataManager() {
        initNomorStruk();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // =============================================
    //  INISIALISASI nomor struk dari DB
    // =============================================
    private void initNomorStruk() {
        String sql = "SELECT COUNT(*) FROM transaksi";
        try (Connection con = DatabaseConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                nomorStruk = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================================
    //  BARANG
    // =============================================
    public List<Barang> getDaftarBarang() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT kode, nama, harga, stok, kategori FROM barang ORDER BY kode";
        try (Connection con = DatabaseConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Barang(
                        rs.getString("kode"),
                        rs.getString("nama"),
                        rs.getDouble("harga"),
                        rs.getInt("stok"),
                        rs.getString("kategori")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Barang cariBarang(String kode) {
        String sql = "SELECT kode, nama, harga, stok, kategori FROM barang WHERE kode = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Barang(
                            rs.getString("kode"),
                            rs.getString("nama"),
                            rs.getDouble("harga"),
                            rs.getInt("stok"),
                            rs.getString("kategori")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void tambahBarang(Barang b) {
        String sql = "INSERT INTO barang (kode, nama, harga, stok, kategori) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getKode());
            ps.setString(2, b.getNama());
            ps.setDouble(3, b.getHarga());
            ps.setInt(4, b.getStok());
            ps.setString(5, b.getKategori());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBarang(Barang b) {
        String sql = "UPDATE barang SET nama=?, harga=?, stok=?, kategori=? WHERE kode=?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getNama());
            ps.setDouble(2, b.getHarga());
            ps.setInt(3, b.getStok());
            ps.setString(4, b.getKategori());
            ps.setString(5, b.getKode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void hapusBarang(String kode) {
        String sql = "DELETE FROM barang WHERE kode = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void kurangiStokBarang(String kode, int jumlah) {
        String sql = "UPDATE barang SET stok = stok - ? WHERE kode = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tambahStokBarang(String kode, int jumlah) {
        String sql = "UPDATE barang SET stok = stok + ? WHERE kode = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, kode);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================================
    //  TRANSAKSI
    // =============================================
    public String getNomorStrukBerikutnya() {
        return String.format("TRX-%04d", nomorStruk++);
    }

    public void simpanTransaksi(String nomorStruk, String tanggal, double total,
            List<ItemKeranjang> items) {
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setAutoCommit(false);

            String sqlHeader = "INSERT INTO transaksi (nomor_struk, tanggal, total, jumlah_item) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlHeader)) {
                ps.setString(1, nomorStruk);
                ps.setString(2, tanggal);
                ps.setDouble(3, total);
                ps.setInt(4, items.size());
                ps.executeUpdate();
            }

            String sqlDetail = "INSERT INTO detail_transaksi (nomor_struk, kode_barang, nama_barang, harga, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                for (ItemKeranjang item : items) {
                    ps.setString(1, nomorStruk);
                    ps.setString(2, item.getBarang().getKode());
                    ps.setString(3, item.getBarang().getNama());
                    ps.setDouble(4, item.getBarang().getHarga());
                    ps.setInt(5, item.getJumlah());
                    ps.setDouble(6, item.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Kurangi stok setiap item di database
            String sqlStok = "UPDATE barang SET stok = stok - ? WHERE kode = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlStok)) {
                for (ItemKeranjang item : items) {
                    ps.setInt(1, item.getJumlah());
                    ps.setString(2, item.getBarang().getKode());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =============================================
    //  LAPORAN
    // =============================================
    public Map<String, Double> getPenjualanHarian() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT tanggal, SUM(total) as total_hari FROM transaksi GROUP BY tanggal ORDER BY tanggal DESC";
        try (Connection con = DatabaseConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("tanggal"), rs.getDouble("total_hari"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<String> getRiwayatTransaksi() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT tanggal, total, jumlah_item FROM transaksi ORDER BY created_at DESC";
        try (Connection con = DatabaseConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("tanggal") + "|" + rs.getDouble("total") + "|" + rs.getInt("jumlah_item"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getTotalPenjualanHariIni() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM transaksi WHERE tanggal = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, getTanggalHariIni());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalTransaksiHariIni() {
        String sql = "SELECT COUNT(*) FROM transaksi WHERE tanggal = ?";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, getTanggalHariIni());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =============================================
    //  UTILITY
    // =============================================
    public String getTanggalHariIni() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
