package Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import DbConnection.AracDAO;
import DbConnection.KiralamaDAO;
import DbConnection.KullaniciDAO;
import DbConnection.MusteriDAO;
import Model.Arac;
import Model.Kiralama;
import Model.Musteri;

public class RentACarServisi {
    
    // DAO'ları Tanımla
    private final AracDAO aracDAO = new AracDAO();
    private final MusteriDAO musteriDAO = new MusteriDAO();
    private final KullaniciDAO kullaniciDAO = new KullaniciDAO(); 
    private final KiralamaDAO kiralamaDAO = new KiralamaDAO(aracDAO, musteriDAO); 
    
    // --- KULLANICI İŞLEMLERİ (YETKİLENDİRME) ---
    
    public Optional<Kullanici> kullaniciDogrula(String kullaniciAdi, String sifre) {
        try {
            Optional<Kullanici> kullaniciOpt = kullaniciDAO.findByKullaniciAdi(kullaniciAdi);
            if (kullaniciOpt.isPresent() && kullaniciOpt.get().getSifre().equals(sifre)) {
                return kullaniciOpt;
            }
        } catch (SQLException e) {
            System.err.println("DB HATASI (Kullanıcı Doğrulama): " + e.getMessage());
        }
        return Optional.empty();
    }

    // Yeni kullanıcıyı kaydetme metodu (Register)
    public boolean kayitOl(String kullaniciAdi, String sifre) {
        try {
            if (kullaniciDAO.findByKullaniciAdi(kullaniciAdi).isPresent()) {
                System.out.println("HATA: Bu kullanıcı adı zaten sistemde kayıtlı.");
                return false;
            }

            // Yeni kayıtlar direkt MÜŞTERİ olarak kaydedilir.
            Kullanici yeniKullanici = new Kullanici(kullaniciAdi, sifre, "Müşteri"); 
            
            kullaniciDAO.addKullanici(yeniKullanici);
            System.out.println("\nBAŞARILI: Kullanıcı " + kullaniciAdi + " sisteme MÜŞTERİ olarak kaydedildi.");
            System.out.println("Lütfen 1. Menüden Giriş Yap seçeneğiyle devam edin.");
            return true;
            
        } catch (SQLException e) {
            System.err.println("DB HATASI (Kayıt Olma): " + e.getMessage());
            return false;
        }
    }
    
    // YENİ METOT: Kullanıcı rolünü güncelleme (Admin Menüsü için)
    public boolean rolGuncelle(String hedefKullaniciAdi, String yeniRol) {
        // Rol isminin geçerliliğini kontrol et
        if (!yeniRol.equalsIgnoreCase("Admin") && !yeniRol.equalsIgnoreCase("Personel") && !yeniRol.equalsIgnoreCase("Müşteri")) {
            System.out.println("HATA: Geçersiz rol (Admin, Personel, Müşteri olmalı).");
            return false;
        }
        
        try {
            // Hedef kullanıcının varlığını kontrol et
            if (kullaniciDAO.findByKullaniciAdi(hedefKullaniciAdi).isEmpty()) {
                System.out.println("HATA: Belirtilen kullanıcı adı sistemde bulunamadı.");
                return false;
            }
            
            kullaniciDAO.updateKullaniciRol(hedefKullaniciAdi, yeniRol);
            System.out.println("BAŞARILI: " + hedefKullaniciAdi + " kullanıcısının rolü " + yeniRol + " olarak güncellendi.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB HATASI (Rol Güncelleme): " + e.getMessage());
            return false;
        }
    }

    // YENİ METOT: Tüm kullanıcıları çekme (Admin Menüsü için)
    public List<Kullanici> getAllKullanicilar() {
        try {
            return kullaniciDAO.getAllKullanicilar();
        } catch (SQLException e) {
            System.err.println("DB HATASI (Kullanıcı Listesi Çekme): " + e.getMessage());
            return new ArrayList<>();
        }
    }


    // --- ARAÇ İŞLEMLERİ ---
    
    // Case 3: Müsait araçları DB'den çeker ve filtreler
    public List<Arac> getMusaitAraclar() {
        List<Arac> tumAraclar = getAllAraclar();
        return tumAraclar.stream()
                         .filter(arac -> !arac.isKiradaMi())
                         .collect(Collectors.toList());
    }
    
    // Case 4: Tüm araçları DB'den çeker
    public List<Arac> getAllAraclar() {
        try {
            return aracDAO.getAllAraclar();
        } catch (SQLException e) {
            System.err.println("DB HATASI (Tüm Araçlar): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); 
        }
    }
    
    // Araç bulma helper metodu
    private Optional<Arac> araciBul(String plaka) {
        try {
            return aracDAO.getAracByPlaka(plaka);
        } catch (SQLException e) {
            System.err.println("DB HATASI (Araç Bulma): " + e.getMessage());
            return Optional.empty();
        }
    }

    // Case 6: Araç ekleme
    public boolean aracEkle(Arac yeniArac, String kullaniciRolü) {
        if (!kullaniciRolü.equalsIgnoreCase("Admin")) {
            System.out.println("YETKİ HATASI: Yeni araç ekleme yetkisi sadece Admin rolüne aittir.");
            return false;
        }

        try {
            if (aracDAO.getAracByPlaka(yeniArac.getPlaka()).isPresent()) {
                System.out.println("HATA: Bu plakada bir araç zaten sistemde kayıtlı.");
                return false;
            }
            aracDAO.addArac(yeniArac);
            System.out.println("BAŞARILI: " + yeniArac.getPlaka() + " plakalı araç sisteme eklendi.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB HATASI (Araç Ekleme): " + e.getMessage());
            return false;
        }
    }
    
    // --- MÜŞTERİ İŞLEMLERİ ---
    
    public Musteri musteriEkle(String ad, String soyad, String telefon) {
        Musteri musteri = new Musteri(ad, soyad, telefon);
        try {
            return musteriDAO.addMusteri(musteri);
        } catch (SQLException e) {
            System.err.println("DB HATASI (Müşteri Ekleme): " + e.getMessage());
            // Hata durumunda ID'si belirlenmemiş objeyi döndür, kiralama iptal edilir.
            return musteri; 
        }
    }
    
    // --- KİRALAMA İŞLEMLERİ ---
    
    // Case 1: Araç Kiralama (Şimdi Ödeme Bilgisi Alacak)
    public boolean aracKirala(String plaka, Musteri musteri, int gunSayisi, String kartNo) { // <<< METOT İMZASI DEĞİŞTİ
        try {
            Optional<Arac> aracOpt = araciBul(plaka);
            
            if (aracOpt.isEmpty() || aracOpt.get().isKiradaMi()) {
                System.out.println("HATA: Kiralanabilir müsait araç bulunamadı.");
                return false;
            }
            
            Arac arac = aracOpt.get();
            
            // 1. ÖDEME KONTROLÜ
            if (!musteriDAO.odemeDogrula(kartNo)) {
                System.out.println("HATA: Kredi kartı bilgileri geçersiz veya işlem başarısız oldu. Kiralama iptal edildi.");
                return false;
            }
            
            // 2. KİRALAMA İŞLEMİNİ YAP
            Kiralama yeniKiralama = new Kiralama(arac, musteri, gunSayisi, arac.getGunlukUcret());
            yeniKiralama = kiralamaDAO.addKiralama(yeniKiralama); // DAO, odemeDurumu ve ekUcret'i DB'ye kaydeder.
            aracDAO.updateAracDurumu(plaka, true); // Aracın durumunu güncelle
            
            System.out.println("\n*** KİRALAMA BAŞARILI (Kira ID: " + yeniKiralama.getKiralamaId() + ") ***");
            System.out.println("Planlanan İade Tarihi: " + yeniKiralama.getPlanlananIadeTarihi());
            System.out.printf("TOPLAM ÖN ÖDEME: %.2f TL (Ödeme Başarılı)\n", yeniKiralama.getToplamUcret());
            return true;
        } catch (SQLException e) {
            System.err.println("DB HATASI (Araç Kiralama): " + e.getMessage());
            return false;
        }
    }

    // Case 2: Araç İade Etme (Şimdi Gecikme Ücreti Hesaplayacak ve DB'ye Kaydedecek)
    public boolean aracIadeEt(String plaka) {
        try {
            Optional<Arac> aracOpt = araciBul(plaka);
            if (aracOpt.isEmpty() || !aracOpt.get().isKiradaMi()) {
                 System.out.println("HATA: Bu plakada araç bulunamadı veya kirada değil.");
                 return false;
            }
            
            Optional<Kiralama> aktifKiralamaOpt = kiralamaDAO.findAktifKiralamaByPlaka(plaka);

            if (aktifKiralamaOpt.isEmpty()) {
                System.out.println("KRİTİK HATA: Araç kirada görünüyor ama aktif kiralama kaydı yok.");
                return false;
            }

            Kiralama aktifKiralama = aktifKiralamaOpt.get();
            LocalDate bugununTarihi = LocalDate.now();
            
            // 1. GECİKME KONTROLÜ ve ÜCRET HESAPLAMA
            // ChronoUnit.DAYS ile iki tarih arasındaki farkı gün olarak hesapla.
            long gecikmeGunSayisi = java.time.temporal.ChronoUnit.DAYS.between(aktifKiralama.getPlanlananIadeTarihi(), bugununTarihi);
            double ekUcret = 0.0;
            
            if (gecikmeGunSayisi > 0) {
                // Gecikme varsa, aracın günlük ücreti üzerinden ek ücret hesapla
                ekUcret = gecikmeGunSayisi * aktifKiralama.getArac().getGunlukUcret();
                
                System.out.println("\n*** UYARI: PLANLANAN İADE TARİHİ AŞILDI! ***");
                System.out.printf("Gecikme: %d gün. Ek Ücret: %.2f TL\n", gecikmeGunSayisi, ekUcret);
                System.out.println("Ek ücret tahsilatı başarılı oldu (Simülasyon).");
                
            } else {
                 ekUcret = 0.0; // Erken veya tam zamanında iadeyse ücret 0'dır.
            }

            // 2. KİRALAMA KAYITLARINI DB'de GÜNCELLE
            // DAO'ya İade Tarihi ve Hesaplanan Ek Ücreti gönderiyoruz
            kiralamaDAO.updateKiralamaForIade(aktifKiralama.getKiralamaId(), bugununTarihi, ekUcret);
            aracDAO.updateAracDurumu(plaka, false); // Aracın durumunu müsait yap
            
            System.out.println("\n*** İADE İŞLEMİ TAMAMLANDI ***");
            System.out.printf("Tahsil Edilen Ek Ücret: %.2f TL\n", ekUcret);
            System.out.println(plaka + " plakalı araç müsait duruma geçmiştir.");
            return true;
        } catch (SQLException e) {
            System.err.println("DB HATASI (Araç İade Etme): " + e.getMessage());
            return false;
        }
    }
    
    // Case 5: Aktif kiralamaları listele
    public List<Kiralama> getAktifKiralamalar() {
         try {
            return kiralamaDAO.getAktifKiralamalar();
        } catch (SQLException e) {
            System.err.println("DB HATASI (Aktif Kiralamalar): " + e.getMessage());
            return new ArrayList<>();
        }
    }
}