package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TSPSimulation {

    // Simülasyon için bağımsız ve basitleştirilmiş Hedef (Koordinat) Sınıfı
    static class Target {
        int id;
        double x, y;

        public Target(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        // İki nokta arası Öklid mesafesi hesaplama
        public double distanceTo(Target other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Tank Görev Simülatörü - TSP Hibrit Algoritma Testi ===\n");

        // Senaryo 1: 8 Hedef (Kusursuz Permütasyon / Brute-Force çalışmalı)
        runSimulation(8);

        System.out.println("--------------------------------------------------\n");

        // Senaryo 2: 15 Hedef (En Yakın Komşu / Nearest Neighbor çalışmalı)
        runSimulation(15);

        System.out.println("--------------------------------------------------\n");

        // Senaryo 3: 50 Hedef (Performansın çökmediğini kanıtlamak için)
        runSimulation(50);
    }

    // Ana Yönlendirici Metot (Strateji Deseni Mantığı)
    public static void runSimulation(int targetCount) {
        System.out.println("Hedef Sayısı (N) = " + targetCount + " için simülasyon başlatılıyor...");
        List<Target> targets = generateRandomTargets(targetCount);
        Target startPoint = new Target(0, 0, 0); // Tankın üssü başlangıç noktası (0,0)

        long startTime = System.nanoTime(); // Süre ölçümünü başlat

        if (targetCount <= 10) {
            System.out.println("Algoritma Seçimi: Kusursuz Permütasyon (Brute-Force) - O(N!)");
            runBruteForce(startPoint, targets);
        } else {
            System.out.println("Algoritma Seçimi: En Yakın Komşu (Nearest Neighbor) - O(N^2)");
            runNearestNeighbor(startPoint, targets);
        }

        long endTime = System.nanoTime(); // Süre ölçümünü bitir
        double durationMs = (endTime - startTime) / 1_000_000.0;

        System.out.printf("İşlem Süresi: %.3f milisaniye\n", durationMs);
    }

    // --- 1. ALGORİTMA: EN YAKIN KOMŞU (O(N^2)) ---
    private static void runNearestNeighbor(Target start, List<Target> targets) {
        List<Target> unvisited = new ArrayList<>(targets);
        Target current = start;
        double totalDistance = 0;

        System.out.print("Rota: Üs -> ");
        while (!unvisited.isEmpty()) {
            Target nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (Target target : unvisited) {
                double dist = current.distanceTo(target);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = target;
                }
            }

            totalDistance += minDistance;
            current = nearest;
            unvisited.remove(nearest);
            System.out.print("H" + nearest.id + " -> ");
        }
        // Tüm hedefler bitince üsse dönüş
        totalDistance += current.distanceTo(start);
        System.out.println("Üs");
        System.out.printf("Toplam Mesafe (Yaklaşık/Greedy): %.2f birim\n", totalDistance);
    }

    // --- 2. ALGORİTMA: KUSURSUZ PERMÜTASYON (O(N!)) ---
    private static void runBruteForce(Target start, List<Target> targets) {
        double[] minDistance = {Double.MAX_VALUE};
        List<Target> bestRoute = new ArrayList<>();

        // Rekürsif permütasyon fonksiyonunu tetikle
        permute(targets, 0, start, minDistance, bestRoute);

        System.out.print("Rota: Üs -> ");
        for (Target t : bestRoute) {
            System.out.print("H" + t.id + " -> ");
        }
        System.out.println("Üs");
        System.out.printf("Toplam Mesafe (Kesin/Optimum): %.2f birim\n", minDistance[0]);
    }

    // Tüm ihtimalleri deneyen rekürsif fonksiyon
    private static void permute(List<Target> arr, int k, Target start, double[] minDistance, List<Target> bestRoute) {
        if (k == arr.size()) {
            double currentDist = calculateRouteDistance(start, arr);
            if (currentDist < minDistance[0]) {
                minDistance[0] = currentDist;
                bestRoute.clear();
                bestRoute.addAll(arr);
            }
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, start, minDistance, bestRoute);
                Collections.swap(arr, k, i);
            }
        }
    }

    // Verilen rotanın toplam mesafesini (üsse dönüş dahil) hesaplar
    private static double calculateRouteDistance(Target start, List<Target> route) {
        double dist = 0;
        Target current = start;
        for (Target t : route) {
            dist += current.distanceTo(t);
            current = t;
        }
        dist += current.distanceTo(start);
        return dist;
    }

    // Test verisi üretici (Tutarlı sonuçlar için sabit seed kullanılmıştır)
    private static List<Target> generateRandomTargets(int count) {
        List<Target> targets = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 1; i <= count; i++) {
            targets.add(new Target(i, rand.nextInt(100), rand.nextInt(100)));
        }
        return targets;
    }
}