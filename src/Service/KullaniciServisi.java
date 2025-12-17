package Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KullaniciServisi {
    
    // Uygulamada kayıtlı kullanıcı listesi
    private final List<Kullanici> kullanicilar = new ArrayList<>();
    
    // Giriş yapmış kullanıcının bilgisi
    private Kullanici oturumdakiKullanici = null; 

    public KullaniciServisi() {
        // Örnek kullanıcıları sisteme ekleyelim (Bu bilgiler normalde veritabanında tutulur)
        kullanicilar.add(new Kullanici("admin", "1234", "Admin"));
        kullanicilar.add(new Kullanici("personel1", "5678", "Personel"));
    }

    /**
     * Kullanıcı adı ve şifre ile giriş yapmayı dener.
     * @return Giriş başarılıysa Kullanici nesnesini, başarısızsa null döndürür.
     */
    public Kullanici girisYap(String kullaniciAdi, String sifre) {
        Optional<Kullanici> bulunanKullanici = kullanicilar.stream()
            .filter(k -> k.getKullaniciAdi().equalsIgnoreCase(kullaniciAdi) && k.getSifre().equals(sifre))
            .findFirst();

        if (bulunanKullanici.isPresent()) {
            this.oturumdakiKullanici = bulunanKullanici.get(); // Oturumu başlat
            return this.oturumdakiKullanici;
        } else {
            return null;
        }
    }
    
    public void cikisYap() {
        this.oturumdakiKullanici = null;
    }

    public Kullanici getOturumdakiKullanici() {
        return oturumdakiKullanici;
    }

    public boolean girisYapilmisMi() {
        return oturumdakiKullanici != null;
    }
}
