import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// Model, Service ve DbConnection sınıflarını import et
import Model.Arac;
import Model.Kiralama;
import Model.Musteri;
import Service.Kullanici;
import Service.RentACarServisi;
import DbConnection.DbConnection;

public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static RentACarServisi servis = new RentACarServisi();
    
    // Oturum Bilgileri
    private static String kullaniciRol = null;
    private static String kullaniciAdi = null;
    
    public static void main(String[] args) {
        System.out.println("=== Rent a Car Yönetim Sistemine Hoş Geldiniz ===");
        
        // KRİTİK ADIM: Uygulama başladığında DB bağlantısını ve tabloları kontrol et/oluştur.
        DbConnection.initializeDatabase(); 
        
        // Yeni başlangıç menüsünü çağır
        baslangicMenu();
        
        // Eğer giriş başarılı olursa (kullaniciRol set edildiyse) ana menüye geç
        if (kullaniciRol != null) {
            anaMenu();
        } else {
            // Eğer kullanıcı başlangıç menüsünden (0-Çıkış) ile ayrıldıysa
            System.out.println("Uygulamadan çıkılıyor. Hoşça kalın.");
        }
    }

    // --- Başlangıç Menüsü ---
    private static void baslangicMenu() {
        boolean devam = true;
        while (devam && kullaniciRol == null) {
            System.out.println("\n--- BAŞLANGIÇ MENÜSÜ ---");
            System.out.println("1 - Giriş Yap");
            System.out.println("2 - Yeni Hesap Oluştur (Kayıt Ol)");
            System.out.println("0 - Çıkış");
            System.out.print("Seçiminiz: ");

            try {
                int secim = scanner.nextInt();
                scanner.nextLine(); 

                switch (secim) {
                    case 1:
                        login(); 
                        break;
                    case 2:
                        register(); 
                        break;
                    case 0:
                        devam = false;
                        break;
                    default:
                        System.out.println("Geçersiz seçim. Lütfen 0, 1 veya 2'yi seçin.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Hata: Lütfen geçerli bir sayı girin.");
                scanner.nextLine();
            }
        }
    }
    
    // --- Kayıt Olma İşlemi ---
    private static void register() {
        System.out.println("\n--- Yeni Hesap Oluştur ---");
        System.out.print("Kullanıcı Adı: ");
        String ad = scanner.nextLine();
        System.out.print("Şifre: ");
        String sifre = scanner.nextLine();

        if (ad.isEmpty() || sifre.isEmpty()) {
            System.out.println("HATA: Kullanıcı adı ve şifre boş bırakılamaz.");
            return;
        }

        servis.kayitOl(ad, sifre); 
    }

    private static void login() {
        System.out.println("\n--- Kullanıcı Girişi ---");
        System.out.print("Kullanıcı Adı: ");
        String ad = scanner.nextLine();
        System.out.print("Şifre: ");
        String sifre = scanner.nextLine();
        
        Optional<Kullanici> kullaniciOpt = servis.kullaniciDogrula(ad, sifre);
        
        if (kullaniciOpt.isPresent()) {
            Kullanici kullanici = kullaniciOpt.get();
            kullaniciAdi = kullanici.getKullaniciAdi();
            kullaniciRol = kullanici.getRol();
            System.out.println("\nBAŞARILI: Hoş geldiniz, " + kullaniciAdi + " (" + kullaniciRol + ")");
        } else {
            System.out.println("\nHATA: Geçersiz kullanıcı adı veya şifre.");
            kullaniciRol = null; 
        }
    }

    private static void devamEtmekIcinBekle() {
        System.out.println("\nDevam etmek için ENTER tuşuna basın...");
        try {
            scanner.nextLine(); 
        } catch (Exception e) {
            scanner.nextLine(); 
        }
    }

    private static void anaMenu() {
        boolean calisiyor = true;
        while (calisiyor) {
            System.out.println("\n--- ANA MENÜ (" + kullaniciRol + ") ---");
            System.out.println("1 - Araç Kirala");
            System.out.println("2 - Araç İade Et");
            System.out.println("3 - Müsait Araçları Listele");
            System.out.println("4 - Tüm Araçları Listele");
            System.out.println("5 - Aktif Kiralamaları Listele");
            
            if (kullaniciRol.equalsIgnoreCase("Admin")) {
                System.out.println("6 - Yeni Araç Ekle (ADMIN)");
                System.out.println("7 - Kullanıcı Yönetimi (ADMIN)"); 
            }
            
            System.out.println("0 - Çıkış Yap");
            System.out.print("Seçiminiz: ");
            
            try {
                int secim = scanner.nextInt();
                scanner.nextLine();
                
                switch (secim) {
                    case 1:
                        aracKiralaIslemi();
                        break;
                    case 2:
                        aracIadeIslemi();
                        break;
                    case 3: 
                        listeleMusaitAraclar();
                        devamEtmekIcinBekle();
                        break;
                    case 4: 
                        listeleTumAraclar();
                        devamEtmekIcinBekle();
                        break;
                    case 5: 
                        listeleAktifKiralamalar();
                        devamEtmekIcinBekle();
                        break;
                    case 6: 
                        if (kullaniciRol != null && kullaniciRol.equalsIgnoreCase("Admin")) {
                            aracEkleIslemi();
                        } else {
                            System.out.println("YETKİ HATASI: Bu işlem için yetkiniz yok.");
                        }
                        break;
                    case 7:
                        if (kullaniciRol != null && kullaniciRol.equalsIgnoreCase("Admin")) {
                            kullaniciYonetimiMenu();
                        } else {
                            System.out.println("YETKİ HATASI: Bu işlem için yetkiniz yok.");
                        }
                        break;
                    case 0:
                        calisiyor = false;
                        System.out.println("Oturum kapatıldı. İyi günler dileriz.");
                        break;
                    default:
                        System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Hata: Lütfen menüdeki karşılığı olan bir sayı girin.");
                scanner.nextLine(); 
            }
        }
    }

    // --- Admin Kullanıcı Yönetimi Menüsü ---
    private static void kullaniciYonetimiMenu() {
        boolean yonetimDevam = true;
        while (yonetimDevam) {
            System.out.println("\n--- KULLANICI YÖNETİMİ MENÜSÜ ---");
            System.out.println("1 - Tüm Kullanıcıları Listele");
            System.out.println("2 - Kullanıcı Rolü Güncelle/Ata");
            System.out.println("0 - Ana Menüye Dön");
            System.out.print("Seçiminiz: ");
            
            try {
                int secim = scanner.nextInt();
                scanner.nextLine();
                
                switch (secim) {
                    case 1:
                        listeleKullanicilar(servis.getAllKullanicilar());
                        devamEtmekIcinBekle();
                        break;
                    case 2:
                        rolGuncelleIslemi();
                        break;
                    case 0:
                        yonetimDevam = false;
                        break;
                    default:
                        System.out.println("Geçersiz seçim.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Hata: Lütfen geçerli bir sayı girin.");
                scanner.nextLine();
            }
        }
    }

    // --- Rol Güncelleme İşlemi ---
    private static void rolGuncelleIslemi() {
        System.out.println("\n--- Kullanıcı Rolü Güncelleme ---");
        listeleKullanicilar(servis.getAllKullanicilar()); 
        
        System.out.print("Rolünü değiştirmek istediğiniz kullanıcının adını girin: ");
        String hedefKullanici = scanner.nextLine();
        
        System.out.print("Yeni Rolü girin (Admin, Personel veya Müşteri): ");
        String yeniRol = scanner.nextLine();

        if (hedefKullanici.equalsIgnoreCase(kullaniciAdi)) {
            System.out.println("YETKİ HATASI: Kendi rolünüzü bu menüden değiştiremezsiniz.");
            return;
        }
        
        servis.rolGuncelle(hedefKullanici, yeniRol);
    }

    // --- Kullanıcı Listeleme Formatı ---
    private static void listeleKullanicilar(List<Kullanici> kullaniciList) {
        if (kullaniciList.isEmpty()) {
            System.out.println("\nSistemde kayıtlı kullanıcı bulunmamaktadır.");
            return;
        }
        System.out.println("\n--- TÜM KULLANICILAR ---");
        System.out.printf("%-20s %-15s\n", "KULLANICI ADI", "ROLÜ");
        System.out.println("------------------------------------");
        kullaniciList.forEach(k -> 
            System.out.printf("%-20s %-15s\n", k.getKullaniciAdi(), k.getRol()));
    }
    
    // --- Menü İşlemlerini Yöneten Metotlar ---

    private static void listeleMusaitAraclar() {
        List<Arac> musaitAraclar = servis.getMusaitAraclar();
        if (musaitAraclar.isEmpty()) {
            System.out.println("\nŞu anda kiralanabilir müsait araç bulunmamaktadır.");
        } else {
            System.out.println("\n--- MÜSAİT ARAÇLAR ---");
            musaitAraclar.forEach(System.out::println);
        }
    }

    private static void listeleTumAraclar() {
        List<Arac> tumAraclar = servis.getAllAraclar();
        if (tumAraclar.isEmpty()) {
            System.out.println("\nSistemde kayıtlı araç bulunmamaktadır.");
        } else {
            System.out.println("\n--- TÜM ARAÇLAR ---");
            tumAraclar.forEach(System.out::println);
        }
    }

    private static void listeleAktifKiralamalar() {
        List<Kiralama> aktifKiralamalar = servis.getAktifKiralamalar();
        if (aktifKiralamalar.isEmpty()) {
            System.out.println("\nŞu anda aktif kiralama kaydı bulunmamaktadır.");
        } else {
            System.out.println("\n--- AKTİF KİRALAMALAR ---");
            aktifKiralamalar.forEach(System.out::println);
        }
    }
    
    // --- CRUD İşlemleri ---

    private static void aracEkleIslemi() {
        System.out.println("\n--- Yeni Araç Ekleme ---");
        System.out.print("Plaka: ");
        String plaka = scanner.nextLine().toUpperCase();
        System.out.print("Marka: ");
        String marka = scanner.nextLine();
        System.out.print("Model: ");
        String model = scanner.nextLine();
        System.out.print("Günlük Ücret (ör: 550.0): ");
        double ucret;
        try {
            ucret = scanner.nextDouble();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("HATA: Ücret bilgisi geçersiz. İşlem iptal edildi.");
            scanner.nextLine();
            return;
        }

        Arac yeniArac = new Arac(plaka, marka, model, ucret);
        servis.aracEkle(yeniArac, kullaniciRol);
    }
    
    private static void aracKiralaIslemi() {
        // 1. Müşteri Bilgilerini Al
        Musteri musteri = musteriBilgileriniAl();
        if (musteri == null) return;
        
        // 2. Kiralanacak Aracı Seç
        listeleMusaitAraclar(); // Müsait araçları göster
        System.out.print("\nKiralamak istediğiniz aracın plakasını girin (İptal için 0): ");
        String plaka = scanner.nextLine().toUpperCase();
        if (plaka.equals("0")) {
            System.out.println("Kiralama işlemi iptal edildi.");
            return;
        }
        
        // 3. Kira Gün Sayısını Al
        System.out.print("Kaç gün kiralamak istiyorsunuz? (Sayı girin): ");
        int gunSayisi;
        try {
            gunSayisi = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("HATA: Gün sayısı geçersiz. İşlem iptal edildi.");
            scanner.nextLine();
            return;
        }
        
        // 4. ÖDEME BİLGİSİNİ AL (YENİ ADIM)
        System.out.println("\n--- Ödeme Bilgileri (16 haneli herhangi bir sayı girin) ---");
        System.out.print("Kredi Kartı Numarası (16 Hane): ");
        String kartNo = scanner.nextLine();
        
        // 5. Kiralama İşlemini Yap (Servisi yeni imza ile çağır)
        servis.aracKirala(plaka, musteri, gunSayisi, kartNo); // <<< YENİ PARAMETRELER EKLENDİ
    }

    private static Musteri musteriBilgileriniAl() {
        System.out.println("\n--- Müşteri Bilgileri ---");
        System.out.print("Ad: ");
        String ad = scanner.nextLine();
        System.out.print("Soyad: ");
        String soyad = scanner.nextLine();
        System.out.print("Telefon: ");
        String telefon = scanner.nextLine();
        
        // Müşteriyi DB'ye kaydet ve ID'li objeyi geri al
        return servis.musteriEkle(ad, soyad, telefon);
    }
    
    private static void aracIadeIslemi() {
        listeleAktifKiralamalar(); // Aktif kiralamaları göster
        
        if (servis.getAktifKiralamalar().isEmpty()) {
            return; // Aktif kiralama yoksa devam etme
        }
        
        System.out.print("\nİade edilecek aracın plakasını girin (İptal için 0): ");
        String plaka = scanner.nextLine().toUpperCase();
        if (plaka.equals("0")) {
            System.out.println("İade işlemi iptal edildi.");
            return;
        }

        servis.aracIadeEt(plaka);
    }
}