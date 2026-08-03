package org.example;

import org.example.model.Base;
import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;
import org.example.service.MissionManager;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){

        //1.ÜSSÜ OLUŞTUR
        //Üssümüz haritanın tam merkezinde(0,0) olsun
        Coordinate usKonumu =new Coordinate(0,0);
        Base askeriUs =new Base(usKonumu);

        //2.TANKI OLUŞTUR
        //Tank üste(0,0) başlasın.
        //50 litre yakıtı olsun,birimde 2 litre yaksın,kapasitesi 100 olsun.
        Tank paletliTank= new Tank(new Coordinate(0,0),50.0,2.0,100.0);

        //3. HEDEFLERİ OLUŞTUR
        List<Objective> hedefler=new ArrayList<>();
        //0,0'dan 3,4'e mesafe=5 birimdir.
        // 0,0'dan 3,4'e mesafe = 5 birimdir.
        hedefler.add(new Objective(new Coordinate(3, 4), "Düşman Cephaneliği"));
        // 3,4'ten 9,12'ye mesafe = 10 birimdir.
        hedefler.add(new Objective(new Coordinate(9, 12), "Radyo Kulesi"));

        // 4. GÖREV YÖNETİCİSİNİ (MISSION MANAGER) BAŞLAT
        MissionManager yonetici = new MissionManager(paletliTank);
        yonetici.setHedefler(hedefler); // Hedef listesini yöneticimize veriyoruz

        // 5. MOTORLARI ÇALIŞTIR!
        System.out.println("--- SİMÜLASYON BAŞLIYOR ---");
        yonetici.rotaHesapla();

        // 6. SON DURUM RAPORU
        System.out.println("\n--- GÖREV SONU RAPORU ---");
        System.out.println("Tankın Kalan Yakıtı: " + paletliTank.getYakitSeviyesi() + " Litre");
        System.out.println("Tankın Kat Ettiği Toplam Mesafe: " + paletliTank.getKatEdilenMesafe() + " Birim");
        System.out.println("Tankın Son Durumu: " + paletliTank.getDurum());
    }
}
