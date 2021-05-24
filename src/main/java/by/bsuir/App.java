package by.bsuir;

import by.bsuir.controllers.AuthorizationController;
import by.bsuir.controllers.Controller;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryWindow;
    private static Scene primaryScene;

    public static Stage getPrimaryWindow() {
        return primaryWindow;
    }

    public static Scene getPrimaryScene() {
        return primaryScene;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Controller controller = new AuthorizationController();
        primaryScene = new Scene(controller.getRoot());
        primaryWindow = stage;
        // Настройка и отображение главного окна
        primaryWindow.setScene(primaryScene);
        primaryWindow.setMaximized(true);
        primaryWindow.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
