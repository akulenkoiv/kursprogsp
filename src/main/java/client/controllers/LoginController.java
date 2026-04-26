package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import config.Config;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import models.dto.LoginDTO;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

public class LoginController {
    @FXML private TextField txtLogin;
    @FXML private PasswordField passPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    @FXML
    void handleLogin(ActionEvent event) {
        lblMessage.setText("");
        lblMessage.setVisible(false);

        String login = txtLogin.getText();
        String password = passPassword.getText();

        if (login.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Заполните все поля");
            lblMessage.setVisible(true);
            return;
        }

        btnLogin.setDisable(true);
        lblMessage.setText("Подключение...");
        lblMessage.setVisible(true);

        Task<Response> loginTask = new Task<Response>() {
            @Override
            protected Response call() throws Exception {
                // Принудительный разрыв старого соединения
                ClientSocket.getInstance().disconnect();

                // Небольшая пауза для освобождения порта
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                int port = Config.getIntProperty("server.port");
                boolean connected = ClientSocket.getInstance().connect("localhost", port);

                if (!connected) {
                    return new Response(ResponseStatus.ERROR, "Failed to connect to server", null);
                }

                LoginDTO loginDto = new LoginDTO(login, password);
                Request request = new Request(RequestType.LOGIN, GsonUtil.getGson().toJson(loginDto));
                return ClientSocket.getInstance().sendRequestSync(request);
            }
        };

        loginTask.setOnSucceeded(e -> {
            Response response = loginTask.getValue();
            if (response != null && response.getStatus() == ResponseStatus.OK && response.getData() != null) {
                try {
                    User authUser = GsonUtil.getGson().fromJson(response.getData(), User.class);
                    ClientSocket.getInstance().setCurrentUser(authUser);

                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
                    ClientApp.getPrimaryStage().setScene(new Scene(root));
                    ClientApp.getPrimaryStage().setTitle("Главное меню");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblMessage.setText("Ошибка загрузки главного окна");
                    lblMessage.setVisible(true);
                }
            } else {
                lblMessage.setText("Ошибка: " + (response != null ? response.getMessage() : "Сервер не ответил"));
                lblMessage.setVisible(true);
            }
            btnLogin.setDisable(false);
        });

        loginTask.setOnFailed(e -> {
            lblMessage.setText("Ошибка сети: " + loginTask.getException().getMessage());
            lblMessage.setVisible(true);
            btnLogin.setDisable(false);
        });

        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }
}