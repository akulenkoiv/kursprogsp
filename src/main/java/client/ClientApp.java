package client;

import config.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // статик, доступ из контроллеров
        showLoginScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        //создает граф объектов интерфейса
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Авторизация - ИП Финансы");
        primaryStage.show(); // создание сцены
    }

    public void showMainScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Главное меню");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}