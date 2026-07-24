package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.example.model.Base;
import org.example.model.Objective;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;

public class SimulationController {

    // --- FXML BİLEŞENLERİ ---
    @FXML private Button btnBaseMode;
    @FXML private Button btnObjMode;
    @FXML private TextField txtFuelCapacity;
    @FXML private TextField txtFuelRate;
    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnResume;
    @FXML private Button btnClear;
    @FXML private Pane mapPane;
    @FXML private Label lblFuel;
    @FXML private Label lblDistance;
    @FXML private Label lblStatus;
    @FXML private Label lblStatusBar;
    @FXML private Label lblCoordinates;

    // --- SİMÜLASYON DURUM (STATE) DEĞİŞKENLERİ ---
    private enum Mode { NONE, BASE, OBJECTIVE }
    private Mode currentMode = Mode.NONE;

    private Base baseNode = null;
    private List<Objective> objectiveNodes = new ArrayList<>();
    private int objectiveCounter = 1;

    private String lastValidCapacity;
    private String lastValidRate;

    @FXML
    public void initialize() {
        // Mod Butonları
        btnBaseMode.setOnAction(e -> setMode(Mode.BASE));
        btnObjMode.setOnAction(e -> setMode(Mode.OBJECTIVE));
        lblStatusBar.setText("Durum Çubuğu: Sistem Hazır. Lütfen mod seçiniz...");
        javafx.application.Platform.runLater(this::drawGrid);
        // Harita Tıklama
        mapPane.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            handleMapClick(x, y);
        });

        // Aksiyon Butonları (Tıklandığında renk/durum geri bildirimi ekledik)
        btnClear.setOnAction(e -> {
            clearMap();
            lblStatusBar.setText("Durum Çubuğu: Harita ve veriler temizlendi.");
        });

        btnStart.setOnAction(e -> {
            validateAndStart();
        });

        btnPause.setOnAction(e -> {
            lblStatus.setText("Operasyon Durumu: [ DURAKLATILDI ]");
            lblStatusBar.setText("Durum Çubuğu: Simülasyon duraklatıldı.");
        });

        btnResume.setOnAction(e -> {
            lblStatus.setText("Operasyon Durumu: [ ÇALIŞIYOR ]");
            lblStatusBar.setText("Durum Çubuğu: Simülasyon devam ediyor.");
        });

        lastValidCapacity = txtFuelCapacity.getText();
        lastValidRate = txtFuelRate.getText();

        lblStatusBar.setText("Durum Çubuğu: Sistem Hazır. Lütfen mod seçiniz...");

        // --- TOOLTIP (İPUÇLARI) ---
        Tooltip capacityTip = new Tooltip("Tankın operasyona başlayacağı toplam yakıt kapasitesini (Litre) belirler.");
        Tooltip rateTip = new Tooltip("Tankın haritada her 100 birim (px) ilerlediğinde harcayacağı yakıt miktarı.");

        txtFuelCapacity.setTooltip(capacityTip);
        txtFuelRate.setTooltip(rateTip);

        // --- CANLI KOORDİNAT TAKİBİ ---
        mapPane.setOnMouseMoved(event -> {
            // Fare harita üzerinde gezdikçe koordinatları canlı yazdır
            lblCoordinates.setText(String.format("RADAR: [ X: %.1f | Y: %.1f ]", event.getX(), event.getY()));
        });

        mapPane.setOnMouseExited(event -> {
            // Fare haritadan çıkınca bekleme moduna dön
            lblCoordinates.setText("RADAR: [ BEKLEMEDE ]");
        });
    }

    // Seçilen moda göre buton stillerini ve metinlerini günceller (Seçildi/Seçilmedi net anlaşılır)
    private void setMode(Mode mode) {
        this.currentMode = mode;

        // Varsayılan (Seçilmemiş) Haller
        btnBaseMode.setStyle("-fx-background-color: #384d38; -fx-text-fill: white; -fx-border-color: #4caf50;");
        btnBaseMode.setText("[ ] Üs Konumu Seç");

        btnObjMode.setStyle("-fx-background-color: #384d38; -fx-text-fill: white; -fx-border-color: #4caf50;");
        btnObjMode.setText("[ ] Hedef Noktası Ekle");

        // Aktif Modu Parlat ve Onay İşareti Koy
        if (mode == Mode.BASE) {
            btnBaseMode.setStyle("-fx-background-color: #8bc34a; -fx-text-fill: black; -fx-border-color: #ffffff; -fx-font-weight: bold;");
            btnBaseMode.setText("[✔] Üs Konumu Seç (Aktif)");
            lblStatusBar.setText("Durum Çubuğu: Üs seçme modu aktif. Haritaya tıklayın.");
        } else if (mode == Mode.OBJECTIVE) {
            btnObjMode.setStyle("-fx-background-color: #8bc34a; -fx-text-fill: black; -fx-border-color: #ffffff; -fx-font-weight: bold;");
            btnObjMode.setText("[✔] Hedef Ekleme (Aktif)");
            lblStatusBar.setText("Durum Çubuğu: Hedef ekleme modu aktif. Haritaya tıklayın.");
        } else {
            lblStatusBar.setText("Durum Çubuğu: Sistem Hazır.");
        }
    }

    private void handleMapClick(double x, double y) {
        if (currentMode == Mode.BASE) {
            placeBase(x, y);
        } else if (currentMode == Mode.OBJECTIVE) {
            placeObjective(x, y);
        } else {
            lblStatusBar.setText("Durum Çubuğu: Önce sol menüden bir mod seçmelisiniz!");
        }
    }

    private void placeBase(double x, double y) {
        mapPane.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("baseIcon"));
        this.baseNode = new Base(x, y);

        Polygon house = new Polygon(
                0.0, -15.0,
                -15.0, 0.0,
                -10.0, 0.0,
                -10.0, 15.0,
                10.0, 15.0,
                10.0, 0.0,
                15.0, 0.0
        );
        house.setFill(Color.DODGERBLUE);
        house.setLayoutX(x);
        house.setLayoutY(y);
        house.setId("baseIcon");

        Label lbl = new Label("[HQ]");
        lbl.setTextFill(Color.DODGERBLUE);
        lbl.setLayoutX(x - 12);
        lbl.setLayoutY(y + 15);
        lbl.setStyle("-fx-font-weight: bold;");
        lbl.setId("baseIcon");

        mapPane.getChildren().addAll(house, lbl);
        lblStatusBar.setText("Durum Çubuğu: Üs konumu haritaya yerleştirildi.");
    }

    private void placeObjective(double x, double y) {
        String objName = "Hedef " + objectiveCounter;
        Objective obj = new Objective(objName, x, y, false);
        objectiveNodes.add(obj);

        javafx.scene.text.Text cross = new javafx.scene.text.Text("(X)");
        cross.setFill(Color.CRIMSON);
        cross.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        cross.setLayoutX(x - 10);
        cross.setLayoutY(y);

        Label lbl = new Label(objName);
        lbl.setTextFill(Color.WHITE);
        lbl.setLayoutX(x - 20);
        lbl.setLayoutY(y + 5);

        mapPane.getChildren().addAll(cross, lbl);
        lblStatusBar.setText("Durum Çubuğu: " + objName + " eklendi.");
        objectiveCounter++;
    }

    private void clearMap() {
        mapPane.getChildren().removeIf(node -> node.getId() == null || !node.getId().equals("gridLine"));
        baseNode = null;
        objectiveNodes.clear();
        objectiveCounter = 1;
        setMode(Mode.NONE);
        lblDistance.setText("Kat Edilen Yol: [ 0.0 px ]");
        lblStatus.setText("Operasyon Durumu: [ BEKLEMEDE ]");
    }

    private void validateAndStart() {
        if (baseNode == null) {
            showAlert(Alert.AlertType.WARNING, "Eksik Üs", "Lütfen önce tankın çıkış noktası olacak Askeri Üssü (HQ) haritaya yerleştirin!");
            lblStatusBar.setText("Durum Çubuğu Hata: Üs noktası eksik!");
            return;
        }

        if (objectiveNodes.size() < 2) {
            showAlert(Alert.AlertType.WARNING, "Yetersiz Hedef", "Görevi başlatabilmek için haritaya en az iki hedef noktası eklemelisiniz!");
            lblStatusBar.setText("Durum Çubuğu Hata: Yetersiz hedef sayısı!");
            return;
        }

        try {
            double capacity = Double.parseDouble(txtFuelCapacity.getText());
            double rate = Double.parseDouble(txtFuelRate.getText());

            if (capacity <= 0 || rate <= 0) {
                throw new NumberFormatException();
            }

            lastValidCapacity = txtFuelCapacity.getText();
            lastValidRate = txtFuelRate.getText();

            lblStatus.setText("Operasyon Durumu: [ HESAPLANIYOR ]");
            lblStatusBar.setText("Durum Çubuğu: Rota hesaplanıyor ve görev başlatılıyor...");

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz Parametre", "Lütfen geçerli pozitif sayılar giriniz! Değerler son geçerli haline döndürüldü.");
            txtFuelCapacity.setText(lastValidCapacity);
            txtFuelRate.setText(lastValidRate);
            lblStatusBar.setText("Durum Çubuğu Hata: Geçersiz yakıt parametresi!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Haritanın arka planına askeri radar ızgarasını Java koduyla çizer
    private void drawGrid() {
        double width = mapPane.getWidth();
        double height = mapPane.getHeight();
        int gridSize = 25; // Izgara karelerinin boyutu

        for (int i = 0; i < width; i += gridSize) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(i, 0, i, height);
            line.setStroke(Color.web("#223322")); // Çok soluk yeşil renk
            line.setStrokeWidth(1);
            line.setId("gridLine"); // Temizlerken silinmemesi için ID veriyoruz
            mapPane.getChildren().add(0, line); // En arka plana ekle
        }

        for (int i = 0; i < height; i += gridSize) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, i, width, i);
            line.setStroke(Color.web("#223322"));
            line.setStrokeWidth(1);
            line.setId("gridLine");
            mapPane.getChildren().add(0, line);
        }
    }
}