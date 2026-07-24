package org.example.model;

    public enum TankState {
        IDLE,           // Bekliyor (Henüz görev başlamadı)
        MOVING,         // Hareket halinde
        MISSION_SUCCESS,// Görevi başarıyla tamamladı
        OUT_OF_FUEL,    // Yakıtı bitti (Yolda kaldı)
        FAILED          // Diğer başarısızlık durumları
    }

