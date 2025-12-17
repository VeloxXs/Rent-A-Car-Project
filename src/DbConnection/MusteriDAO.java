package DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import Model.Musteri;

public class MusteriDAO {
    
    // 1. YENİ MÜŞTERİ EKLEME (INSERT)
    public Musteri addMusteri(Musteri musteri) throws SQLException {
        String sql = "INSERT INTO MUSTERILER (ad, soyad, telefon) VALUES (?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, musteri.getAd());
            pstmt.setString(2, musteri.getSoyad());
            pstmt.setString(3, musteri.getTelefon());
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int musteriId = rs.getInt(1);
                    return new Musteri(musteriId, musteri.getAd(), musteri.getSoyad(), musteri.getTelefon());
                }
            }
        }
        return musteri;
    }

    // 2. MÜŞTERİYİ ID'YE GÖRE BULMA (READ)
    public Optional<Musteri> getMusteriById(int id) throws SQLException {
         String sql = "SELECT * FROM MUSTERILER WHERE musteriId = ?";
         try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Musteri musteri = new Musteri(
                        rs.getInt("musteriId"),
                        rs.getString("ad"),
                        rs.getString("soyad"),
                        rs.getString("telefon")
                    );
                    return Optional.of(musteri);
                }
            }
        }
        return Optional.empty();
    }
    
    // 3. YENİ METOT: ÖDEME BİLGİSİ DOĞRULAMA SİMÜLASYONU
    // Bu metot, kart numarasının formatını kontrol ederek ödeme ağ geçidi rolünü üstlenir.
    // DB bağlantısı gerektirmez.
    public boolean odemeDogrula(String kartNo) {
        // Kart numarasının 16 haneli ve sadece rakamlardan oluştuğunu varsayalım.
        return kartNo != null && kartNo.length() == 16 && kartNo.matches("\\d+");
    }
}