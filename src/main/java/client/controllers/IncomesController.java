package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import enums.RequestType;
import enums.ResponseStatus;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import models.dto.DateRangeDTO;
import models.dto.DeleteRequestDTO;
import models.dto.UpdateTransactionDTO;
import models.entities.Income;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class IncomesController implements Initializable {

    @FXML private TableView<Income> tableIncomes;
    @FXML private TableColumn<Income, Integer> colId;
    @FXML private TableColumn<Income, LocalDate> colDate;
    @FXML private TableColumn<Income, BigDecimal> colAmount;
    @FXML private TableColumn<Income, Integer> colCategory;
    @FXML private TableColumn<Income, Integer> colCounterparty;
    @FXML private TableColumn<Income, String> colComment;
    @FXML private TableColumn<Income, Void> colActions;

    @FXML private DatePicker datePickerIncome;
    @FXML private TextField txtAmount;
    @FXML private TextField txtCategoryId;
    @FXML private TextField txtCounterpartyId;
    @FXML private TextField txtComment;

    @FXML private Label lblFormTitle;
    @FXML private Label lblMessage;
    @FXML private Label lblStats;
    @FXML private Button btnAddIncome;
    @FXML private Button btnUpdateIncome;
    @FXML private Button btnCancel;

    private ObservableList<Income> incomesList = FXCollections.observableArrayList();
    private Income currentEditingIncome = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDate()));
        colAmount.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));
        colCategory.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCategoryId()));
        colCounterparty.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCounterpartyId()));
        colComment.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getComment()));

        setupActionColumn();

        tableIncomes.setItems(incomesList);
        tableIncomes.setSortPolicy(table -> true);

        loadIncomes();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<Income, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                btnEdit.setOnAction(event -> {
                    Income income = getTableView().getItems().get(getIndex());
                    handleEditIncome(income);
                });

                btnDelete.setOnAction(event -> {
                    Income income = getTableView().getItems().get(getIndex());
                    handleDeleteIncome(income);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadIncomes() {
        try {
            LocalDate start = LocalDate.of(2000, 1, 1);
            LocalDate end = LocalDate.of(2099, 12, 31);

            String payload = GsonUtil.getGson().toJson(new DateRangeDTO(start, end));
            Request request = new Request(RequestType.GET_INCOMES, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                List<Income> incomes = GsonUtil.getGson().fromJson(response.getData(),
                        new com.google.gson.reflect.TypeToken<List<Income>>(){}.getType());

                incomesList.clear();
                if (incomes != null) incomesList.addAll(incomes);

                updateStats();
                showMessage("Загружено записей: " + incomesList.size(), false);
            } else {
                showMessage("Ошибка загрузки: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
        }
    }

    @FXML
    void handleAddIncome(javafx.event.ActionEvent event) {
        try {
            if (!validateForm()) return;

            Income newIncome = new Income();
            newIncome.setDate(datePickerIncome.getValue());
            newIncome.setAmount(new BigDecimal(txtAmount.getText().replace(",", ".")));
            newIncome.setCategoryId(Integer.parseInt(txtCategoryId.getText()));
            newIncome.setComment(txtComment.getText());
            newIncome.setUserId(ClientSocket.getInstance().getCurrentUser().getId());

            if (!txtCounterpartyId.getText().isEmpty()) {
                newIncome.setCounterpartyId(Integer.parseInt(txtCounterpartyId.getText()));
            }

            String payload = GsonUtil.getGson().toJson(newIncome);
            Request request = new Request(RequestType.ADD_INCOME, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                showMessage("Доход успешно добавлен!", false);
                clearForm();
                loadIncomes();
            } else {
                showMessage("Ошибка добавления: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (NumberFormatException e) {
            showMessage("Проверьте правильность ввода чисел!", true);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
        }
    }

    @FXML
    void handleUpdateIncome(javafx.event.ActionEvent event) {
        if (currentEditingIncome == null) {
            showMessage("Не выбран элемент для обновления!", true);
            return;
        }

        try {
            if (!validateForm()) return;

            UpdateTransactionDTO dto = new UpdateTransactionDTO(
                    currentEditingIncome.getId(),
                    datePickerIncome.getValue(),
                    new BigDecimal(txtAmount.getText().replace(",", ".")),
                    txtComment.getText(),
                    Integer.parseInt(txtCategoryId.getText()),
                    txtCounterpartyId.getText().isEmpty() ? null : Integer.parseInt(txtCounterpartyId.getText())
            );

            String payload = GsonUtil.getGson().toJson(dto);
            Request request = new Request(RequestType.UPDATE_INCOME, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                showMessage("Доход успешно обновлен!", false);
                clearForm();
                loadIncomes();
            } else {
                showMessage("Ошибка обновления: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка: " + e.getMessage(), true);
        }
    }

    private void handleDeleteIncome(Income income) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление записи");
        alert.setContentText("Вы уверены, что хотите удалить доход #" + income.getId() + " на сумму " + income.getAmount() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String payload = GsonUtil.getGson().toJson(new DeleteRequestDTO(income.getId()));
                    Request request = new Request(RequestType.DELETE_INCOME, payload);

                    ClientSocket.getInstance().sendRequest(request);
                    Response response_delete = ClientSocket.getInstance().readResponse();

                    if (response_delete != null && response_delete.getStatus() == ResponseStatus.OK) {
                        showMessage("Доход успешно удален!", false);
                        loadIncomes();
                    } else {
                        showMessage("Ошибка удаления: " + (response_delete != null ? response_delete.getMessage() : "Нет ответа"), true);
                    }
                } catch (Exception e) {
                    showMessage("Ошибка: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    void handleEditIncome(Income income) {
        currentEditingIncome = income;

        datePickerIncome.setValue(income.getDate());
        txtAmount.setText(income.getAmount().toString());
        txtCategoryId.setText(String.valueOf(income.getCategoryId()));
        txtCounterpartyId.setText(income.getCounterpartyId() != null ? String.valueOf(income.getCounterpartyId()) : "");
        txtComment.setText(income.getComment());

        lblFormTitle.setText("Редактировать доход #" + income.getId());
        btnAddIncome.setVisible(false);
        btnUpdateIncome.setVisible(true);
        btnCancel.setVisible(true);
    }

    @FXML
    void handleCancel(javafx.event.ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleRefresh(javafx.event.ActionEvent event) {
        loadIncomes();
    }

    private boolean validateForm() {
        if (txtAmount.getText().isEmpty() || txtCategoryId.getText().isEmpty() || datePickerIncome.getValue() == null) {
            showMessage("Заполните обязательные поля: дата, сумма, категория!", true);
            return false;
        }

        try {
            new BigDecimal(txtAmount.getText().replace(",", "."));
            Integer.parseInt(txtCategoryId.getText());

            if (!txtCounterpartyId.getText().isEmpty()) {
                Integer.parseInt(txtCounterpartyId.getText());
            }
        } catch (NumberFormatException e) {
            showMessage("Сумма, категория и контрагент должны быть числами!", true);
            return false;
        }
        return true;
    }

    private void clearForm() {
        datePickerIncome.setValue(null);
        txtAmount.clear();
        txtCategoryId.clear();
        txtCounterpartyId.clear();
        txtComment.clear();

        currentEditingIncome = null;
        lblFormTitle.setText("Добавить новый доход");
        btnAddIncome.setVisible(true);
        btnUpdateIncome.setVisible(false);
        btnCancel.setVisible(false);
    }

    private void updateStats() {
        BigDecimal total = incomesList.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblStats.setText("Всего: " + incomesList.size() + " записей | Сумма: " + total + " BYN");
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
        lblMessage.setVisible(true);
    }

    @FXML
    void handleBackToMenu(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Главное меню");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}