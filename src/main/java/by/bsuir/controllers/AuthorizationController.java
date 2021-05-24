package by.bsuir.controllers;

import by.bsuir.App;
import by.bsuir.servers.ExplorerServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class AuthorizationController extends Controller implements Initializable {

    @FXML
    private ChoiceBox<String> networkInterfaceIpChoiceBox;
    @FXML
    private TextField nameTextField;

    @FXML
    private Button connectButton;

    public AuthorizationController() {
        super("/fxml/AuthorizationView.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        networkInterfaceIpChoiceBox.getItems().addAll(getNetworkInterfaceIpList());
        networkInterfaceIpChoiceBox.setValue(networkInterfaceIpChoiceBox.getItems().get(0));
        connectButton.setOnAction(this::connectButtonActionHandler);
    }

    private void connectButtonActionHandler(ActionEvent event) {
        // Проверка ввода
        if (isInputValid()) {
            // Получение ввода
            String ip = networkInterfaceIpChoiceBox.getValue();
            String name = nameTextField.getText();
            try {
                // Переход в главное меню
                Controller controller = new MainMenuController(InetAddress.getByName(ip), name);
                App.getPrimaryScene().setRoot(controller.getRoot());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isInputValid() {
        // Проверка имени
        String name = nameTextField.getText();
        if (name.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Enter some name!");
            alert.showAndWait();
            return false;
        }
        // Всё верно
        return true;
    }

    private List<String> getNetworkInterfaceIpList() {
        List<String> list = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            list.add(inetAddress.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
