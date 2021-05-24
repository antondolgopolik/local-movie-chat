package by.bsuir.controllers;

import by.bsuir.App;
import by.bsuir.servers.StreamerServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomCreatorController extends Controller implements Initializable {
    private final InetAddress inetAddress;
    private final String name;

    @FXML
    private TextField mediaTextField;

    @FXML
    private TextField pathTextField;
    @FXML
    private Button chooseButton;

    @FXML
    private Button getBackButton;
    @FXML
    private Button createButton;

    public RoomCreatorController(InetAddress inetAddress, String name) {
        super("/fxml/RoomCreatorView.fxml");
        this.inetAddress = inetAddress;
        this.name = name;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chooseButton.setOnAction(this::chooseButtonActionHandler);
        getBackButton.setOnAction(this::getBackButtonActionHandler);
        createButton.setOnAction(this::createButtonActionHandler);
    }

    private void chooseButtonActionHandler(ActionEvent event) {
        // Выбор файла
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(App.getPrimaryWindow());
        // Если файл был выбран
        if (file != null) {
            pathTextField.setText(file.getAbsolutePath());
        }
    }

    private void getBackButtonActionHandler(ActionEvent event) {
        // Переход в главное меню
        Controller controller = new MainMenuController(inetAddress, name);
        App.getPrimaryScene().setRoot(controller.getRoot());
    }

    private void createButtonActionHandler(ActionEvent event) {
        if (isInputValid()) {
            // Получение ввода
            String media = mediaTextField.getText();
            String path = pathTextField.getText();
            // Создание сервера
            StreamerServer streamerServer = new StreamerServer(inetAddress, name, media);
            // Переход в комнату
            Controller controller = new RoomController(streamerServer, inetAddress, name, path);
            App.getPrimaryScene().setRoot(controller.getRoot());
        }
    }

    private boolean isInputValid() {
        // Проверка медиа
        String media = mediaTextField.getText();
        if (media.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Enter some media title!");
            alert.showAndWait();
            return false;
        }
        // Проверка пути
        String path = pathTextField.getText();
        if (!isPathValid(path)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Path to media is invalid!");
            alert.showAndWait();
            return false;
        }
        // Всё верно
        return true;
    }

    private boolean isPathValid(String path) {
        try (var inputStream = new FileInputStream(path)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
