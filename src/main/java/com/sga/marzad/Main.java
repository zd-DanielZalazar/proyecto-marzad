package com.sga.marzad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        goToLogin();
    }

    public static void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            mainStage.setScene(scene);
            mainStage.setTitle("SGA – Login");
            mainStage.setMaximized(true);
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void goToMainView(String usuario, String rol) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainView.fxml"));
            Scene scene = new Scene(loader.load());
            mainStage.setScene(scene);
            mainStage.setTitle("SGA – Menú Principal");
            mainStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
