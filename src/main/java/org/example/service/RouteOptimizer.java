package org.example.service;

import org.example.model.Coordinate;
import org.example.model.Objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteOptimizer {

    /**
     * Verilen hedef listesini, Hibrit TSP algoritması kullanarak en kısa rotaya göre sıralar.
     * N <= 10 ise Brute-Force, N > 10 ise Nearest Neighbor algoritmasını çalıştırır.
     *
     * @param hqLocation Tankın yola çıkacağı üs (HQ) koordinatı
     * @param targets    Ziyaret edilecek karmaşık hedef listesi
     * @return Ziyaret sırasına göre optimize edilmiş yeni hedef listesi
     */
    public List<Objective> optimizeRoute(Coordinate hqLocation, List<Objective> targets) {
        if (targets == null || targets.isEmpty()) {
            return new ArrayList<>();
        }

        System.out.println("Rota optimizasyonu başlatılıyor. Hedef sayısı: " + targets.size());

        if (targets.size() <= 10) {
            return calculateBruteForceRoute(hqLocation, targets);
        } else {
            return calculateNearestNeighborRoute(hqLocation, targets);
        }
    }

    // --- 1. KUSURSUZ PERMÜTASYON (N <= 10) ---
    private List<Objective> calculateBruteForceRoute(Coordinate start, List<Objective> targets) {
        double[] minDistance = {Double.MAX_VALUE};
        List<Objective> bestRoute = new ArrayList<>();

        List<Objective> currentRoute = new ArrayList<>(targets);
        permute(currentRoute, 0, start, minDistance, bestRoute);
        return bestRoute;
    }

    private void permute(List<Objective> arr, int k, Coordinate start, double[] minDistance, List<Objective> bestRoute) {
        if (k == arr.size()) {
            double currentTotalDist = calculateRouteDistance(start, arr);

            if (currentTotalDist < minDistance[0]) {
                minDistance[0] = currentTotalDist;
                bestRoute.clear();
                bestRoute.addAll(arr);
            }
            // TIE-BREAKER (Eşitlik Bozucu): "İlk Vuruş" kuralı
            else if (currentTotalDist == minDistance[0]) {
                double currentFirstLeg = start.hesaplaMesafe(arr.get(0).getLocation());
                double bestFirstLeg = start.hesaplaMesafe(bestRoute.get(0).getLocation());

                if (currentFirstLeg < bestFirstLeg) {
                    bestRoute.clear();
                    bestRoute.addAll(arr);
                }
            }
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, start, minDistance, bestRoute);
                Collections.swap(arr, k, i);
            }
        }
    }

    // --- 2. EN YAKIN KOMŞU (N > 10) ---
    private List<Objective> calculateNearestNeighborRoute(Coordinate start, List<Objective> targets) {
        List<Objective> unvisited = new ArrayList<>(targets);
        List<Objective> optimizedRoute = new ArrayList<>();
        Coordinate current = start;

        while (!unvisited.isEmpty()) {
            Objective nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (Objective target : unvisited) {
                double dist = current.hesaplaMesafe(target.getLocation());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = target;
                }
            }

            optimizedRoute.add(nearest);
            current = nearest.getLocation();
            unvisited.remove(nearest);
        }

        return optimizedRoute;
    }

    // Rotanın toplam maliyetini hesaplayan yardımcı metot
    private double calculateRouteDistance(Coordinate start, List<Objective> route) {
        double dist = 0;
        Coordinate current = start;
        for (Objective t : route) {
            dist += current.hesaplaMesafe(t.getLocation());
            current = t.getLocation();
        }
        dist += current.hesaplaMesafe(start);
        return dist;
    }
}
