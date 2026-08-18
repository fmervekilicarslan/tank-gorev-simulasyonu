package org.example.controller;

import javafx.animation.PathTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.example.model.Base;
import org.example.model.Coordinate;
import org.example.model.Objective;
import org.example.model.Tank;
import org.example.service.MissionManager;

import java.util.ArrayList;
import java.util.List;

public class SimulationController {

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
    @FXML private Label lblMenzil;

    // Tankın son geçerli parametrelerini hafızada tutan değişkenler
    private double sonGecerliKapasite = 100.0;
    private double sonGecerliTuketim = 5.0;

    // Objective nesnelerini görsel VBox'ları ile eşleştiren harita
    private java.util.Map<Objective, javafx.scene.layout.VBox> objectiveVisuals = new java.util.HashMap<>();

    private enum Mode { NONE, BASE, OBJECTIVE }
    private Mode currentMode = Mode.NONE;

    private Base baseNode = null;
    private List<Objective> objectiveNodes = new ArrayList<>();
    private int objectiveCounter = 1;

    private Tank aktifTank = null;
    private List<Objective> kesinlesmisRota = new ArrayList<>();
    private Polygon animTankGorseli = null;
    private Coordinate sonAnimasyonKonumu = null;
    private PathTransition activeTransition;

    @FXML
    public void initialize() {
        btnBaseMode.setOnAction(e -> setMode(Mode.BASE));
        btnObjMode.setOnAction(e -> setMode(Mode.OBJECTIVE));
        lblStatusBar.setText("Durum Çubuğu: Sistem Hazır. Lütfen mod seçiniz...");

        mapPane.widthProperty().addListener((obs, oldVal, newVal) -> drawGrid());
        mapPane.heightProperty().addListener((obs, oldVal, newVal) -> drawGrid());
        javafx.application.Platform.runLater(this::drawGrid);

        mapPane.setOnMouseClicked(event -> {
            // Sadece Sol Tık yapıldıysa haritaya işlem yap
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                handleMapClick(event.getX(), event.getY());
            }
        });

        // HARİTA ÜZERİNDE FARE HAREKETİNİ DİNLEYEN RADAR SİSTEMİ
        mapPane.setOnMouseMoved(event -> {
            if (activeTransition == null || activeTransition.getStatus() != javafx.animation.Animation.Status.RUNNING) {
                lblCoordinates.setText(String.format("RADAR: [ X: %.1f | Y: %.1f ]", event.getX(), event.getY()));
            }
        });

        // Fare haritadan çıkınca koordinatları gizle, sadece "RADAR" yazsın
        mapPane.setOnMouseExited(event -> {
            if (activeTransition == null || activeTransition.getStatus() != javafx.animation.Animation.Status.RUNNING) {
                lblCoordinates.setText("RADAR");
            }
        });

        btnClear.setOnAction(e -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Haritayı Temizleme Onayı");
            alert.setHeaderText(null);
            alert.setContentText("Haritadaki tüm hedefleri, üssü ve rotayı silmek istediğinize emin misiniz?");

            javafx.scene.control.ButtonType btnEvet = new javafx.scene.control.ButtonType("Evet");
            javafx.scene.control.ButtonType btnHayir = new javafx.scene.control.ButtonType("Hayır", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnEvet, btnHayir);

            alert.showAndWait().ifPresent(type -> {
                if (type == btnEvet) {
                    clearMap(); // Sadece Evet'e basılırsa temizle
                }
            });
        });

        btnStart.setOnAction(e -> validateAndStart());

        // --- DURDUR (PAUSE) BUTONU BAĞLANTISI ---
        btnPause.setOnAction(e -> {
            if (activeTransition != null) {
                activeTransition.pause(); // Tank hareketini anlık olarak dondurur
                lblStatus.setText("Operasyon Durumu: [ DURAKLATILDI ]");
                lblStatus.setTextFill(Color.YELLOW);
            }
        });

        // --- DEVAM ET (RESUME) BUTONU BAĞLANTISI ---
        btnResume.setOnAction(e -> {
            if (activeTransition != null) {
                activeTransition.play(); // Kaldığı yerden devam ettirir
                lblStatus.setText("Operasyon Durumu: [ SEYİR HALİNDE ]");
                lblStatus.setTextFill(Color.web("#7cb342"));
            }
        });

        btnBaseMode.setOnAction(e -> {
            if (currentMode == Mode.BASE) setMode(Mode.NONE);
            else setMode(Mode.BASE);
        });

        btnObjMode.setOnAction(e -> {
            if (currentMode == Mode.OBJECTIVE) setMode(Mode.NONE);
            else setMode(Mode.OBJECTIVE);
        });

        txtFuelCapacity.setTooltip(new Tooltip("Tankın kapasitesi."));
        txtFuelRate.setTooltip(new Tooltip("100 px başına harcanan yakıt."));

        // Kullanıcı Yakıt Kapasitesi kutusuna veri girip, başka bir yere tıkladığı (Focus kaybettiği) an kontrol et:
        txtFuelCapacity.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                anlikYakitKontrolu();
            }
        });

        // Kullanıcı Tüketim Oranı kutusuna veri girip, başka bir yere tıkladığı an kontrol et:
        txtFuelRate.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                anlikYakitKontrolu();
            }
        });

        txtFuelCapacity.textProperty().addListener((obs, oldVal, newVal) -> updateTelemetryAndRange());
        txtFuelRate.textProperty().addListener((obs, oldVal, newVal) -> updateTelemetryAndRange());

        updateTelemetryAndRange();
    }

    private void updateTelemetryAndRange() {
        try {
            double capacity = Double.parseDouble(txtFuelCapacity.getText());
            double rate = Double.parseDouble(txtFuelRate.getText());

            // Eğer kullanıcı eksi veya sıfır girerse, anlık göstergeyi bozma ve güncelleme!
            if(capacity <= 0 || rate <= 0) return;

            // Anlık yazımda değerler geçerliyse hafızayı güncelle
            sonGecerliKapasite = capacity;
            sonGecerliTuketim = rate;

            if (aktifTank == null) {
                lblFuel.setText(String.format("Kalan Yakıt: [ %.1f / %.1f L ]", capacity, capacity));
            }
            if (rate > 0 && lblMenzil != null) {
                lblMenzil.setText(String.format("Menzil: [ %.1f px ]", (capacity / rate) * 100.0));
            }
        } catch (NumberFormatException ignored) {}
    }

    private void anlikYakitKontrolu() {
        try {
            double capacity = Double.parseDouble(txtFuelCapacity.getText());
            double rate = Double.parseDouble(txtFuelRate.getText());

            // Eğer negatif veya sıfırsa manuel olarak hata fırlat
            if (capacity <= 0 || rate <= 0) {
                throw new NumberFormatException("Değerler pozitif olmalı.");
            }

            // Değerler tamamen geçerliyse hafızadaki son başarılı durumu güncelle
            sonGecerliKapasite = capacity;
            sonGecerliTuketim = rate;

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz Parametre",
                    "Yakıt kapasitesi ve tüketim oranı pozitif sayı olmalıdır!\n\nGeçersiz giriş iptal edildi, değerler son geçerli haline döndürüldü.");

            // Hata anında değerleri DEFAULT yerine SON BAŞARILI DURUMA geri al
            txtFuelCapacity.setText(String.valueOf(sonGecerliKapasite));
            txtFuelRate.setText(String.valueOf(sonGecerliTuketim));
        }
    }

    private void setMode(Mode newMode) {
        currentMode = newMode;
        if (currentMode == Mode.BASE) {
            btnBaseMode.setStyle("-fx-background-color: #33691e; -fx-border-color: #7cb342; -fx-border-width: 2px;");
            btnObjMode.setStyle("");
            lblStatusBar.setText("Durum Çubuğu: Üs Konumu Seç modundasınız.");
        } else if (currentMode == Mode.OBJECTIVE) {
            btnObjMode.setStyle("-fx-background-color: #33691e; -fx-border-color: #7cb342; -fx-border-width: 2px;");
            btnBaseMode.setStyle("");
            lblStatusBar.setText("Durum Çubuğu: Hedef Noktası Ekle modundasınız.");
        } else {
            btnBaseMode.setStyle("");
            btnObjMode.setStyle("");
        }
    }

    private void handleMapClick(double x, double y) {
        double maxX = mapPane.getWidth();
        double maxY = mapPane.getHeight();
        if (x < 10 || y < 10 || x > maxX - 10 || y > maxY - 10) return;
        if (currentMode == Mode.BASE) placeBase(x, y);
        else if (currentMode == Mode.OBJECTIVE) placeObjective(x, y);
    }

    private void placeBase(double x, double y) {
        mapPane.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("baseIcon"));
        baseNode = new Base(new Coordinate(x, y));
        Polygon house = new Polygon(0.0, -15.0, -15.0, 0.0, -10.0, 0.0, -10.0, 15.0, 10.0, 15.0, 10.0, 0.0, 15.0, 0.0);
        house.setFill(Color.DODGERBLUE); house.setLayoutX(x); house.setLayoutY(y); house.setId("baseIcon");
        Label lbl = new Label("[HQ]"); lbl.setTextFill(Color.DODGERBLUE); lbl.setLayoutX(x - 12); lbl.setLayoutY(y + 15); lbl.setId("baseIcon");
        mapPane.getChildren().addAll(house, lbl);
    }

    private void placeObjective(double x, double y) {
        String objName = "Hedef " + objectiveCounter;
        Objective obj = new Objective(new Coordinate(x, y), objName);
        objectiveNodes.add(obj);

        // Hedef ve etiketi tek bir kapsayıcıda (VBox) birleştiriyoruz
        javafx.scene.layout.VBox markerBox = new javafx.scene.layout.VBox(2);
        markerBox.setLayoutX(x - 20);
        markerBox.setLayoutY(y - 10);
        markerBox.setPickOnBounds(true); // Kapsayıcının tüm alanını tıklanabilir yapar
        markerBox.setStyle("-fx-background-color: transparent;");

        javafx.scene.text.Text cross = new javafx.scene.text.Text("(X)");
        cross.setFill(Color.CRIMSON);

        Label lbl = new Label(objName);
        lbl.setTextFill(Color.WHITE);

        markerBox.getChildren().addAll(cross, lbl);

        // --- SAĞ TIK İLE SİLME VE ONAY PENCERESİ ---
        javafx.event.EventHandler<javafx.scene.input.MouseEvent> silmeOlayi = e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                e.consume(); // Olayın haritaya (alt katmana) geçmesini kesin olarak engeller!

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Hedef İptal Onayı");
                alert.setHeaderText(null);
                alert.setContentText(objName + " noktasını silmek istediğinize emin misiniz?");

                javafx.scene.control.ButtonType btnEvet = new javafx.scene.control.ButtonType("Evet");
                javafx.scene.control.ButtonType btnHayir = new javafx.scene.control.ButtonType("Hayır", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(btnEvet, btnHayir);

                alert.showAndWait().ifPresent(type -> {
                    if (type == btnEvet) {
                        objectiveNodes.remove(obj);
                        mapPane.getChildren().remove(markerBox); // Tüm kutuyu haritadan siler
                        kesinlesmisRota.clear();
                        mapPane.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("routeObj"));
                        lblStatus.setText("Operasyon Durumu: [ ROTA GÜNCELLENMELİ ]");
                        lblStatus.setTextFill(Color.ORANGE);
                    }
                });
            }
        };
        objectiveVisuals.put(obj, markerBox); // Hedefi ve görsel kutusunu eşleştiriyoruz
        // Tıklama olayını tüm kutuya tanımlıyoruz
        markerBox.setOnMousePressed(silmeOlayi);

        mapPane.getChildren().add(markerBox);
        objectiveCounter++;
    }

    private void clearMap() {
        if(activeTransition != null) activeTransition.stop();
        mapPane.getChildren().removeIf(node -> node.getId() == null || (!node.getId().equals("gridLine")));
        objectiveVisuals.clear();
        baseNode = null; objectiveNodes.clear(); kesinlesmisRota.clear();
        aktifTank = null; sonAnimasyonKonumu = null; animTankGorseli = null; objectiveCounter = 1;
        setMode(Mode.NONE); updateTelemetryAndRange();
        lblDistance.setText("Kat Edilen Yol: [ 0.0 px ]");
        lblStatus.setText("Operasyon Durumu: [ BEKLEMEDE ]");
    }

    private void validateAndStart() {
        // REQ-ERR-3.1 ve REQ-ERR-3.2: Base ve En Az 2 Hedef Kontrolü
        if (baseNode == null) {
            showAlert(Alert.AlertType.WARNING, "Eksik Veri", "Lütfen önce tankın çıkış noktası olacak Askeri Üssü (HQ) haritaya yerleştirin!");
            return;
        }
        if (objectiveNodes.size() < 2) {
            showAlert(Alert.AlertType.WARNING, "Eksik Veri", "Görevi başlatabilmek için haritaya en az iki hedef noktası eklemelisiniz!");
            return;
        }

        List<Objective> yeniHedefler = new ArrayList<>(objectiveNodes);
        yeniHedefler.removeAll(kesinlesmisRota);

        if (yeniHedefler.isEmpty()) {
            if (activeTransition != null && activeTransition.getStatus() == javafx.animation.Animation.Status.PAUSED) {
                activeTransition.play();
                lblStatus.setText("Operasyon Durumu: [ SEYİR HALİNDE ]");
            }
            return;
        }

        // Tıklanma anında değerlerin doğruluğunu son kez garanti altına almak için try-catch (Güvenlik Katmanı)
        double capacity;
        double rate;
        try {
            capacity = Double.parseDouble(txtFuelCapacity.getText());
            rate = Double.parseDouble(txtFuelRate.getText());
            if (capacity <= 0 || rate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            capacity = sonGecerliKapasite;
            rate = sonGecerliTuketim;
            txtFuelCapacity.setText(String.valueOf(capacity));
            txtFuelRate.setText(String.valueOf(rate));
        }

        if (aktifTank == null) {
            aktifTank = new Tank(baseNode.getLocation(), capacity, rate, capacity);
            sonAnimasyonKonumu = baseNode.getLocation();
        }

        double baslangicMesafe = aktifTank.getKatEdilenMesafe();

        MissionManager yonetici = new MissionManager(aktifTank);
        yonetici.setHedefler(yeniHedefler);

        // 1. Yeni rotayı hesapla ve sıralanmış listeyi yonetici'den geri al!
        List<Objective> optimizeEdilmisRota = yonetici.rotaHesapla(baseNode.getLocation());

        // 2. Artık karmaşık listeyi değil, bu kusursuz dizilmiş rotayı arayüze ekle
        kesinlesmisRota.addAll(optimizeEdilmisRota);

        double bitisMesafe = aktifTank.getKatEdilenMesafe();

        lblStatus.setText("Operasyon Durumu: [ SEYİR HALİNDE ]");
        drawRouteLinesAndMarkers(kesinlesmisRota);

        animateTankSegment(optimizeEdilmisRota, baslangicMesafe, bitisMesafe, rate, capacity);
    }

    private void drawRouteLinesAndMarkers(List<Objective> rota) {
        mapPane.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("routeObj"));
        Coordinate prevCoord = baseNode.getLocation();
        int step = 1;

        // 1. Hedefler Arası Normal Rotayı Çiz
        for (Objective obj : rota) {
            Coordinate nextCoord = obj.getLocation();
            Line line = new Line(prevCoord.getX(), prevCoord.getY(), nextCoord.getX(), nextCoord.getY());
            line.setStroke(Color.DODGERBLUE); line.setStrokeWidth(2); line.getStrokeDashArray().addAll(8d, 8d); line.setId("routeObj");
            double midX = (prevCoord.getX() + nextCoord.getX()) / 2; double midY = (prevCoord.getY() + nextCoord.getY()) / 2;
            double angle = Math.toDegrees(Math.atan2(nextCoord.getY() - prevCoord.getY(), nextCoord.getX() - prevCoord.getX()));
            Polygon arrow = new Polygon(0.0, -5.0, 10.0, 0.0, 0.0, 5.0); arrow.setFill(Color.DODGERBLUE); arrow.setLayoutX(midX); arrow.setLayoutY(midY); arrow.setRotate(angle); arrow.setId("routeObj");
            StackPane badge = new StackPane(); badge.setLayoutX(midX - 15); badge.setLayoutY(midY - 25); badge.setId("routeObj");
            Circle circle = new Circle(10, Color.web("#33691e")); circle.setStroke(Color.web("#7cb342"));
            Label lblStep = new Label(String.valueOf(step)); lblStep.setTextFill(Color.WHITE); lblStep.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            badge.getChildren().addAll(circle, lblStep);
            mapPane.getChildren().addAll(line, arrow, badge);
            prevCoord = nextCoord; step++;
        }

        // 2. PROFESYONEL UI MÜDAHALESİ: Eğer backend tankı üsse döndürdüyse, dönüş çizgisini UI kendisi çizer!
        if (aktifTank.getDurum().equals("GÖREV TAMAMLANDI") && !rota.isEmpty()) {
            Coordinate hqCoord = baseNode.getLocation();
            Line returnLine = new Line(prevCoord.getX(), prevCoord.getY(), hqCoord.getX(), hqCoord.getY());
            returnLine.setStroke(Color.ORANGE); // Dönüş yolunu Turuncu ile vurguluyoruz
            returnLine.setStrokeWidth(2);
            returnLine.getStrokeDashArray().addAll(8d, 8d);
            returnLine.setId("routeObj");

            double midX = (prevCoord.getX() + hqCoord.getX()) / 2;
            double midY = (prevCoord.getY() + hqCoord.getY()) / 2;

            StackPane badge = new StackPane();
            badge.setLayoutX(midX - 35);
            badge.setLayoutY(midY - 15);
            badge.setId("routeObj");
            Label lblReturn = new Label("Üsse Dönüş");
            lblReturn.setTextFill(Color.ORANGE);
            lblReturn.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #1a1a1a; -fx-padding: 2px; -fx-border-color: orange; -fx-border-radius: 3px;");
            badge.getChildren().add(lblReturn);

            mapPane.getChildren().addAll(returnLine, badge);
        }

        if (animTankGorseli != null) animTankGorseli.toFront();
    }

    private void animateTankSegment(List<Objective> yeniRotaSegmenti, double baslangicMesafe, double bitisMesafe, double rate, double capacity) {
        if (animTankGorseli == null) {
            animTankGorseli = new Polygon(-12.0, -8.0, 12.0, -8.0, 16.0, 0.0, 12.0, 8.0, -12.0, 8.0);
            animTankGorseli.setFill(Color.DARKOLIVEGREEN);
            animTankGorseli.setStroke(Color.LIGHTGREEN);
            animTankGorseli.setId("animTank");

            // Tank ilk oluşturulduğunda doğrudan Askeri Üssün (Base) konumuna yerleşsin
            if (baseNode != null) {
                animTankGorseli.setTranslateX(baseNode.getLocation().getX());
                animTankGorseli.setTranslateY(baseNode.getLocation().getY());
            }

            mapPane.getChildren().add(animTankGorseli);
        } else {
            animTankGorseli.setFill(Color.DARKOLIVEGREEN);
            animTankGorseli.setStroke(Color.LIGHTGREEN);
        }

        Path path = new Path();
        path.getElements().add(new MoveTo(sonAnimasyonKonumu.getX(), sonAnimasyonKonumu.getY()));
        for (Objective obj : yeniRotaSegmenti) {
            path.getElements().add(new LineTo(obj.getLocation().getX(), obj.getLocation().getY()));
        }

        boolean usseDonusVarMi = aktifTank.getDurum().equals("GÖREV TAMAMLANDI") && !yeniRotaSegmenti.isEmpty();
        if (usseDonusVarMi) {
            path.getElements().add(new LineTo(baseNode.getLocation().getX(), baseNode.getLocation().getY()));
        }

        activeTransition = new PathTransition();
        activeTransition.setNode(animTankGorseli); activeTransition.setPath(path);
        activeTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);

        int segmentCount = yeniRotaSegmenti.size() + (usseDonusVarMi ? 1 : 0);
        activeTransition.setDuration(Duration.seconds(Math.max(1, segmentCount * 2)));

        double segmentMesafeFarki = bitisMesafe - baslangicMesafe;

        // Hangi hedeflerin vurulacağını listeye kopyala
        List<Objective> vurulacakHedefler = new ArrayList<>(yeniRotaSegmenti);

        activeTransition.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double fraction = newTime.toMillis() / activeTransition.getDuration().toMillis();

            double anlikMesafe = baslangicMesafe + (segmentMesafeFarki * fraction);
            double harcananYakit = (anlikMesafe / 100.0) * rate;
            double kalanYakit = capacity - harcananYakit;

            if (kalanYakit <= 0) {
                kalanYakit = 0;
                activeTransition.pause();
                animTankGorseli.setFill(Color.DARKRED);
                animTankGorseli.setStroke(Color.RED);
                lblStatus.setText("Operasyon Durumu: [ GÖREV BAŞARISIZ ]");
                lblStatus.setTextFill(Color.RED);
                lblStatusBar.setText("YETERSİZ YAKIT! Tank Yolda Kaldı, Görev Başarısız!");

            }

            double currentX = animTankGorseli.getTranslateX();
            double currentY = animTankGorseli.getTranslateY();

            // REQ-SIM-2.3: HEDEF İMHA KONTROLÜ (Mesafe Sensörü)
            java.util.Iterator<Objective> iterator = vurulacakHedefler.iterator();
            while (iterator.hasNext()) {
                Objective hedef = iterator.next();
                double hX = hedef.getLocation().getX();
                double hY = hedef.getLocation().getY();

                if (Math.abs(currentX - hX) < 5.0 && Math.abs(currentY - hY) < 5.0) {
                    iterator.remove();
                    patlamaEfektiOlustur(hX, hY);

                    // --- GÖRSEL GERİ BİLDİRİM: İMHA EDİLDİ ---
                    javafx.scene.layout.VBox visual = objectiveVisuals.get(hedef);
                    if (visual != null) {
                        visual.setOpacity(0.4); // Saydamlığı azalt (sönükleştir)
                        // Çarpı işaretini (Text nesnesini) koyu gri yap
                        javafx.scene.text.Text cross = (javafx.scene.text.Text) visual.getChildren().get(0);
                        cross.setFill(Color.DARKGRAY);
                    }
                    // ----------------------------------------
                }
            }

            lblDistance.setText(String.format("Kat Edilen Yol: [ %.1f px ]", anlikMesafe));
            lblFuel.setText(String.format("Kalan Yakıt: [ %.1f L ]", kalanYakit));
            lblCoordinates.setText(String.format("CANLI TANK: [ X: %.1f | Y: %.1f ]", currentX, currentY));
        });

        activeTransition.setOnFinished(e -> {
            lblStatusBar.setText("Durum Çubuğu: GÖREV BAŞARILI! Tank Üsse Güvenli Dönüş Yaptı.");
            lblStatus.setText("Operasyon Durumu: [ GÖREV TAMAMLANDI ]");
            lblStatus.setTextFill(Color.web("#7cb342"));

            lblDistance.setText(String.format("Kat Edilen Yol: [ %.1f px ]", bitisMesafe));

            // Animasyon bittiğinde toplam harcanan yakıtı kesin olarak hesapla
            double toplamHarcananYakit = (bitisMesafe / 100.0) * rate;
            double gercekKalanYakit = capacity - toplamHarcananYakit;

            // Güvenlik: Yakıt eksiye düşerse 0'da tut
            if (gercekKalanYakit < 0) gercekKalanYakit = 0;

            // 1. Arka plandaki tankın verisini GÜNCELLE
            aktifTank.setYakitSeviyesi(gercekKalanYakit);

            // 2. Ekrana gerçek kalan yakıtı yazdır
            lblFuel.setText(String.format("Kalan Yakıt: [ %.1f L ]", gercekKalanYakit));

            if (usseDonusVarMi) {
                sonAnimasyonKonumu = baseNode.getLocation();
            } else if (!yeniRotaSegmenti.isEmpty()) {
                sonAnimasyonKonumu = yeniRotaSegmenti.get(yeniRotaSegmenti.size() - 1).getLocation();
            }
        });

        lblStatus.setTextFill(Color.WHITE);
        activeTransition.play();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }

    private void drawGrid() {
        mapPane.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("gridLine"));
        double width = Math.max(mapPane.getWidth(), 100); double height = Math.max(mapPane.getHeight(), 100);
        for (double i = 0; i <= width; i += 25) { Line line = new Line(i, 0, i, height); line.setStroke(Color.web("#223322")); line.setId("gridLine"); mapPane.getChildren().add(0, line); }
        for (double i = 0; i <= height; i += 25) { Line line = new Line(0, i, width, i); line.setStroke(Color.web("#223322")); line.setId("gridLine"); mapPane.getChildren().add(0, line); }
    }

    // REQ-SIM-2.3: Hedef İmha Efekti (Patlama ve Yanık İzi)
    private void patlamaEfektiOlustur(double x, double y) {
        javafx.scene.shape.Circle patlama = new javafx.scene.shape.Circle(x, y, 2);
        patlama.setFill(javafx.scene.paint.Color.TRANSPARENT);
        patlama.setStroke(javafx.scene.paint.Color.ORANGERED);
        patlama.setStrokeWidth(3);
        mapPane.getChildren().add(patlama);

        // Şok Dalgası Animasyonu (Büyüme)
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(500), patlama);
        st.setToX(10);
        st.setToY(10);

        // Şok Dalgası Animasyonu (Kaybolma)
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), patlama);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        st.play();
        ft.play();

        // Patlama bitince yere siyah/gri bir krater izi bırak
        ft.setOnFinished(e -> {
            mapPane.getChildren().remove(patlama);
            javafx.scene.shape.Circle kaliciIz = new javafx.scene.shape.Circle(x, y, 8, javafx.scene.paint.Color.rgb(40, 40, 40, 0.8));
            mapPane.getChildren().add(kaliciIz);
            kaliciIz.toBack(); // İzin haritanın en altına gitmesi için
        });
    }
}