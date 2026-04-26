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
import models.dto.BudgetDTO;
import models.dto.BudgetReportDTO;
import models.dto.DateRangeDTO;
import models.dto.DeleteRequestDTO;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dpPeriod;
    @FXML private TextField txtPlannedAmount;
    @FXML private TextField txtCategoryId;
    @FXML private TableView<BudgetReportDTO> tableBudgets;
    @FXML private TableColumn<BudgetReportDTO, Integer> colId;
    @FXML private TableColumn<BudgetReportDTO, LocalDate> colPeriod;
    @FXML private TableColumn<BudgetReportDTO, String> colType;
    @FXML private TableColumn<BudgetReportDTO, String> colCategory;
    @FXML private TableColumn<BudgetReportDTO, BigDecimal> colPlan;
    @FXML private TableColumn<BudgetReportDTO, BigDecimal> colFact;
    @FXML private TableColumn<BudgetReportDTO, BigDecimal> colDeviation;
    @FXML private TableColumn<BudgetReportDTO, Double> colPercent;
    @FXML private TableColumn<BudgetReportDTO, String> colStatus;
    @FXML private TableColumn<BudgetReportDTO, Void> colActions;
    @FXML private Label lblMessage;

    private ObservableList<BudgetReportDTO> budgetList = FXCollections.observableArrayList();
    private BudgetReportDTO currentEditing = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbType.getItems().clear();
        cbType.getItems().addAll("Доход", "Расход");
        cbType.getSelectionModel().selectFirst();

        // Устанавливаем дату по умолчанию (1-е число текущего месяца)
        LocalDate defaultDate = LocalDate.now().withDayOfMonth(1);
        dpPeriod.setValue(defaultDate);

        txtPlannedAmount.setText("0.00");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));

        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            String display;
            if (type == null || type.isEmpty()) {
                display = "Расход";
            } else if (type.equalsIgnoreCase("INCOME") || type.equalsIgnoreCase("income")) {
                display = "Доход";
            } else {
                display = "Расход";
            }
            return new ReadOnlyObjectWrapper<>(display);
        });

        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPlan.setCellValueFactory(new PropertyValueFactory<>("plannedAmount"));
        colFact.setCellValueFactory(new PropertyValueFactory<>("factAmount"));
        colDeviation.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDeviation()));
        colPercent.setCellValueFactory(new PropertyValueFactory<>("completionPercent"));
        colStatus.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(translateStatus(cellData.getValue().getStatus())));

        setupActionColumn();
        tableBudgets.setItems(budgetList);

        // ✅ АВТООБНОВЛЕНИЕ: при смене даты в DatePicker таблица перезагружается
        // Теперь система будет искать бюджет на весь выбранный месяц
        dpPeriod.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("[UI] Дата изменена на: " + newVal + " (ищем бюджет на этот месяц)");
                loadBudgets();
            }
        });

        loadBudgets();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<BudgetReportDTO, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnApprove = new Button("✅");
            private final HBox pane = new HBox(5, btnApprove, btnEdit, btnDelete);
            {
                btnApprove.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                btnApprove.setOnAction(e -> {
                    BudgetReportDTO item = getTableView().getItems().get(getIndex());
                    handleApprove(item);
                });
                btnEdit.setOnAction(e -> {
                    BudgetReportDTO item = getTableView().getItems().get(getIndex());
                    handleEdit(item);
                });
                btnDelete.setOnAction(e -> {
                    BudgetReportDTO item = getTableView().getItems().get(getIndex());
                    handleDelete(item);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    String status = getTableView().getItems().get(getIndex()).getStatus();
                    pane.setVisible("DRAFT".equals(status) || "Черновик".equals(status));
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadBudgets() {
        LocalDate selectedDate = dpPeriod.getValue();
        if (selectedDate == null) {
            showMessage("Выберите дату периода!", true);
            return;
        }

        System.out.println("[Client] Загрузка бюджетов за месяц: " + selectedDate.getMonth() + " " + selectedDate.getYear());

        try {
            // Отправляем выбранную дату. Сервер сам поймет, что нужно искать бюджет на весь этот месяц.
            Request req = new Request(RequestType.GET_BUDGET_REPORT,
                    GsonUtil.getGson().toJson(new DateRangeDTO(selectedDate, selectedDate)));

            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();

            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                List<BudgetReportDTO> items = GsonUtil.getGson().fromJson(resp.getData(),
                        new com.google.gson.reflect.TypeToken<List<BudgetReportDTO>>(){}.getType());

                // ✅ ОБНОВЛЕНИЕ В JAVA FX ПОТОКЕ
                javafx.application.Platform.runLater(() -> {
                    budgetList.clear();
                    if (items != null) {
                        budgetList.addAll(items);
                        tableBudgets.refresh(); // Принудительное обновление визуала
                    }
                    System.out.println("[Client] Отображено записей: " + budgetList.size());
                });
            } else {
                showMessage("Ошибка загрузки: " + (resp != null ? resp.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Ошибка загрузки: " + e.getMessage(), true);
        }
    }

    @FXML
    void handleAdd(javafx.event.ActionEvent e) {
        LocalDate selectedDate = dpPeriod.getValue();
        if (selectedDate == null) {
            showMessage("Выберите период!", true);
            return;
        }

        String rawAmount = txtPlannedAmount.getText();
        if (rawAmount == null || rawAmount.trim().isEmpty()) {
            showMessage("Введите плановую сумму!", true);
            return;
        }

        try {
            // Безопасная обработка суммы
            String safeAmount = rawAmount.replace(",", ".").replaceAll("\\s+", "");
            if (!safeAmount.matches("^\\d+(\\.\\d+)?$")) {
                showMessage("Некорректная сумма. Используйте только цифры и точку.", true);
                return;
            }

            int selectedIndex = cbType.getSelectionModel().getSelectedIndex();
            String typeEnglish = (selectedIndex == 0) ? "INCOME" : "EXPENSE";

            System.out.println("[Client] Добавление: Месяц=" + selectedDate.getMonth() + ", Тип=" + typeEnglish + ", Сумма=" + safeAmount);

            BudgetDTO dto = new BudgetDTO();

            // ✅ ИСПРАВЛЕНО: Бюджет всегда привязывается к 1-му числу месяца.
            // Это стандартная практика: "Бюджет на Апрель" хранится как "01.04.2026".
            dto.setPeriod(selectedDate.withDayOfMonth(1));

            dto.setType(typeEnglish);
            dto.setPlannedAmount(new BigDecimal(safeAmount));

            String catText = txtCategoryId.getText().trim();
            if (!catText.isEmpty()) {
                try {
                    dto.setCategoryId(Integer.parseInt(catText));
                } catch (NumberFormatException ex) {
                    showMessage("ID категории должен быть числом", true);
                    return;
                }
            } else {
                dto.setCategoryId(null);
            }
            dto.setStatus("DRAFT");

            String json = GsonUtil.getGson().toJson(dto);
            Request req = new Request(RequestType.ADD_BUDGET, json);
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();

            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                showMessage("Бюджет успешно добавлен!", false);
                clearForm();
                loadBudgets(); // Обновляем таблицу
            } else {
                showMessage("Ошибка: " + (resp != null ? resp.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Ошибка: " + ex.getMessage(), true);
        }
    }

    @FXML
    void handleUpdate(javafx.event.ActionEvent e) {
        if (currentEditing == null) {
            showMessage("Выберите бюджет для редактирования!", true);
            return;
        }

        int selectedIndex = cbType.getSelectionModel().getSelectedIndex();
        String typeEnglish = (selectedIndex == 0) ? "INCOME" : "EXPENSE";

        try {
            BudgetDTO dto = new BudgetDTO();
            dto.setId(currentEditing.getId());
            dto.setPeriod(currentEditing.getPeriod());
            dto.setType(typeEnglish);
            dto.setPlannedAmount(new BigDecimal(txtPlannedAmount.getText().replace(",", ".")));

            if (!txtCategoryId.getText().isEmpty()) {
                dto.setCategoryId(Integer.parseInt(txtCategoryId.getText()));
            } else {
                dto.setCategoryId(null);
            }

            Request req = new Request(RequestType.UPDATE_BUDGET, GsonUtil.getGson().toJson(dto));
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();

            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                showMessage("Бюджет успешно обновлен!", false);
                clearForm();
                loadBudgets();
            } else {
                showMessage("Ошибка: " + (resp != null ? resp.getMessage() : "Нет ответа"), true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Ошибка: " + ex.getMessage(), true);
        }
    }

    private void handleEdit(BudgetReportDTO item) {
        currentEditing = item;

        if ("INCOME".equalsIgnoreCase(item.getType())) {
            cbType.getSelectionModel().select(0);
        } else {
            cbType.getSelectionModel().select(1);
        }

        dpPeriod.setValue(item.getPeriod());
        txtPlannedAmount.setText(item.getPlannedAmount().toString());

        if (item.getCategoryName() != null && item.getCategoryName().contains("ID:")) {
            txtCategoryId.setText(item.getCategoryName().split(": ")[1].trim());
        } else {
            txtCategoryId.clear();
        }
        showMessage("Редактирование бюджета #" + item.getId(), false);
    }

    private void handleDelete(BudgetReportDTO item) {
        new Alert(Alert.AlertType.CONFIRMATION, "Удалить бюджет?", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        try {
                            Request req = new Request(RequestType.DELETE_BUDGET, GsonUtil.getGson().toJson(new DeleteRequestDTO(item.getId())));
                            ClientSocket.getInstance().sendRequest(req);
                            Response resp = ClientSocket.getInstance().readResponse();
                            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                                showMessage("Бюджет удален", false);
                                loadBudgets();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showMessage("Ошибка: " + ex.getMessage(), true);
                        }
                    }
                });
    }

    private void handleApprove(BudgetReportDTO item) {
        new Alert(Alert.AlertType.CONFIRMATION, "Утвердить бюджет? После этого редактирование будет невозможно.", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        try {
                            Request req = new Request(RequestType.APPROVE_BUDGET, GsonUtil.getGson().toJson(new DeleteRequestDTO(item.getId())));
                            ClientSocket.getInstance().sendRequest(req);
                            Response resp = ClientSocket.getInstance().readResponse();
                            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                                showMessage("Бюджет утвержден", false);
                                loadBudgets();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showMessage("Ошибка: " + ex.getMessage(), true);
                        }
                    }
                });
    }

    @FXML void handleRefresh(javafx.event.ActionEvent e) { loadBudgets(); }

    @FXML void handleBack(javafx.event.ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Главное меню");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        cbType.getSelectionModel().selectFirst();
        // Не сбрасываем дату
        txtPlannedAmount.setText("0.00");
        txtCategoryId.clear();
        currentEditing = null;
        showMessage("", false);
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "DRAFT": return "Черновик";
            case "APPROVED": return "Утвержден";
            case "IN_PROGRESS": return "В выполнении";
            case "COMPLETED": return "Завершен";
            case "CANCELLED": return "Отменен";
            default: return status;
        }
    }

    private void showMessage(String msg, boolean err) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(err ? Color.RED : Color.GREEN);
        lblMessage.setVisible(!msg.isEmpty());
    }
}