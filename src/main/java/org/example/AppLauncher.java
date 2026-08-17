package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/main.fxml")));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Askeri Tank Simülasyonu");
        primaryStage.setScene(scene);

        // 1. ADIM: Pencereyi tüm ekrana yay (Maximized)
        primaryStage.setMaximized(true);

        // 2. ADIM: O boyuttayken pencereyi KİLİTLE (Kullanıcı değiştiremesin)
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}