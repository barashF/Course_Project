package com.example.filesystemanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/filesystem/main.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Анализ файловой системы");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}