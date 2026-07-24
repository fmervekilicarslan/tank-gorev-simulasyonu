package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simülasyondaki Askeri Üssü (Başlangıç ve Bitiş noktası) temsil eder.
 */
@Data
@AllArgsConstructor
public class Base {
    private double x;
    private double y;
}