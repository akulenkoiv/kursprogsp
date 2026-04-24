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
import models.dto.ProfitCalculationRequestDTO;
import models.dto.ProfitCalculationResponseDTO;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfitCalculationController implements Initializable {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblProfit;
    @FXML private Label lblMessage;
    @FXML private Button btnCalculate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());
    }

    @FXML
    void handleCalculate(javafx.event.ActionEvent event) {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showMessage("Выберите даты периода!", true);
            return;
        }
        if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
            showMessage("Дата окончания не может быть раньше даты начала!", true);
            return;
        }

        try {
            ProfitCalculationRequestDTO req = new ProfitCalculationRequestDTO(dpStartDate.getValue(), dpEndDate.getValue());
            Request request = new Request(RequestType.CALCULATE_PROFIT, GsonUtil.getGson().toJson(req));

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                ProfitCalculationResponseDTO result = GsonUtil.getGson().fromJson(response.getData(), ProfitCalculationResponseDTO.class);

                lblTotalIncome.setText(result.getTotalIncome().toString() + " BYN");
                lblTotalExpense.setText(result.getTotalExpense().toString() + " BYN");
                lblProfit.setText(result.getProfit().toString() + " BYN");

                lblProfit.setTextFill(result.getProfit().compareTo(BigDecimal.ZERO) >= 0 ? Color.GREEN : Color.RED);
                showMessage("Расчёт завершён", false);
            } else {
                showMessage("Ошибка: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
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