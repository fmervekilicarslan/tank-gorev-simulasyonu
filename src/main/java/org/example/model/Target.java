package org.example.model;

public class Target {
    private int x;
    private int y;
    private boolean ulasildiMi;

    public Target(int x, int y) {
        this.x = x;
        this.y = y;
        this.ulasildiMi = false; // Başlangıçta hedefe henüz ulaşılmadı
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isUlasildiMi() {
        return ulasildiMi;
    }

    public void setUlasildiMi(boolean ulasildiMi) {
        this.ulasildiMi = ulasildiMi;
    }
}
