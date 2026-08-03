package org.example.model;

    public enum TankState {
        BEKLEMEDE,    // Bekliyor (Henüz görev başlamadı)
        HAREKET_HALINDE,    // Hareket halinde
        GOREV_TAMAMLANDI,   // Görevi başarıyla tamamladı
        YAKIT_BITTI,    // Yakıtı bitti (Yolda kaldı)
        GOREV_BASARISIZ,   // Diğer başarısızlık durumları
    }

