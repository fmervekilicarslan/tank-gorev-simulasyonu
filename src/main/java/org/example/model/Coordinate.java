package org.example.model;

public class Coordinate {
    private double x;
    private double y;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    // İki koordinat arasındaki Öklid mesafesini hesaplar
    public double hesaplaMesafe(Coordinate diger) {
        if (diger == null) return 0.0;
        double dx = this.x - diger.getX();
        double dy = this.y - diger.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}