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
import models.dto.DeleteRequestDTO;
import models.entities.ExpenseCategory;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExpenseCategoriesController implements Initializable {
    @FXML private TableView<ExpenseCategory> tableCategories;
    @FXML private TableColumn<ExpenseCategory, Integer> colId;
    @FXML private TableColumn<ExpenseCategory, String> colName;
    @FXML private TableColumn<ExpenseCategory, Boolean> colDeductible;
    @FXML private TableColumn<ExpenseCategory, Void> colActions;

    @FXML private TextField txtName;
    @FXML private CheckBox chkDeductible;
    @FXML private Label lblFormTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private ObservableList<ExpenseCategory> list = FXCollections.observableArrayList();
    private ExpenseCategory currentEditing = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDeductible.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().isDeductible()));
        setupActionColumn();
        tableCategories.setItems(list);
        load();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<ExpenseCategory, Void>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void load() {
        try {
            Request req = new Request(RequestType.GET_EXPENSE_CATS, "");
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();
            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                List<ExpenseCategory> items = GsonUtil.getGson().fromJson(resp.getData(), new com.google.gson.reflect.TypeToken<List<ExpenseCategory>>(){}.getType());
                list.clear();
                if (items != null) list.addAll(items);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleAdd(javafx.event.ActionEvent e) {
        if (txtName.getText().isEmpty()) { showMessage("Введите название!", true); return; }
        try {
            ExpenseCategory cat = new ExpenseCategory(txtName.getText(), chkDeductible.isSelected());
            Request req = new Request(RequestType.ADD_EXPENSE_CAT, GsonUtil.getGson().toJson(cat));
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();
            if (resp != null && resp.getStatus() == ResponseStatus.OK) { clearForm(); load(); }
            else showMessage("Ошибка: " + (resp != null ? resp.getMessage() : "Нет ответа"), true);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML void handleUpdate(javafx.event.ActionEvent e) {
        if (currentEditing == null || txtName.getText().isEmpty()) return;
        try {
            currentEditing.setName(txtName.getText());
            currentEditing.setDeductible(chkDeductible.isSelected());
            Request req = new Request(RequestType.UPDATE_EXPENSE_CAT, GsonUtil.getGson().toJson(currentEditing));
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();
            if (resp != null && resp.getStatus() == ResponseStatus.OK) { clearForm(); load(); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleEdit(ExpenseCategory item) {
        currentEditing = item;
        txtName.setText(item.getName());
        chkDeductible.setSelected(item.isDeductible());
        lblFormTitle.setText("Редактировать #" + item.getId());
        btnAdd.setVisible(false); btnUpdate.setVisible(true); btnCancel.setVisible(true);
    }

    private void handleDelete(ExpenseCategory item) {
        new Alert(Alert.AlertType.CONFIRMATION, "Удалить статью " + item.getName() + "?", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        try {
                            Request req = new Request(RequestType.DELETE_EXPENSE_CAT, GsonUtil.getGson().toJson(new DeleteRequestDTO(item.getId())));
                            ClientSocket.getInstance().sendRequest(req);
                            if (ClientSocket.getInstance().readResponse().getStatus() == ResponseStatus.OK) load();
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                });
    }

    @FXML void handleCancel(javafx.event.ActionEvent e) { clearForm(); }
    @FXML void handleBack(javafx.event.ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/references.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Справочники");
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void clearForm() {
        txtName.clear(); chkDeductible.setSelected(true); currentEditing = null;
        lblFormTitle.setText("Добавить статью расхода");
        btnAdd.setVisible(true); btnUpdate.setVisible(false); btnCancel.setVisible(false);
    }

    private void showMessage(String msg, boolean err) {
        lblMessage.setText(msg); lblMessage.setTextFill(err ? Color.RED : Color.GREEN); lblMessage.setVisible(true);
    }
}