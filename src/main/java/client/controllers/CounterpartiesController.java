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
import models.entities.Counterparty;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CounterpartiesController implements Initializable {
    @FXML private TableView<Counterparty> table;
    @FXML private TableColumn<Counterparty, Integer> colId;
    @FXML private TableColumn<Counterparty, String> colName;
    @FXML private TableColumn<Counterparty, String> colInn;
    @FXML private TableColumn<Counterparty, String> colContact;
    @FXML private TableColumn<Counterparty, Void> colActions;

    @FXML private TextField txtName;
    @FXML private TextField txtInn;
    @FXML private TextField txtContact;
    @FXML private Label lblFormTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private ObservableList<Counterparty> list = FXCollections.observableArrayList();
    private Counterparty currentEditing = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colInn.setCellValueFactory(new PropertyValueFactory<>("inn"));
        colContact.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getContactInfo()));
        setupActionColumn();
        table.setItems(list);
        load();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<Counterparty, Void>() {
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
            Request req = new Request(RequestType.GET_COUNTERPARTIES, "");
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();
            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                List<Counterparty> items = GsonUtil.getGson().fromJson(resp.getData(), new com.google.gson.reflect.TypeToken<List<Counterparty>>(){}.getType());
                list.clear();
                if (items != null) list.addAll(items);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleAdd(javafx.event.ActionEvent e) {
        if (txtName.getText().isEmpty()) { showMessage("Введите наименование!", true); return; }
        try {
            Counterparty cp = new Counterparty();
            cp.setName(txtName.getText());
            cp.setInn(txtInn.getText().isEmpty() ? null : txtInn.getText());
            cp.setContactInfo(txtContact.getText().isEmpty() ? null : txtContact.getText());
            Request req = new Request(RequestType.ADD_COUNTERPARTY, GsonUtil.getGson().toJson(cp));
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
            currentEditing.setInn(txtInn.getText().isEmpty() ? null : txtInn.getText());
            currentEditing.setContactInfo(txtContact.getText().isEmpty() ? null : txtContact.getText());
            Request req = new Request(RequestType.UPDATE_COUNTERPARTY, GsonUtil.getGson().toJson(currentEditing));
            ClientSocket.getInstance().sendRequest(req);
            Response resp = ClientSocket.getInstance().readResponse();
            if (resp != null && resp.getStatus() == ResponseStatus.OK) { clearForm(); load(); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleEdit(Counterparty item) {
        currentEditing = item;
        txtName.setText(item.getName());
        txtInn.setText(item.getInn() != null ? item.getInn() : "");
        txtContact.setText(item.getContactInfo() != null ? item.getContactInfo() : "");
        lblFormTitle.setText("Редактировать #" + item.getId());
        btnAdd.setVisible(false); btnUpdate.setVisible(true); btnCancel.setVisible(true);
    }

    private void handleDelete(Counterparty item) {
        new Alert(Alert.AlertType.CONFIRMATION, "Удалить контрагента " + item.getName() + "?", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        try {
                            Request req = new Request(RequestType.DELETE_COUNTERPARTY, GsonUtil.getGson().toJson(new DeleteRequestDTO(item.getId())));
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
        txtName.clear(); txtInn.clear(); txtContact.clear(); currentEditing = null;
        lblFormTitle.setText("Добавить контрагента");
        btnAdd.setVisible(true); btnUpdate.setVisible(false); btnCancel.setVisible(false);
    }

    private void showMessage(String msg, boolean err) {
        lblMessage.setText(msg); lblMessage.setTextFill(err ? Color.RED : Color.GREEN); lblMessage.setVisible(true);
    }
}