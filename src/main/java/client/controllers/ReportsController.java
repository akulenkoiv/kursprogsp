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
import javafx.stage.FileChooser;
import models.dto.DateRangeDTO;
import models.dto.ProfitLossReportDTO;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private ProfitLossReportDTO currentReport = null;
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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


                currentReport = report;


                lblTotalIncome.setText(formatDecimal(report.getTotalIncome()) + " BYN");
                lblTotalExpense.setText(formatDecimal(report.getTotalExpense()) + " BYN");
                lblProfit.setText(formatDecimal(report.getProfit()) + " BYN");
                lblTaxAmount.setText(formatDecimal(report.getTaxAmount()) + " BYN");


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
    void handleExport(javafx.event.ActionEvent event) {
        if (currentReport == null) {
            showMessage("Сначала сформируйте отчёт!", true);
            return;
        }


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчёт");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Таблицы Excel (CSV)", "*.csv")
        );
        String fileName = "Report_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv";
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(lblProfit.getScene().getWindow());

        if (file != null) {
            try {

                StringBuilder csv = new StringBuilder();


                csv.append("ОТЧЕТ О ПРИБЫЛЯХ И УБЫТКАХ ИП").append("\n");
                csv.append("Дата формирования: ").append(LocalDate.now().format(CSV_DATE_FORMAT)).append("\n");
                csv.append("Период: ")
                        .append(currentReport.getStartDate().format(CSV_DATE_FORMAT))
                        .append(" - ")
                        .append(currentReport.getEndDate().format(CSV_DATE_FORMAT))
                        .append("\n\n");


                csv.append("Показатель;Сумма (BYN)\n");


                csv.append("Доходы;").append(formatDecimal(currentReport.getTotalIncome())).append("\n");
                csv.append("Расходы;").append(formatDecimal(currentReport.getTotalExpense())).append("\n");
                csv.append("Прибыль до налогообложения;").append(formatDecimal(currentReport.getProfit())).append("\n");
                csv.append("Налог (УСН 6%);").append(formatDecimal(currentReport.getTaxAmount())).append("\n");


                BigDecimal netProfit = currentReport.getProfit().subtract(currentReport.getTaxAmount());
                csv.append("ЧИСТАЯ ПРИБЫЛЬ;").append(formatDecimal(netProfit)).append("\n");

                //  Запись в файл с UTF-8+BOM для корректного открытия в Excel
                try (BufferedWriter writer = new BufferedWriter(
                        new FileWriter(file, java.nio.charset.Charset.forName("UTF-8")))) {
                    writer.write('\ufeff'); // BOM для Excel
                    writer.write(csv.toString());
                }

                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Отчёт сохранён:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка",
                        "Не удалось сохранить файл:\n" + e.getMessage());
            }
        }
    }


    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
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
        currentReport = null;
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
        lblMessage.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(lblProfit.getScene().getWindow());
        alert.showAndWait();
    }
}