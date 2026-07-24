package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simülasyonda tankın gitmesi gereken hedef noktalarını temsil eder.
 */
@Data
@AllArgsConstructor
public class Objective {
    private String name;     // Hedefin adı (Örn: "Hedef 1", "Hedef 2") -> SRS REQ-UI-1.3
    private double x;        // X koordinatı
    private double y;        // Y koordinatı
    private boolean reached; // Tank bu hedefe ulaştı mı? (Görseli değiştirmek için) -> SRS REQ-SIM-2.3
}
