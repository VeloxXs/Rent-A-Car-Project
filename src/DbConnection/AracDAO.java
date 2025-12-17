package DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import Model.Arac;

public class AracDAO {
    
    // addArac metodu doğru, değişmedi
    public void addArac(Arac arac) throws SQLException {
        String sql = "INSERT INTO ARACLAR (plaka, marka, model, gunlukUcret, kiradaMi) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, arac.getPlaka());
            pstmt.setString(2, arac.getMarka());
            pstmt.setString(3, arac.getModel());
            pstmt.setDouble(4, arac.getGunlukUcret());
            pstmt.setBoolean(5, arac.isKiradaMi());
            pstmt.executeUpdate();
        }
    }

    public List<Arac> getAllAraclar() throws SQLException {
        List<Arac> aracList = new ArrayList<>();
        String sql = "SELECT * FROM ARACLAR";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
                        
            while (rs.next()) {
                
                Arac arac = new Arac(
                    rs.getString("plaka"),
                    rs.getString("marka"),
                    rs.getString("model"),
                    rs.getDouble("gunlukUcret")
                );
                arac.setKiradaMi(rs.getBoolean("kiradaMi")); 
                aracList.add(arac);
            }

            
        } 
        return aracList;
    }
    
    // getAracByPlaka metodu doğru, değişmedi
    public Optional<Arac> getAracByPlaka(String plaka) throws SQLException {
        String sql = "SELECT * FROM ARACLAR WHERE plaka = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plaka);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Arac arac = new Arac(
                        rs.getString("plaka"),
                        rs.getString("marka"),
                        rs.getString("model"),
                        rs.getDouble("gunlukUcret")
                    );
                    arac.setKiradaMi(rs.getBoolean("kiradaMi"));
                    return Optional.of(arac);
                }
            }
        }
        return Optional.empty();
    }
    
    // updateAracDurumu metodu doğru, değişmedi
    public void updateAracDurumu(String plaka, boolean kiradaMi) throws SQLException {
        String sql = "UPDATE ARACLAR SET kiradaMi = ? WHERE plaka = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, kiradaMi);
            pstmt.setString(2, plaka);
            pstmt.executeUpdate();
        }
    }
}