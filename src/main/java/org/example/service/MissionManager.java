package org.example.service;

import org.example.exception.OutOfFuelException;
import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;

import java.util.ArrayList;
import java.util.List;

public class MissionManager {
    private Tank tank;
    private List<Objective> hedefler;

    public MissionManager(Tank tank) {
        this.tank = tank;
        this.hedefler = new ArrayList<>();
    }

    public void setHedefler(List<Objective> hedefler) {
        this.hedefler = hedefler;
    }

    public void rotaHesapla() {
        if (hedefler == null || hedefler.isEmpty()) return;

        Coordinate hqKonumu = new Coordinate(tank.getKonum().getX(), tank.getKonum().getY());

        if (hedefler.size() <= 10) {
            kusursuzRotayiBul(hqKonumu);
        } else {
            acgozluRotayiBul(hqKonumu);
        }

        if (tank.getYakitSeviyesi() > 0 && tank.getKonum().getX() == hqKonumu.getX() && tank.getKonum().getY() == hqKonumu.getY()) {
            tank.setDurum("GÖREV TAMAMLANDI");
        }
    }

    private void kusursuzRotayiBul(Coordinate hqKonumu) {
        List<Objective> enKisaRota = new ArrayList<>();
        double[] enKisaToplamMesafe = {Double.MAX_VALUE};
        Coordinate baslangic = tank.getKonum();

        permute(hedefler, 0, baslangic, hqKonumu, enKisaRota, enKisaToplamMesafe);

        List<Objective> basariliRota = new ArrayList<>();

        for (Objective hedef : enKisaRota) {
            double mesafe = tank.getKonum().hesaplaMesafe(hedef.getLocation());
            try {
                tank.mesafeGit(mesafe);
                tank.setKonum(hedef.getLocation());
                basariliRota.add(hedef);
            } catch (OutOfFuelException e) {
                System.err.println(e.getMessage());
                break;
            }
        }

        // Profesyonel Yaklaşım: Sadece yakıt ve mesafeyi hesapla, listeye sahte UI verisi ekleme!
        if (tank.getDurum().equals("BEKLEMEDE") || tank.getDurum().equals("HAREKET HALİNDE")) {
            double donusMesafesi = tank.getKonum().hesaplaMesafe(hqKonumu);
            try {
                tank.mesafeGit(donusMesafesi);
                tank.setKonum(hqKonumu);
            } catch (OutOfFuelException e) {
                System.err.println(e.getMessage());
            }
        }

        hedefler.clear();
        hedefler.addAll(basariliRota);
    }

    private void permute(List<Objective> arr, int k, Coordinate baslangic, Coordinate hqKonumu, List<Objective> enKisaRota, double[] minMesafe) {
        if (k == arr.size()) {
            double anlikToplamMesafe = 0.0;
            Coordinate aktif = baslangic;
            for (Objective obj : arr) {
                anlikToplamMesafe += aktif.hesaplaMesafe(obj.getLocation());
                aktif = obj.getLocation();
            }
            anlikToplamMesafe += aktif.hesaplaMesafe(hqKonumu);

            if (anlikToplamMesafe < minMesafe[0]) {
                minMesafe[0] = anlikToplamMesafe;
                enKisaRota.clear();
                enKisaRota.addAll(arr);
            }
        } else {
            for (int i = k; i < arr.size(); i++) {
                java.util.Collections.swap(arr, i, k);
                permute(arr, k + 1, baslangic, hqKonumu, enKisaRota, minMesafe);
                java.util.Collections.swap(arr, k, i);
            }
        }
    }

    private void acgozluRotayiBul(Coordinate hqKonumu) {
        Coordinate mevcutKonum = tank.getKonum();
        List<Objective> ziyaretEdilecekler = new ArrayList<>(hedefler);
        List<Objective> optimizeRota = new ArrayList<>();

        while (!ziyaretEdilecekler.isEmpty()) {
            Objective enYakin = null;
            double enKisa = Double.MAX_VALUE;

            for (Objective aday : ziyaretEdilecekler) {
                double mesafe = mevcutKonum.hesaplaMesafe(aday.getLocation());
                if (mesafe < enKisa) {
                    enKisa = mesafe;
                    enYakin = aday;
                }
            }

            try {
                tank.mesafeGit(enKisa);
                mevcutKonum = enYakin.getLocation();
                tank.setKonum(mevcutKonum);
                optimizeRota.add(enYakin);
                ziyaretEdilecekler.remove(enYakin);
            } catch (OutOfFuelException e) {
                System.err.println(e.getMessage());
                break;
            }
        }

        // Profesyonel Yaklaşım: Sadece yakıt ve mesafeyi hesapla, listeye sahte UI verisi ekleme!
        if (ziyaretEdilecekler.isEmpty()) {
            double donusMesafesi = tank.getKonum().hesaplaMesafe(hqKonumu);
            try {
                tank.mesafeGit(donusMesafesi);
                tank.setKonum(hqKonumu);
            } catch (OutOfFuelException e) {
                System.err.println(e.getMessage());
            }
        }

        hedefler.clear();
        hedefler.addAll(optimizeRota);
    }
}