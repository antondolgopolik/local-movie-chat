package by.bsuir.controllers;

import by.bsuir.App;
import by.bsuir.events.RoomCloseEvent;
import by.bsuir.events.RoomInfoEvent;
import by.bsuir.messages.RoomInfoMessage;
import by.bsuir.servers.ExplorerServer;
import by.bsuir.servers.ViewerServer;
import by.bsuir.servers.api.NetworkStateApi;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainMenuController extends Controller implements Initializable {
    private final InetAddress inetAddress;
    private final String name;

    private final ExplorerServer server;
    private final NetworkStateApi networkStateApi;

    @FXML
    private TableView<RoomInfoMessage> roomTableView;

    @FXML
    private TableColumn<RoomInfoMessage, String> ownerTableColumn;
    @FXML
    private TableColumn<RoomInfoMessage, String> mediaTableColumn;
    @FXML
    private TableColumn<RoomInfoMessage, Number> onlineTableColumn;

    @FXML
    private Button updateButton;
    @FXML
    private Button joinButton;
    @FXML
    private Button createNewButton;

    public MainMenuController(InetAddress inetAddress, String name) {
        super("/fxml/MainMenuView.fxml");
        this.inetAddress = inetAddress;
        this.name = name;
        // Создание сервера
        server = new ExplorerServer(inetAddress);
        // Привязка к RoomAPI
        networkStateApi = server.getNetworkStateApi();
        networkStateApi.setOnRoomOpened(this::roomOpenedHandler);
        networkStateApi.setOnRoomClosed(this::roomClosedHandler);
        // Запуск сервера
        server.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roomTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ownerTableColumn.setCellValueFactory(this::ownerCellValueFactory);
        mediaTableColumn.setCellValueFactory(this::mediaCellValueFactory);
        onlineTableColumn.setCellValueFactory(this::onlineCellValueFactory);
        joinButton.disableProperty().bind(roomTableView.getSelectionModel().selectedItemProperty().isNull());
        updateButton.setOnAction(this::updateButtonActionHandler);
        joinButton.setOnAction(this::joinButtonActionHandler);
        createNewButton.setOnAction(this::createNewButtonActionHandler);
    }

    private ObservableValue<String> ownerCellValueFactory(TableColumn.CellDataFeatures<RoomInfoMessage, String> m) {
        RoomInfoMessage value = m.getValue();
        String s = value.getName() + "(" + value.getAddress().getHostAddress() + ")";
        return new ReadOnlyStringWrapper(s);
    }

    private ObservableValue<String> mediaCellValueFactory(TableColumn.CellDataFeatures<RoomInfoMessage, String> m) {
        return new ReadOnlyStringWrapper(m.getValue().getMedia());
    }

    private ObservableValue<Number> onlineCellValueFactory(TableColumn.CellDataFeatures<RoomInfoMessage, Number> m) {
        return new ReadOnlyIntegerWrapper(m.getValue().getOnline());
    }

    private void updateButtonActionHandler(ActionEvent event) {
        // Обновление UI
        roomTableView.getItems().clear();
        updateButton.setDisable(true);
        // Создание задания запросить обновление сведений о доступных комнатах
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                networkStateApi.requestUpdateForAvailableRooms();
                return null;
            }
        };
        task.stateProperty().addListener((observableValue, s1, s2) -> {
            if (task.isDone()) {
                updateButton.setDisable(false);
            }
        });
        // Начать выполнение
        Thread thread = new Thread(task);
        thread.start();
    }

    private void joinButtonActionHandler(ActionEvent event) {
        // Остановка сервера
        server.stop();
        // Создание сервера
        RoomInfoMessage selectedItem = roomTableView.getSelectionModel().getSelectedItem();
        InetAddress streamerInetAddress = selectedItem.getAddress();
        ViewerServer viewerServer = new ViewerServer(inetAddress, streamerInetAddress);
        // Переход в комнату
        Controller controller = new RoomController(viewerServer, inetAddress, name, streamerInetAddress);
        App.getPrimaryScene().setRoot(controller.getRoot());
    }

    private void createNewButtonActionHandler(ActionEvent event) {
        // Остановка сервера
        server.stop();
        // Переход в менб создания комнаты
        Controller controller = new RoomCreatorController(inetAddress, name);
        App.getPrimaryScene().setRoot(controller.getRoot());
    }

    private void roomOpenedHandler(RoomInfoEvent event) {
        Platform.runLater(() -> addRoomToTable(event.getRoomInfoMessage()));
    }

    private void roomClosedHandler(RoomCloseEvent event) {
        Platform.runLater(() -> removeRoomFromTable(event.getRoomCloseMessage().getAddress()));
    }

    private void addRoomToTable(RoomInfoMessage message) {
        roomTableView.getItems().add(message);
    }

    private void removeRoomFromTable(InetAddress inetAddress) {
        roomTableView.getItems().removeIf(roomInfoMessage -> inetAddress.equals(roomInfoMessage.getAddress()));
    }
}
