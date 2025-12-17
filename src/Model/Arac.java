package Model;

public class Arac {
    private String plaka;
    private String marka;
    private String model;
    private double gunlukUcret;
    private boolean kiradaMi; 

    public Arac(String plaka, String marka, String model, double gunlukUcret) {
        this.plaka = plaka;
        this.marka = marka;
        this.model = model;
        this.gunlukUcret = gunlukUcret;
        this.kiradaMi = false; 
    }

    // Getter ve Setter Metotları
    public String getPlaka() { return plaka; }
    public String getMarka() { return marka; }
    public String getModel() { return model; }
    public double getGunlukUcret() { return gunlukUcret; }
    public boolean isKiradaMi() { return kiradaMi; }
    public void setKiradaMi(boolean kiradaMi) { this.kiradaMi = kiradaMi; }

    @Override
    public String toString() {
        return "Plaka: " + plaka + 
               " | Marka: " + marka + 
               " | Model: " + model +
               " | Ücret: " + String.format("%.2f", gunlukUcret) + " TL/Gün" +
               " | Durum: " + (kiradaMi ? "KİRADA" : "MÜSAİT");
    }
}