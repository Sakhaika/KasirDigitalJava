package kasirdigital;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "kasirdigital";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // default XAMPP kosong

    private static final String URL
            = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Driver MySQL tidak ditemukan!\n"
                    + "Tambahkan mysql-connector-java ke Dependencies project.",
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Gagal terhubung ke database!\n\n"
                    + "Pastikan:\n"
                    + "  1. XAMPP sudah dijalankan (Apache + MySQL)\n"
                    + "  2. Database 'kasirdigital' sudah dibuat\n"
                    + "  3. Username/password benar\n\n"
                    + "Error: " + e.getMessage(),
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
