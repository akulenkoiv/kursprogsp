package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import enums.Roles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import models.entities.User;

import java.io.IOException;

public class MainController {
    @FXML private Label lblWelcome;
    @FXML private Button btnIncomes;
    @FXML private Button btnExpenses;
    @FXML private Button btnReferences;
    @FXML private Button btnProfit;
    @FXML private Button btnReports;
    @FXML private Button btnBudget;
    @FXML private Button btnSettings;
    @FXML private Button btnTax;

    @FXML
    public void initialize() {
        User user = ClientSocket.getInstance().getCurrentUser();
        if (user != null) {
            lblWelcome.setText("Добро пожаловать, " + user.getLogin() + " (Роль: " + getRoleName(user.getRoleId()) + ")");
            applyRoleRestrictions(user.getRoleId());
        }
    }

    private void applyRoleRestrictions(int roleId) {
        // Ограничения для Бухгалтера
        if (roleId == Roles.ACCOUNTANT.getLevel()) {
            if (btnReferences != null) btnReferences.setVisible(false);
            if (btnBudget != null) btnBudget.setVisible(false);
        }

        // Ограничения для всех, кроме Администратора
        if (roleId != Roles.ADMIN.getLevel()) {
            if (btnSettings != null) btnSettings.setVisible(false);
        }
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 3: return "Администратор";
            case 2: return "ИП";
            case 1: return "Бухгалтер";
            default: return "Неизвестно";
        }
    }

    @FXML
    void handleIncomes(ActionEvent event) { navigateTo("/fxml/incomes.fxml", "Доходы"); }

    @FXML
    void handleExpenses(ActionEvent event) { navigateTo("/fxml/expenses.fxml", "Расходы"); }

    @FXML
    void handleReferences(ActionEvent event) { navigateTo("/fxml/references.fxml", "Справочники"); }

    @FXML
    void handleProfitCalculation(ActionEvent event) { navigateTo("/fxml/profit_calculation.fxml", "Расчёт прибыли"); }

    @FXML
    void handleTaxCalculation(ActionEvent event) { navigateTo("/fxml/tax_calculation.fxml", "Расчёт налога"); }

    @FXML
    void handleReports(ActionEvent event) { navigateTo("/fxml/reports.fxml", "Отчёты"); }

    @FXML
    void handleBudget(ActionEvent event) { navigateTo("/fxml/budget.fxml", "Бюджет"); }

    @FXML
    void handleSettings(ActionEvent event) {
        navigateTo("/fxml/users.fxml", "Управление пользователями");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // 1. Принудительно закрываем сокет
        ClientSocket.getInstance().disconnect();

        try {
            // 2. Делаем паузу 300мс, чтобы ОС успела освободить порт (решает проблему timeout)
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            // 3. Возвращаемся на экран входа
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Вход");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}