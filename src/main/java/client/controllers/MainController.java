package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import models.entities.User;

import java.io.IOException;

public class MainController {
    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        User user = ClientSocket.getInstance().getCurrentUser();
        if (user != null) {
            lblWelcome.setText("Добро пожаловать, " + user.getLogin() + " (Роль: " + user.getRoleId() + ")");
        }
    }

    @FXML
    void handleIncomes(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/incomes.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Доходы");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleExpenses(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/expenses.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Расходы");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleReferences(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/references.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Справочники");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleProfitCalculation(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/profit_calculation.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Расчёт прибыли");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleTaxCalculation(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/tax_calculation.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Расчёт налога");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleReports(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/reports.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Отчёты");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBudget(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/budget.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Бюджет");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSettings(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/settings.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Настройки");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        ClientSocket.getInstance().disconnect();
        try {
            new ClientApp().showLoginScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}