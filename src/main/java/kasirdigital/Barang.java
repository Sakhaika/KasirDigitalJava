package kasirdigital;

public class Barang {
    private String kode;
    private String nama;
    private double harga;
    private int stok;
    private String kategori;

    public Barang(String kode, String nama, double harga, int stok, String kategori) {
        this.kode = kode;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
        this.kategori = kategori;
    }

    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
    public String getKategori() { return kategori; }

    public void setNama(String nama) { this.nama = nama; }
    public void setHarga(double harga) { this.harga = harga; }
    public void setStok(int stok) { this.stok = stok; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public void kurangiStok(int jumlah) {
        this.stok -= jumlah;
    }

    public void tambahStok(int jumlah) {
        this.stok += jumlah;
    }

    @Override
    public String toString() {
        return nama;
    }
}