package Model;

import java.time.LocalDate;

public class Kiralama {
    private int kiralamaId; 
    private Arac arac;
    private Musteri musteri;
    private LocalDate kiralamaTarihi;
    private LocalDate planlananIadeTarihi;
    private LocalDate gercekIadeTarihi; 
    private double toplamUcret;
    
    // YENİ ALANLAR: Ödeme sistemi için eklendi
    private double ekUcret; 
    private String odemeDurumu; // Örn: BAŞARILI, HATA vb.

    // CONSTRUCTOR 1: DB'den okuma (DAO kullanır) - YENİ PARAMETRELER EKLENDİ
    public Kiralama(int kiralamaId, Arac arac, Musteri musteri, LocalDate kiralamaTarihi, 
                    LocalDate planlananIadeTarihi, LocalDate gercekIadeTarihi, double toplamUcret,
                    double ekUcret, String odemeDurumu) { // <<< YENİ PARAMETRELER
        this.kiralamaId = kiralamaId;
        this.arac = arac;
        this.musteri = musteri;
        this.kiralamaTarihi = kiralamaTarihi;
        this.planlananIadeTarihi = planlananIadeTarihi;
        this.gercekIadeTarihi = gercekIadeTarihi;
        this.toplamUcret = toplamUcret;
        this.ekUcret = ekUcret; // Yeni
        this.odemeDurumu = odemeDurumu; // Yeni
    }

    // CONSTRUCTOR 2: Yeni kiralama oluşturma (Servis kullanır) - YENİ ALANLAR SET EDİLDİ
    public Kiralama(Arac arac, Musteri musteri, int gunSayisi, double gunlukUcret) {
        this.arac = arac;
        this.musteri = musteri;
        this.kiralamaTarihi = LocalDate.now();
        this.planlananIadeTarihi = this.kiralamaTarihi.plusDays(gunSayisi);
        this.toplamUcret = gunlukUcret * gunSayisi;
        this.gercekIadeTarihi = null; 
        
        // Yeni Kiralama başlangıç değerleri
        this.ekUcret = 0.0;
        this.odemeDurumu = "BAŞARILI"; // Ödeme Servis'te kontrol edildiği için başarılı varsayılır
    }

    // Getter ve Setter Metotları
    public int getKiralamaId() { return kiralamaId; }
    public Arac getArac() { return arac; }
    public Musteri getMusteri() { return musteri; }
    public LocalDate getKiralamaTarihi() { return kiralamaTarihi; }
    public LocalDate getPlanlananIadeTarihi() { return planlananIadeTarihi; }
    public LocalDate getGercekIadeTarihi() { return gercekIadeTarihi; }
    public double getToplamUcret() { return toplamUcret; }
    
    // YENİ GETTER/SETTER METOTLARI
    public double getEkUcret() { return ekUcret; }
    public String getOdemeDurumu() { return odemeDurumu; }

    public void setKiralamaId(int kiralamaId) { this.kiralamaId = kiralamaId; }
    public void setGercekIadeTarihi(LocalDate gercekIadeTarihi) { this.gercekIadeTarihi = gercekIadeTarihi; }
    public void setToplamUcret(double toplamUcret) { this.toplamUcret = toplamUcret; }
    public void setEkUcret(double ekUcret) { this.ekUcret = ekUcret; }
    public void setOdemeDurumu(String odemeDurumu) { this.odemeDurumu = odemeDurumu; }
    
    @Override
    public String toString() {
        String durum = gercekIadeTarihi == null ? "AKTİF" : "İADE EDİLDİ (Tarih: " + gercekIadeTarihi + ")";
        String ekBilgi = "";
        
        if (gercekIadeTarihi != null && ekUcret > 0) {
             ekBilgi = String.format(" | Gecikme Ücreti: %.2f TL", ekUcret);
        }

        return String.format(
            "| ID: %d | Araç: %s (%s) | Müşteri: %s | Kir. Tarihi: %s | Plan. İade: %s | Ödendi: %.2f TL | Durum: %s%s",
            kiralamaId, 
            arac.getMarka() + " " + arac.getModel(), 
            arac.getPlaka(),
            musteri.getAd() + " " + musteri.getSoyad(),
            kiralamaTarihi, 
            planlananIadeTarihi,
            toplamUcret, // Artık toplam ücret, ek ücreti de içerebilir
            durum,
            ekBilgi
        );
    }
}