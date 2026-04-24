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
import models.entities.Expense;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ExpensesController implements Initializable {

    @FXML private TableView<Expense> tableExpenses;
    @FXML private TableColumn<Expense, Integer> colId;
    @FXML private TableColumn<Expense, LocalDate> colDate;
    @FXML private TableColumn<Expense, BigDecimal> colAmount;
    @FXML private TableColumn<Expense, Integer> colCategory;
    @FXML private TableColumn<Expense, Integer> colCounterparty;
    @FXML private TableColumn<Expense, String> colComment;
    @FXML private TableColumn<Expense, Void> colActions;

    @FXML private DatePicker datePickerExpense;
    @FXML private TextField txtAmount;
    @FXML private TextField txtCategoryId;
    @FXML private TextField txtCounterpartyId;
    @FXML private TextField txtComment;

    @FXML private Label lblFormTitle;
    @FXML private Label lblMessage;
    @FXML private Label lblStats;
    @FXML private Button btnAddExpense;
    @FXML private Button btnUpdateExpense;
    @FXML private Button btnCancel;

    private ObservableList<Expense> expensesList = FXCollections.observableArrayList();
    private Expense currentEditingExpense = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDate()));
        colAmount.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAmount()));
        colCategory.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCategoryId()));
        colCounterparty.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCounterpartyId()));
        colComment.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getComment()));

        setupActionColumn();

        tableExpenses.setItems(expensesList);
        tableExpenses.setSortPolicy(table -> true);

        loadExpenses();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<Expense, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                btnEdit.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleEditExpense(expense);
                });

                btnDelete.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleDeleteExpense(expense);
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

    private void loadExpenses() {
        try {
            LocalDate start = LocalDate.of(2000, 1, 1);
            LocalDate end = LocalDate.of(2099, 12, 31);


            String payload = GsonUtil.getGson().toJson(new DateRangeDTO(start, end));
            Request request = new Request(RequestType.GET_EXPENSES, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                List<Expense> expenses = GsonUtil.getGson().fromJson(response.getData(),
                        new com.google.gson.reflect.TypeToken<List<Expense>>(){}.getType());

                expensesList.clear();
                if (expenses != null) expensesList.addAll(expenses);

                updateStats();
                showMessage("Загружено записей: " + expensesList.size(), false);
            } else {
                showMessage("Ошибка загрузки: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка сети: " + e.getMessage(), true);
        }
    }

    @FXML
    void handleAddExpense(javafx.event.ActionEvent event) {
        try {
            if (!validateForm()) return;

            Expense newExpense = new Expense();
            newExpense.setDate(datePickerExpense.getValue());
            newExpense.setAmount(new BigDecimal(txtAmount.getText().replace(",", ".")));
            newExpense.setCategoryId(Integer.parseInt(txtCategoryId.getText()));
            newExpense.setComment(txtComment.getText());
            newExpense.setUserId(ClientSocket.getInstance().getCurrentUser().getId());

            if (!txtCounterpartyId.getText().isEmpty()) {
                newExpense.setCounterpartyId(Integer.parseInt(txtCounterpartyId.getText()));
            }

            String payload = GsonUtil.getGson().toJson(newExpense);
            Request request = new Request(RequestType.ADD_EXPENSE, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                showMessage("Расход успешно добавлен!", false);
                clearForm();
                loadExpenses();
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
    void handleUpdateExpense(javafx.event.ActionEvent event) {
        if (currentEditingExpense == null) {
            showMessage("Не выбран элемент для обновления!", true);
            return;
        }

        try {
            if (!validateForm()) return;


            UpdateTransactionDTO dto = new UpdateTransactionDTO(
                    currentEditingExpense.getId(),
                    datePickerExpense.getValue(),
                    new BigDecimal(txtAmount.getText().replace(",", ".")),
                    txtComment.getText(),
                    Integer.parseInt(txtCategoryId.getText()),
                    txtCounterpartyId.getText().isEmpty() ? null : Integer.parseInt(txtCounterpartyId.getText())
            );

            String payload = GsonUtil.getGson().toJson(dto);
            Request request = new Request(RequestType.UPDATE_EXPENSE, payload);

            ClientSocket.getInstance().sendRequest(request);
            Response response = ClientSocket.getInstance().readResponse();

            if (response != null && response.getStatus() == ResponseStatus.OK) {
                showMessage("Расход успешно обновлен!", false);
                clearForm();
                loadExpenses();
            } else {
                showMessage("Ошибка обновления: " + (response != null ? response.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка: " + e.getMessage(), true);
        }
    }

    private void handleDeleteExpense(Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление записи");
        alert.setContentText("Вы уверены, что хотите удалить расход #" + expense.getId() + " на сумму " + expense.getAmount() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Используем DTO для удаления
                    String payload = GsonUtil.getGson().toJson(new DeleteRequestDTO(expense.getId()));
                    Request request = new Request(RequestType.DELETE_EXPENSE, payload);

                    ClientSocket.getInstance().sendRequest(request);
                    Response response_delete = ClientSocket.getInstance().readResponse();

                    if (response_delete != null && response_delete.getStatus() == ResponseStatus.OK) {
                        showMessage("Расход успешно удален!", false);
                        loadExpenses();
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
    void handleEditExpense(Expense expense) {
        currentEditingExpense = expense;

        datePickerExpense.setValue(expense.getDate());
        txtAmount.setText(expense.getAmount().toString());
        txtCategoryId.setText(String.valueOf(expense.getCategoryId()));
        txtCounterpartyId.setText(expense.getCounterpartyId() != null ? String.valueOf(expense.getCounterpartyId()) : "");
        txtComment.setText(expense.getComment());

        lblFormTitle.setText("Редактировать расход #" + expense.getId());
        btnAddExpense.setVisible(false);
        btnUpdateExpense.setVisible(true);
        btnCancel.setVisible(true);
    }

    @FXML
    void handleCancel(javafx.event.ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleRefresh(javafx.event.ActionEvent event) {
        loadExpenses();
    }

    private boolean validateForm() {
        if (txtAmount.getText().isEmpty() || txtCategoryId.getText().isEmpty() || datePickerExpense.getValue() == null) {
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
        datePickerExpense.setValue(null);
        txtAmount.clear();
        txtCategoryId.clear();
        txtCounterpartyId.clear();
        txtComment.clear();

        currentEditingExpense = null;
        lblFormTitle.setText("Добавить новый расход");
        btnAddExpense.setVisible(true);
        btnUpdateExpense.setVisible(false);
        btnCancel.setVisible(false);
    }

    private void updateStats() {
        BigDecimal total = expensesList.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblStats.setText("Всего: " + expensesList.size() + " записей | Сумма: " + total + " BYN");
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
