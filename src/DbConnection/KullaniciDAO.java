package DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Statement importunu ekledik
import java.util.ArrayList; // ArrayList importunu ekledik
import java.util.List; // List importunu ekledik
import java.util.Optional;
import Service.Kullanici; // Kullanici modelini import ettik

public class KullaniciDAO {
    
    // 1. Kullanıcı Adına Göre Bulma (Giriş Yapma Kontrolü)
    public Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi) throws SQLException {
        String sql = "SELECT * FROM KULLANICILAR WHERE kullaniciAdi = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, kullaniciAdi);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Kullanici kullanici = new Kullanici(
                        rs.getString("kullaniciAdi"),
                        rs.getString("sifre"),
                        rs.getString("rol")
                    );
                    return Optional.of(kullanici);
                }
            }
        }
        return Optional.empty();
    }

    // 2. Yeni Kullanıcı Ekleme (Kayıt Olma)
    public void addKullanici(Kullanici kullanici) throws SQLException {
        String sql = "INSERT INTO KULLANICILAR (kullaniciAdi, sifre, rol) VALUES (?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, kullanici.getKullaniciAdi());
            pstmt.setString(2, kullanici.getSifre());
            pstmt.setString(3, kullanici.getRol());
            pstmt.executeUpdate();
        }
    } // addKullanici metodu bitti

    // 3. Kullanıcı Rolünü Güncelleme (Admin Yetkilendirmesi)
    public void updateKullaniciRol(String kullaniciAdi, String yeniRol) throws SQLException {
        String sql = "UPDATE KULLANICILAR SET rol = ? WHERE kullaniciAdi = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, yeniRol);
            pstmt.setString(2, kullaniciAdi);
            pstmt.executeUpdate();
        }
    } 

    // 4. Tüm Kullanıcıları Listeleme (Admin Menüsü)
    public List<Kullanici> getAllKullanicilar() throws SQLException {
        List<Kullanici> kullaniciList = new ArrayList<>();
        String sql = "SELECT kullaniciAdi, rol FROM KULLANICILAR"; // Güvenlik nedeniyle şifre çekilmez
        
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Not: Şifre DB'den çekilmediği için geçici olarak boş bırakılabilir.
                Kullanici k = new Kullanici(
                    rs.getString("kullaniciAdi"), 
                    "", // Şifre bilgisi boş bırakılır
                    rs.getString("rol")
                ); 
                kullaniciList.add(k);
            }
        }
        return kullaniciList;
    }
}