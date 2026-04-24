package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import config.Config;
import enums.RequestType;
import enums.ResponseStatus;
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

        int port = Config.getIntProperty("server.port");
        boolean connected = ClientSocket.getInstance().connect("localhost", port);

        if (!connected) {
            lblMessage.setText("Ошибка подключения к серверу");
            lblMessage.setVisible(true);
            return;
        }

        LoginDTO loginDto = new LoginDTO(login, password);
        Request request = new Request(RequestType.LOGIN, GsonUtil.getGson().toJson(loginDto));

        ClientSocket.getInstance().sendRequest(request);
        Response response = ClientSocket.getInstance().readResponse();

        if (response != null && response.getStatus() == ResponseStatus.OK) {
            User authUser = GsonUtil.getGson().fromJson(response.getData(), User.class);
            ClientSocket.getInstance().setCurrentUser(authUser);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
                ClientApp.getPrimaryStage().setScene(new Scene(root));
                ClientApp.getPrimaryStage().setTitle("Главное меню");
            } catch (Exception e) {
                e.printStackTrace();
                lblMessage.setText("Ошибка загрузки главного окна");
                lblMessage.setVisible(true);
            }
        } else {
            lblMessage.setText("Ошибка: " + (response != null ? response.getMessage() : "Unknown"));
            lblMessage.setVisible(true);
        }
    }
}