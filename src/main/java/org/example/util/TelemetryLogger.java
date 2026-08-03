package org.example.util;

public class TelemetryLogger {

    private String logDosyasi;

    public TelemetryLogger(String logDosyasi) {
        this.logDosyasi = logDosyasi;
    }

    public void logInfo(String mesaj){
        // Şimdilik sadece ekrana yazdıralım, ileride dosyaya da yazdırabiliriz
        System.out.println("[BİLGİ]" + mesaj);
    }

    public void logError(String mesaj){
        // Ekrana kırmızı renkte hata yazdırmak için 'out' yerine 'err' kullanıyoruz
        System.out.println("HATA" + mesaj);
    }
}
