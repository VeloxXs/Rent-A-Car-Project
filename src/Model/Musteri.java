package Model;

public class Musteri {
    private int musteriId; 
    private String ad;
    private String soyad;
    private String telefon;

    // CONSTRUCTOR 1: DB'den okuma veya ID set etme için (DAO kullanır)
    public Musteri(int musteriId, String ad, String soyad, String telefon) {
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.telefon = telefon;
    }

    // CONSTRUCTOR 2: Yeni kayıt eklerken (DB ID'yi otomatik atar)
    public Musteri(String ad, String soyad, String telefon) {
        this.ad = ad;
        this.soyad = soyad;
        this.telefon = telefon;
    }
    
    public int getMusteriId() { return musteriId; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getTelefon() { return telefon; }
    
    // DAO'dan ID'yi set etmek için (addMusteri metodunda kullanılır)
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; } 

    @Override
    public String toString() {
        return "ID: " + musteriId + 
               " | Ad Soyad: " + ad + " " + soyad + 
               " | Telefon: " + telefon;
    }
}