package org.example.model;

import org.example.exception.OutOfFuelException; // Kendi yazdığımız hata sınıfını import ettik

public class Tank {
    private Coordinate konum;
    private double yakitSeviyesi;
    private double tuketimOrani;
    private double kapasite;
    private double katEdilenMesafe;
    private String durum;

    public Tank(Coordinate konum, double yakitSeviyesi, double tuketimOrani, double kapasite) {
        this.konum = konum;
        this.yakitSeviyesi = yakitSeviyesi;
        this.tuketimOrani = tuketimOrani;
        this.kapasite = kapasite;
        this.katEdilenMesafe = 0.0;
        this.durum = "BEKLEMEDE";
    }

    // throws OutOfFuelException: "Bu metot çalışırken yakıt bitme tehlikesi var, haberiniz olsun!" demek.
    public void mesafeGit(double gidilenMesafe) throws OutOfFuelException {
        double harcananYakit = (gidilenMesafe / 100.0) * tuketimOrani;

        // EĞER YAKIT YETMİYORSA:
        if (this.yakitSeviyesi < harcananYakit) {
            // Tank depodaki son damlaya kadar (gidebileceği yere kadar) gider
            double gidilebilenMaksimumYol = (this.yakitSeviyesi / tuketimOrani) * 100.0;
            this.katEdilenMesafe += gidilebilenMaksimumYol;
            this.yakitSeviyesi = 0;
            this.durum = "YAKIT TÜKENDİ";

            // Ve kırmızı alarmı (Exception) çalarak işlemi durdurur!
            throw new OutOfFuelException("Kritik Hata: Tankın yakıtı tükendi! Hedefe tam olarak ulaşılamadı.");
        }

        // YAKIT YETİYORSA NORMAL ÇALIŞMAYA DEVAM:
        this.katEdilenMesafe += gidilenMesafe;
        this.yakitSeviyesi -= harcananYakit;
    }

    public double getTahminiMenzil() {
        if (tuketimOrani <= 0) return 0.0;
        return (yakitSeviyesi / tuketimOrani) * 100.0;
    }

    public Coordinate getKonum() { return konum; }
    public void setKonum(Coordinate konum) { this.konum = konum; }

    public double getYakitSeviyesi() { return yakitSeviyesi; }
    public void setYakitSeviyesi(double yakitSeviyesi) { this.yakitSeviyesi = yakitSeviyesi; }

    public double getKatEdilenMesafe() { return katEdilenMesafe; }
    public void setKatEdilenMesafe(double katEdilenMesafe) { this.katEdilenMesafe = katEdilenMesafe; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }
}