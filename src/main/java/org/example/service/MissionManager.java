package org.example.service;

import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;

import java.util.ArrayList;
import java.util.List;

public class MissionManager {
    private Tank aktifTank;
    private List<Objective> hedefler;

    public MissionManager(Tank tank) {
        this.aktifTank = tank;
        this.hedefler = new ArrayList<>();
    }

    public void setHedefler(List<Objective> yeniHedefler) {
        this.hedefler = yeniHedefler;
    }

    /**
     * Hibrit TSP Algoritmasını kullanarak en kısa rotayı hesaplar,
     * tankın yakıtını ve mesafesini günceller,
     * ve sıralanmış hedef listesini UI'a geri döndürür.
     */
    public List<Objective> rotaHesapla(Coordinate hqLocation) {
        if (hedefler == null || hedefler.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Gümrük Kapımız Olan RouteOptimizer'ı Çağır!
        RouteOptimizer optimizer = new RouteOptimizer();
        List<Objective> siraliRota = optimizer.optimizeRoute(hqLocation, hedefler);

        // 2. Tankın toplam maliyetini hesapla
        double toplamMesafe = 0;
        Coordinate currentPos = hqLocation;

        for (Objective obj : siraliRota) {
            toplamMesafe += currentPos.hesaplaMesafe(obj.getLocation());
            currentPos = obj.getLocation();
        }
        // Üsse Dönüş Mesafesi
        toplamMesafe += currentPos.hesaplaMesafe(hqLocation);

        // 3. Hesaplanan mesafeyi tanka bildir (Senin sınıftaki Get ve Set'i kullanarak)
        aktifTank.setKatEdilenMesafe(aktifTank.getKatEdilenMesafe() + toplamMesafe);

        // Tankın durumunu görev tamamlandı olarak işaretle
        aktifTank.setDurum("GÖREV TAMAMLANDI");

        // 4. Kusursuz sıraya dizilmiş bu rotayı arayüze gönder
        return siraliRota;
    }
}