package Service;

public class Kullanici {
    private String kullaniciAdi;
    private String sifre;
    private String rol; // Admin, Personel

    public Kullanici(String kullaniciAdi, String sifre, String rol) {
        this.kullaniciAdi = kullaniciAdi;
        this.sifre = sifre;
        this.rol = rol;
    }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public String getSifre() { return sifre; }
    public String getRol() { return rol; }
}