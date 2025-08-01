package com.sga.marzad;

import com.sga.marzad.utils.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        try {
            ConexionBD.getConnection().close();
            System.out.println("✅ Conexión a BD OK");
        } catch (Exception e) {
            System.err.println("❌ Error al conectar a BD:");
            e.printStackTrace();
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("SGA – Login");
        stage.setScene(scene);
        stage.show();
    }
}
