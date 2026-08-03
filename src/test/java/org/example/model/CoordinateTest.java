package org.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

    class CoordinateTest {

        @Test
        void testHesaplaMesafe() {
            // Hazırlık (Arrange)
            Coordinate merkez = new Coordinate(0, 0);
            Coordinate hedef = new Coordinate(3, 4);

            // Eylem (Act)
            double sonuc = merkez.hesaplaMesafe(hedef);

            // Doğrulama (Assert) - 3,4,5 üçgenine göre mesafe tam olarak 5 olmalıdır.
            // Sondaki 0.001 değeri, double sayılardaki milimetrik sapma toleransıdır.
            assertEquals(5.0, sonuc, 0.001, "Mesafe hesaplama algoritması hatalı!");
        }
    }

