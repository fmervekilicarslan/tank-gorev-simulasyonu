package org.example.service;

import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MissionManagerTest {

    private Tank tank;
    private MissionManager manager;

    @BeforeEach
    void setUp() {
        // Her testten önce temiz bir başlangıç
        tank = new Tank(new Coordinate(0, 0), 1000.0, 1.0, 0.0);
        manager = new MissionManager(tank);
    }

    @Test
    void testBruteForceAlgoritmasi_KucukVeriSeti() {
        // N <= 10 (Örn: 5 hedef - Brute Force çalışmalı)
        List<Objective> hedefler = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            hedefler.add(new Objective(new Coordinate(i * 10, 0), "Hedef " + i));
        }
        manager.setHedefler(hedefler);

        // Act
        manager.rotaHesapla(new Coordinate(0, 0));

        // Assert: 5 hedef için 0->10->20->30->40->50 ve geri dönüş (50) = 100 mesafe
        assertEquals(100.0, tank.getKatEdilenMesafe(), 0.01, "Brute Force optimum rotayı hesaplayamadı!");
    }

    @Test
    void testNearestNeighborAlgoritmasi_BuyukVeriSeti() {
        // N > 10 (Örn: 12 hedef - Nearest Neighbor çalışmalı)
        List<Objective> hedefler = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            // Basit bir hat üzerinde 12 hedef
            hedefler.add(new Objective(new Coordinate(i * 10, 0), "Hedef " + i));
        }
        manager.setHedefler(hedefler);

        // Act
        manager.rotaHesapla(new Coordinate(0, 0));

        // Assert:
        // Nearest Neighbor da bu basit senaryoda optimum sonucu (0->120->0 = 240) bulacaktır.
        // Önemli olan sistemin 12 hedefi kabul etmesi ve hata vermemesidir.
        assertEquals(240.0, tank.getKatEdilenMesafe(), 0.01, "Nearest Neighbor büyük veri setinde hatalı hesaplama yaptı!");
        assertEquals("GÖREV TAMAMLANDI", tank.getDurum(), "Görev durumu güncellenmedi!");
    }
}