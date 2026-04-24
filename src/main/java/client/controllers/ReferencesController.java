package client.controllers;

import client.ClientApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;

import java.io.IOException;

public class ReferencesController {

    @FXML
    void handleIncomeCategories(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/income_categories.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Категории доходов");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleExpenseCategories(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/expense_categories.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Категории расходов");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleCounterparties(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/counterparties.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Контрагенты");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Главное меню");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
