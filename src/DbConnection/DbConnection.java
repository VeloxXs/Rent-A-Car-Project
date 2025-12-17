package DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet; // ResultSet'i import etmeyi unutmayalım
import java.sql.SQLException;
import java.sql.Statement;

public class DbConnection {
    
    // -------------------------------------------------------------
    // H2 Embedded DB Bağlantı Bilgileri (KURULUMSUZ ÇÖZÜM)
    // -------------------------------------------------------------
    private static final String DB_URL = "jdbc:h2:./rentacar_db"; 
    private static final String USER = "sa"; 
    private static final String PASS = ""; 
    // -------------------------------------------------------------

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
    
    // Veri olup olmadığını kontrol eden güvenli yardımcı metot
    private static boolean isTableEmpty(Statement stmt, String tableName) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;
        }
    }
    
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Kolay geliştirme için eski tabloları sil
            stmt.execute("DROP TABLE IF EXISTS KIRALAMALAR");
            stmt.execute("DROP TABLE IF EXISTS MUSTERILER");
            stmt.execute("DROP TABLE IF EXISTS ARACLAR");
            stmt.execute("DROP TABLE IF EXISTS KULLANICILAR");
            
            // 1. Tabloları oluştur (H2 uyumlu SQL)
            
            String createAraclar = "CREATE TABLE IF NOT EXISTS ARACLAR ("
                                 + "plaka VARCHAR(20) PRIMARY KEY, "
                                 + "marka VARCHAR(50) NOT NULL, "
                                 + "model VARCHAR(50) NOT NULL, "
                                 + "gunlukUcret DOUBLE NOT NULL, "
                                 + "kiradaMi BOOLEAN NOT NULL DEFAULT FALSE)";
            stmt.execute(createAraclar);
            
            String createMusteriler = "CREATE TABLE IF NOT EXISTS MUSTERILER ("
                                    + "musteriId INT PRIMARY KEY AUTO_INCREMENT, "
                                    + "ad VARCHAR(50) NOT NULL, "
                                    + "soyad VARCHAR(50) NOT NULL, "
                                    + "telefon VARCHAR(20))";
            stmt.execute(createMusteriler);

            String createKullanicilar = "CREATE TABLE IF NOT EXISTS KULLANICILAR ("
                                      + "kullaniciAdi VARCHAR(50) PRIMARY KEY, "
                                      + "sifre VARCHAR(50) NOT NULL, "
                                      + "rol VARCHAR(20) NOT NULL)";
            stmt.execute(createKullanicilar);
            
            String createKiralamalar = "CREATE TABLE IF NOT EXISTS KIRALAMALAR ("
                                    + "kiralamaId INT PRIMARY KEY AUTO_INCREMENT, "
                                    + "aracPlaka VARCHAR(20) NOT NULL, "
                                    + "musteriId INT NOT NULL, "
                                    + "kiralamaTarihi DATE NOT NULL, "
                                    + "planlananIadeTarihi DATE NOT NULL, "
                                    + "gercekIadeTarihi DATE, "
                                    + "toplamUcret DOUBLE NOT NULL, "
                                    + "ekUcret DOUBLE NOT NULL DEFAULT 0.0, "
                                    + "odemeDurumu VARCHAR(50) NOT NULL, "
                                    + "FOREIGN KEY (aracPlaka) REFERENCES ARACLAR(plaka), "
                                    + "FOREIGN KEY (musteriId) REFERENCES MUSTERILER(musteriId))";
            stmt.execute(createKiralamalar);

            
            // 2. Başlangıç verilerini (SEED DATA) ekle 
           
            // KULLANICILAR tablosunda veri yoksa kontrolü (Yardımcı metot kullanıldı)
            if (isTableEmpty(stmt, "KULLANICILAR")) {
                stmt.executeUpdate("INSERT INTO KULLANICILAR (kullaniciAdi, sifre, rol) VALUES ('admin', '1234', 'Admin')");
                stmt.executeUpdate("INSERT INTO KULLANICILAR (kullaniciAdi, sifre, rol) VALUES ('personel1', '5678', 'Personel')");
                System.out.println("Başlangıç kullanıcıları eklendi.");
            }
           
            // ARACLAR tablosunda veri yoksa kontrolü
            if (isTableEmpty(stmt, "ARACLAR")) {
                stmt.executeUpdate("INSERT INTO ARACLAR (plaka, marka, model, gunlukUcret, kiradaMi) VALUES ('34ABC11', 'Renault', 'Clio', 450.0, FALSE)");
                stmt.executeUpdate("INSERT INTO ARACLAR (plaka, marka, model, gunlukUcret, kiradaMi) VALUES ('06XYZ22', 'Fiat', 'Egea', 500.0, FALSE)");
                stmt.executeUpdate("INSERT INTO ARACLAR (plaka, marka, model, gunlukUcret, kiradaMi) VALUES ('35DFG33', 'Ford', 'Focus', 650.0, FALSE)");
                System.out.println("Başlangıç araç listesi eklendi.");
            }

            System.out.println("Tüm tablolar oluşturuldu/doğrulandı (H2 Embedded).");

        } catch (SQLException e) {
            System.err.println("Tablolar oluşturulurken kritik hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}