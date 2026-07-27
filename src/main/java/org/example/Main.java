package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Hazırladığımız FXML dosyasını belleğe yüklüyoruz
        Parent root = FXMLLoader.load(getClass().getResource("/view/main.fxml"));

        // Şablonu sahneye koyuyoruz
        Scene scene = new Scene(root);

        // Pencere ayarları
        primaryStage.setTitle("Tank Görev Simülatörü - Askeri Radar (V1.0)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Harita boyutu bozulmasın diye boyutu sabitliyoruz
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}