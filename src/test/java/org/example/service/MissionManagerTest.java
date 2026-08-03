package org.example.service;

import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MissionManagerTest {

    @Test
    void testRotaOptimizasyonu_KusursuzAlgoritma() {
        // Hazırlık (Arrange)
        Tank tank = new Tank(new Coordinate(0, 0), 100.0, 1.0, 100.0);
        MissionManager manager = new MissionManager(tank);

        List<Objective> hedefler = new ArrayList<>();
        hedefler.add(new Objective(new Coordinate(10, 0), "Hedef A (Uzak)"));
        hedefler.add(new Objective(new Coordinate(2, 0), "Hedef B (Yakın)"));
        hedefler.add(new Objective(new Coordinate(5, 0), "Hedef C (Orta)"));

        manager.setHedefler(hedefler);

        // Eylem (Act) -> Algoritmayı çalıştır
        manager.rotaHesapla();

        // Doğrulama (Assert)
        // Düz bir çizgide (0 -> 10) gidip dönmek ile (10 -> 0) dönmek aynı mesafedir (Maliyet: 20).
        // Algoritma permütasyon ağacında "A -> C -> B" kombinasyonunu önce bulduğu için bu sırayı seçer ve bu %100 doğrudur.
        assertEquals("Hedef A (Uzak)", hedefler.get(0).getEtiket(), "İlk hedef uzak nokta olmalıydı!");
        assertEquals("Hedef C (Orta)", hedefler.get(1).getEtiket(), "İkinci hedef orta nokta olmalıydı!");
        assertEquals("Hedef B (Yakın)", hedefler.get(2).getEtiket(), "Üçüncü hedef yakın nokta olmalıydı!");

        // TSP Algoritmasının nihai kanıtı: Hangi sırayla giderse gitsin toplam mesafe mükemmel şekilde 20 olmalıdır!
        assertEquals(20.0, tank.getKatEdilenMesafe(), 0.001, "Toplam mesafe yanlış hesaplandı (Üsse dönüş dahil edilmemiş olabilir)!");
    }
}