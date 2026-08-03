package org.example.model;

import org.example.exception.OutOfFuelException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TankTest {

    @Test
    void testNormalHareketVeYakitTuketimi() {
        // Hazırlık: 50L kapasiteli, 100px'te 5L yakan bir tank
        Tank tank = new Tank(new Coordinate(0, 0), 50.0, 5.0, 50.0);

        try {
            // Eylem: 200 piksel git
            tank.mesafeGit(200);

            // Doğrulama: 200px'te 10L yakmalı, geriye 40L kalmalı.
            assertEquals(40.0, tank.getYakitSeviyesi(), 0.001);
            assertEquals(200.0, tank.getKatEdilenMesafe(), 0.001);
            assertEquals("BEKLEMEDE", tank.getDurum()); // Yakıt bitmediği için hata yok
        } catch (OutOfFuelException e) {
            fail("Yakıt yeterli olmasına rağmen hata fırlatıldı!");
        }
    }

    @Test
    void testOutOfFuelExceptionFirlatmaSınırDurumu() {
        // Hazırlık: Sadece 10L yakıtı olan, 100px'te 10L yakan bir tank
        Tank tank = new Tank(new Coordinate(0, 0), 10.0, 10.0, 10.0);

        // Eylem & Doğrulama: Tank 150 piksel gitmeye zorlanıyor
        OutOfFuelException firlatilanHata = assertThrows(OutOfFuelException.class, () -> {
            tank.mesafeGit(150);
        });

        // Hata mesajı doğru mu?
        assertEquals("Kritik Hata: Tankın yakıtı tükendi! Hedefe tam olarak ulaşılamadı.", firlatilanHata.getMessage());

        // Tank depodaki son damlaya kadar gidip (100px) orada durmuş olmalı!
        assertEquals(0.0, tank.getYakitSeviyesi(), 0.001);
        assertEquals(100.0, tank.getKatEdilenMesafe(), 0.001);
        assertEquals("YAKIT TÜKENDİ", tank.getDurum());
    }
}