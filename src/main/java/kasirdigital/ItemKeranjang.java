package kasirdigital;

public class ItemKeranjang {
    private Barang barang;
    private int jumlah;

    public ItemKeranjang(Barang barang, int jumlah) {
        this.barang = barang;
        this.jumlah = jumlah;
    }

    public Barang getBarang() { return barang; }
    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }

    public double getSubtotal() {
        return barang.getHarga() * jumlah;
    }
}