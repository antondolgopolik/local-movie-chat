package by.bsuir.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class Controller {
    private Parent root;

    protected Controller(String pathToView) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(pathToView));
        fxmlLoader.setController(this);
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Parent getRoot() {
        return root;
    }
}
