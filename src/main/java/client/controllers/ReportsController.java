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
import models.dto.DateRangeDTO;
import models.dto.ProfitLossReportDTO;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblProfit;
    @FXML private Label lblTaxAmount;
    @FXML private Label lblMessage;
    @FXML private Button btnGenerate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());
    }

    @FXML
    void handleGenerateReport(javafx.event.ActionEvent event) {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showMessage("Выберите даты периода!", true);
            return;
        }
        if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
            showMessage("Дата окончания не может быть раньше даты начала!", true);
            return;
        }

        try {
            DateRangeDTO req = new DateRangeDTO(dpStartDate.getValue(), dpEndDate.getValue());
            Request request = new Request(RequestType.GET_PROFIT_LOSS_REPORT, GsonUtil.getGson().toJson(req));

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                ProfitLossReportDTO report = GsonUtil.getGson().fromJson(response.getData(), ProfitLossReportDTO.class);

                lblTotalIncome.setText(report.getTotalIncome().toString() + " BYN");
                lblTotalExpense.setText(report.getTotalExpense().toString() + " BYN");
                lblProfit.setText(report.getProfit().toString() + " BYN");
                lblTaxAmount.setText(report.getTaxAmount().toString() + " BYN");

                lblProfit.setTextFill(report.getProfit().compareTo(BigDecimal.ZERO) >= 0 ? Color.GREEN : Color.RED);
                showMessage("Отчёт успешно сформирован!", false);
            } else {
                showMessage("Ошибка: " + (response != null ? response.getMessage() : "Нет ответа"), true);
                clearResults();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
            clearResults();
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

    private void clearResults() {
        lblTotalIncome.setText("0.00 BYN");
        lblTotalExpense.setText("0.00 BYN");
        lblProfit.setText("0.00 BYN");
        lblTaxAmount.setText("0.00 BYN");
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
        lblMessage.setVisible(true);
    }
}