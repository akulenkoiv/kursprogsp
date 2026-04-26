package client.controllers;

import client.ClientApp;
import client.utility.ClientSocket;
import enums.RequestType;
import enums.ResponseStatus;
import enums.Roles;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import models.dto.DeleteRequestDTO;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UsersController implements Initializable {
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colLogin;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private Label lblMessage;
    @FXML private Button btnAdd;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @SuppressWarnings("rawtypes")
    private Task currentTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(getRoleName(cellData.getValue().getRoleId())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        cbRole.getItems().addAll("ИП", "Бухгалтер");
        cbRole.getSelectionModel().selectFirst();

        setupActionColumn();
        tableUsers.setItems(userList);
        loadUsers();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button btnDelete = new Button("🗑️");
            {
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    private void cancelCurrentTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
        }
    }

    private void loadUsers() {
        cancelCurrentTask();
        showMessage("Загрузка...", false);

        currentTask = new Task<List<User>>() {
            @Override
            protected List<User> call() throws Exception {
                Request req = new Request(RequestType.GET_USERS, "");
                Response resp = ClientSocket.getInstance().sendRequestSync(req);
                if (resp != null && resp.getStatus() == ResponseStatus.OK && resp.getData() != null) {
                    return GsonUtil.getGson().fromJson(resp.getData(), new com.google.gson.reflect.TypeToken<List<User>>(){}.getType());
                }
                return null;
            }
        };

        currentTask.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<User> items = (List<User>) currentTask.getValue();
            userList.clear();
            if (items != null) {
                userList.addAll(items);
                showMessage("", false);
            } else {
                showMessage("Нет данных", true);
            }
        });

        currentTask.setOnFailed(e -> showMessage("Ошибка загрузки", true));

        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void handleAddUser(javafx.event.ActionEvent event) {
        if (txtLogin.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            showMessage("Заполните логин и пароль!", true);
            return;
        }

        cancelCurrentTask();
        if (btnAdd != null) btnAdd.setDisable(true);
        showMessage("Добавление...", false);

        currentTask = new Task<Response>() {
            @Override
            protected Response call() throws Exception {
                User newUser = new User();
                newUser.setLogin(txtLogin.getText());
                newUser.setPasswordHash(txtPassword.getText());

                String selectedRole = cbRole.getValue();
                newUser.setRoleId("Бухгалтер".equals(selectedRole) ? Roles.ACCOUNTANT.getLevel() : Roles.ENTREPRENEUR.getLevel());

                String json = GsonUtil.getGson().toJson(newUser);
                Request req = new Request(RequestType.ADD_USER, json);
                return ClientSocket.getInstance().sendRequestSync(req);
            }
        };

        currentTask.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            Response resp = (Response) currentTask.getValue();
            if (resp == null) {
                showMessage("Ошибка: сервер не ответил", true);
            } else if (resp.getStatus() == ResponseStatus.OK) {
                showMessage("Пользователь успешно добавлен!", false);
                txtLogin.clear();
                txtPassword.clear();
                loadUsers();
            } else {
                showMessage("Ошибка: " + resp.getMessage(), true);
            }
            if (btnAdd != null) btnAdd.setDisable(false);
        });

        currentTask.setOnFailed(e -> {
            showMessage("Ошибка сети", true);
            if (btnAdd != null) btnAdd.setDisable(false);
        });

        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void handleDelete(User user) {
        new Alert(Alert.AlertType.CONFIRMATION, "Удалить пользователя " + user.getLogin() + "?", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        cancelCurrentTask();
                        showMessage("Удаление...", false);

                        currentTask = new Task<Response>() {
                            @Override
                            protected Response call() throws Exception {
                                Request req = new Request(RequestType.DELETE_USER, GsonUtil.getGson().toJson(new DeleteRequestDTO(user.getId())));
                                return ClientSocket.getInstance().sendRequestSync(req);
                            }
                        };

                        currentTask.setOnSucceeded(e -> {
                            @SuppressWarnings("unchecked")
                            Response resp = (Response) currentTask.getValue();
                            if (resp != null && resp.getStatus() == ResponseStatus.OK) {
                                showMessage("Пользователь удален", false);
                                loadUsers();
                            } else {
                                showMessage("Ошибка: " + (resp != null ? resp.getMessage() : "Нет ответа"), true);
                            }
                        });

                        currentTask.setOnFailed(e -> showMessage("Ошибка сети при удалении", true));

                        Thread thread = new Thread(currentTask);
                        thread.setDaemon(true);
                        thread.start();
                    }
                });
    }

    @FXML
    void handleRefresh(javafx.event.ActionEvent event) {
        loadUsers();
    }

    @FXML
    void handleBack(javafx.event.ActionEvent event) {
        cancelCurrentTask();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            ClientApp.getPrimaryStage().setScene(new Scene(root));
            ClientApp.getPrimaryStage().setTitle("Главное меню");
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Ошибка навигации", true);
        }
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 3: return "Администратор";
            case 2: return "ИП";
            case 1: return "Бухгалтер";
            default: return "Неизвестно";
        }
    }

    private void showMessage(String msg, boolean err) {
        lblMessage.setText(msg);
        lblMessage.setTextFill(err ? Color.RED : Color.GREEN);
        lblMessage.setVisible(!msg.isEmpty());
    }
}