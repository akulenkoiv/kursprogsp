package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import models.dto.TaxCalculationRequestDTO;
import models.entities.TaxPayment;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TaxCalculationController implements Initializable {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbRegime;
    @FXML private Label lblTaxAmount;
    @FXML private Label lblTaxBase;
    @FXML private Label lblMessage;
    @FXML private Button btnCalculate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());

        cbRegime.getItems().addAll(
                "УСН Доходы (6%) - налог со всех доходов",
                "УСН Д-Р (15%) - налог с разницы доходов и расходов"
        );
        cbRegime.getSelectionModel().selectFirst();
    }

    @FXML
    void handleCalculate(javafx.event.ActionEvent event) {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showMessage("Выберите даты периода!", true);
            return;
        }
        if (cbRegime.getValue() == null) {
            showMessage("Выберите систему налогообложения!", true);
            return;
        }

        try {
            String regimeKey = cbRegime.getValue().contains("Д-Р") ?
                    "USN_INCOME_EXPENSE" : "USN_INCOME";

            TaxCalculationRequestDTO req = new TaxCalculationRequestDTO(
                    dpStartDate.getValue(), dpEndDate.getValue(), regimeKey);

            Request request = new Request(RequestType.CALCULATE_TAX, GsonUtil.getGson().toJson(req));
            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                TaxPayment payment = GsonUtil.getGson().fromJson(response.getData(), TaxPayment.class);
                lblTaxAmount.setText(payment.getAmount().toString() + " BYN");
                lblTaxAmount.setTextFill(Color.DARKGREEN);
                showMessage("Налог успешно рассчитан! Можно пересчитать.", false);
            } else {
                showMessage("Ошибка: " + (response != null ? response.getMessage() : "Нет ответа"), true);
                lblTaxAmount.setText("0.00 BYN");
                lblTaxBase.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
            lblTaxAmount.setText("0.00 BYN");
            lblTaxBase.setText("");
        }
    }

    @FXML
    void handleBack(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Главное меню");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
        lblMessage.setVisible(true);
    }
}