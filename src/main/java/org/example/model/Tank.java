package org.example.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Tank sınıfı, simülasyondaki ana hareketli birimi temsil eder.
 * JavaFX arayüzü ile çift yönlü veri bağlaması (data binding) yapabilmek için
 * yakıt ve konum gibi anlık değişen değerler JavaFX Property'leri olarak tanımlanmıştır.
 */
public class Tank {

    // --- LOMBOK İLE YÖNETİLEN STANDART ALANLAR ---
    @Getter
    @Setter
    private TankState currentState; // Enum: Tankın anlık durumu

    @Getter
    @Setter
    private double fuelConsumptionRate; // 100 pikselde tüketilen yakıt miktarı

    // --- JAVAFX PROPERTY İLE YÖNETİLEN DİNAMİK ALANLAR ---
    // Not: Gamze Hanım'ın uyarısı doğrultusunda, arayüzde anlık güncellenecek
    // veriler için Lombok @Getter/@Setter yerine manuel Property metodları yazılmıştır.

    private final DoubleProperty xCoordinate;
    private final DoubleProperty yCoordinate;
    private final DoubleProperty fuelLevel;

    /**
     * Tank nesnesi oluşturulurken başlangıç değerlerini atayan Constructor.
     */
    public Tank(double startX, double startY, double initialFuel) {
        this.xCoordinate = new SimpleDoubleProperty(startX);
        this.yCoordinate = new SimpleDoubleProperty(startY);
        this.fuelLevel = new SimpleDoubleProperty(initialFuel);
        this.currentState = TankState.IDLE; // Varsayılan durum: Bekliyor
        this.fuelConsumptionRate = 5.0; // Varsayılan: 100px'de 5 birim yakıt (SRS REQ-UI-1.5'e göre)
    }

    // --- JAVAFX PROPERTY METODLARI (X KOORDİNATI) ---
    public double getXCoordinate() { return xCoordinate.get(); }
    public void setXCoordinate(double value) { this.xCoordinate.set(value); }
    public DoubleProperty xCoordinateProperty() { return xCoordinate; }

    // --- JAVAFX PROPERTY METODLARI (Y KOORDİNATI) ---
    public double getYCoordinate() { return yCoordinate.get(); }
    public void setYCoordinate(double value) { this.yCoordinate.set(value); }
    public DoubleProperty yCoordinateProperty() { return yCoordinate; }

    // --- JAVAFX PROPERTY METODLARI (YAKIT SEVİYESİ) ---
    public double getFuelLevel() { return fuelLevel.get(); }
    public void setFuelLevel(double value) { this.fuelLevel.set(value); }
    public DoubleProperty fuelLevelProperty() { return fuelLevel; }
}