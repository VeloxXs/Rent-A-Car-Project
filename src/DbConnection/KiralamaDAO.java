package DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Arac;
import Model.Kiralama;
import Model.Musteri;

public class KiralamaDAO {

    private final AracDAO aracDAO;
    private final MusteriDAO musteriDAO;

    public KiralamaDAO(AracDAO aracDAO, MusteriDAO musteriDAO) {
        this.aracDAO = aracDAO;
        this.musteriDAO = musteriDAO;
    }

    private Date convertToSqlDate(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }

    // 1. KİRALAMA EKLEME (ADD)
    // SQL'e eklenen sütunlar: ekUcret, odemeDurumu
    public Kiralama addKiralama(Kiralama kiralama) throws SQLException {
        String sql = "INSERT INTO KIRALAMALAR (aracPlaka, musteriId, kiralamaTarihi, planlananIadeTarihi, toplamUcret, ekUcret, odemeDurumu) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, kiralama.getArac().getPlaka());
            pstmt.setInt(2, kiralama.getMusteri().getMusteriId());
            pstmt.setDate(3, convertToSqlDate(kiralama.getKiralamaTarihi()));
            pstmt.setDate(4, convertToSqlDate(kiralama.getPlanlananIadeTarihi()));
            pstmt.setDouble(5, kiralama.getToplamUcret());
            
            // YENİ: Başlangıçta ek ücret 0.0, ödeme durumu BAŞARILI (Modelden alabiliriz veya hardcode edebiliriz)
            pstmt.setDouble(6, kiralama.getEkUcret()); // Modelden 0.0 çekilir
            pstmt.setString(7, kiralama.getOdemeDurumu()); // Modelden "BAŞARILI" çekilir
            
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    kiralama.setKiralamaId(rs.getInt(1));
                }
            }
        }
        return kiralama;
    }
    
    // 2. AKTİF KİRALAMALARI LİSTELEME (READ)
    public List<Kiralama> getAktifKiralamalar() throws SQLException {
        List<Kiralama> aktifKiralamalar = new ArrayList<>();
        String sql = "SELECT * FROM KIRALAMALAR WHERE gercekIadeTarihi IS NULL"; 
        
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Optional<Arac> aracOpt = aracDAO.getAracByPlaka(rs.getString("aracPlaka"));
                Optional<Musteri> musteriOpt = musteriDAO.getMusteriById(rs.getInt("musteriId"));
                
                if (aracOpt.isPresent() && musteriOpt.isPresent()) {
                    
                    Date gercekIadeSqlDate = rs.getDate("gercekIadeTarihi");
                    LocalDate gercekIadeTarihi = gercekIadeSqlDate != null ? gercekIadeSqlDate.toLocalDate() : null;
                    
                    // YENİ CONSTRUCTOR KULLANILDI (9 parametreli)
                    Kiralama kiralama = new Kiralama(
                        rs.getInt("kiralamaId"),
                        aracOpt.get(), 
                        musteriOpt.get(),
                        rs.getDate("kiralamaTarihi").toLocalDate(),
                        rs.getDate("planlananIadeTarihi").toLocalDate(),
                        gercekIadeTarihi,
                        rs.getDouble("toplamUcret"),
                        rs.getDouble("ekUcret"), // YENİ
                        rs.getString("odemeDurumu") // YENİ
                    );
                    aktifKiralamalar.add(kiralama);
                }
            }
        }
        return aktifKiralamalar;
    }
    
    // 3. PLAKAYA GÖRE AKTİF KİRALAMA BULMA (READ)
    public Optional<Kiralama> findAktifKiralamaByPlaka(String plaka) throws SQLException {
        String sql = "SELECT * FROM KIRALAMALAR WHERE aracPlaka = ? AND gercekIadeTarihi IS NULL";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaka);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                     Optional<Arac> aracOpt = aracDAO.getAracByPlaka(plaka);
                     Optional<Musteri> musteriOpt = musteriDAO.getMusteriById(rs.getInt("musteriId"));
                     
                     if (aracOpt.isPresent() && musteriOpt.isPresent()) {
                         
                         Date gercekIadeSqlDate = rs.getDate("gercekIadeTarihi");
                         LocalDate gercekIadeTarihi = gercekIadeSqlDate != null ? gercekIadeSqlDate.toLocalDate() : null;
                         
                         // YENİ CONSTRUCTOR KULLANILDI (9 parametreli)
                         Kiralama kiralama = new Kiralama(
                            rs.getInt("kiralamaId"),
                            aracOpt.get(), 
                            musteriOpt.get(),
                            rs.getDate("kiralamaTarihi").toLocalDate(),
                            rs.getDate("planlananIadeTarihi").toLocalDate(),
                            gercekIadeTarihi, 
                            rs.getDouble("toplamUcret"),
                            rs.getDouble("ekUcret"), // YENİ
                            rs.getString("odemeDurumu") // YENİ
                         );
                         return Optional.of(kiralama);
                     }
                }
            }
        }
        return Optional.empty();
    }
    
    // 4. KİRALAMA GÜNCELLEME (UPDATE) - İADE İŞLEMİ
    // Metot imzası ekUcret parametresi alacak şekilde değiştirildi
    public void updateKiralamaForIade(int kiralamaId, LocalDate gercekIadeTarihi, double ekUcret) throws SQLException { 
        // SQL'de ekUcret sütununu güncelle ve toplamUcret'e ek ücreti ekle
        String sql = "UPDATE KIRALAMALAR SET gercekIadeTarihi = ?, ekUcret = ?, toplamUcret = toplamUcret + ? WHERE kiralamaId = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, convertToSqlDate(gercekIadeTarihi)); 
            pstmt.setDouble(2, ekUcret); // Hesaplanan ek ücreti kaydet
            pstmt.setDouble(3, ekUcret); // Toplam ücrete ekle (toplamUcret = toplamUcret + ekUcret)
            pstmt.setInt(4, kiralamaId);
            pstmt.executeUpdate();
        }
    }
}