package by.bsuir.controllers;

import by.bsuir.App;
import by.bsuir.events.*;
import by.bsuir.messages.PauseMessage;
import by.bsuir.messages.TextMessage;
import by.bsuir.servers.RoomServer;
import by.bsuir.servers.StreamerServer;
import by.bsuir.servers.ViewerServer;
import by.bsuir.servers.api.ChatApi;
import by.bsuir.servers.api.PlayerApi;
import by.bsuir.servers.api.RoomApi;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomController extends Controller implements Initializable {
    private final RoomServer server;
    private final InetAddress inetAddress;
    private final String name;
    private final InetAddress streamerInetAddress;
    private final boolean isStreamer;

    private final RoomApi roomApi;
    private final ChatApi chatApi;
    private final PlayerApi playerApi;

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;

    @FXML
    private StackPane playerStackPane;
    @FXML
    private ImageView videoImageView;

    @FXML
    private VBox messagesVBox;
    @FXML
    private TextArea messageTextArea;

    @FXML
    private Label onlineLabel;

    @FXML
    private Button disconnectButton;

    public RoomController(StreamerServer server, InetAddress inetAddress, String name, String path) {
        this(server, inetAddress, name, inetAddress, true);
        // Запуск плеера
        embeddedMediaPlayer.media().play(
                path,
                ":sout=#duplicate{dst=rtp{sdp=rtsp://:53129/},dst=display}",
                ":no-sout-rtp-sap",
                ":no-sout-standard-sap",
                ":sout-all",
                ":sout-keep"
        );
    }

    public RoomController(ViewerServer server, InetAddress inetAddress, String name, InetAddress streamerInetAddress) {
        this(server, inetAddress, name, streamerInetAddress, false);
        // Запуск плеера
        embeddedMediaPlayer.media().play("rtsp://" + streamerInetAddress.getHostAddress() + ":53129/");
    }

    private RoomController(RoomServer server, InetAddress inetAddress, String name,
                           InetAddress streamerInetAddress, boolean isStreamer) {
        super("/fxml/RoomView.fxml");
        this.server = server;
        this.inetAddress = inetAddress;
        this.name = name;
        this.streamerInetAddress = streamerInetAddress;
        this.isStreamer = isStreamer;
        // Привязка к RoomAPI
        roomApi = server.getRoomApi();
        roomApi.setOnViewerConnected(this::viewerConnectedHandler);
        roomApi.setOnViewerDisconnected(this::viewerDisconnectedHandler);
        roomApi.setOnRoomClosed(this::roomClosedHandler);
        // Привязка к ChatAPI
        chatApi = server.getChatApi();
        chatApi.setOnTextMessageReceived(this::messageReceivedHandler);
        // Привязка к PlayerAPI
        playerApi = server.getPlayerApi();
        playerApi.setOnPlayerPaused(this::playerPausedHandler);
        // Запуск сервера
        server.start();
    }

    private void viewerConnectedHandler(ConnectEvent event) {
        Platform.runLater(() -> updateOnlineLabel(roomApi.getOnline()));
    }

    private void viewerDisconnectedHandler(DisconnectEvent event) {
        Platform.runLater(() -> updateOnlineLabel(roomApi.getOnline()));
    }

    private void updateOnlineLabel(int online) {
        onlineLabel.setText("Online: " + online);
    }

    private void roomClosedHandler(RoomCloseEvent event) {
        // Освобождение ресурсов плеера
        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
        // Переход в главное меню
        Controller controller = new MainMenuController(inetAddress, name);
        App.getPrimaryScene().setRoot(controller.getRoot());
    }

    private void messageReceivedHandler(TextEvent event) {
        Platform.runLater(() -> displayTextMessage(event.getTextMessage()));
    }

    private void displayTextMessage(TextMessage textMessage) {
        String name = textMessage.getName();
        String hostAddress = textMessage.getAddress().getHostAddress();
        String text = textMessage.getText();
        // Создание компонента
        Label label = new Label(name + "(" + hostAddress + "): " + text);
        label.setWrapText(true);
        label.prefWidthProperty().bind(messagesVBox.widthProperty());
        // Добавление компонента
        messagesVBox.getChildren().add(label);
    }

    private void playerPausedHandler(PauseEvent event) {
        if (isStreamer) {
            embeddedMediaPlayer.controls().pause();
        } else if (embeddedMediaPlayer.status().state() == State.PLAYING) {
            embeddedMediaPlayer.controls().stop();
        } else {
            embeddedMediaPlayer.media().start("rtsp://" + streamerInetAddress.getHostAddress() + ":53129/");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Создание плеера
        mediaPlayerFactory = new MediaPlayerFactory();
        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        // Настройка поверхности для показа видео
        videoImageView.fitHeightProperty().bind(playerStackPane.heightProperty());
        videoImageView.fitWidthProperty().bind(playerStackPane.widthProperty());
        embeddedMediaPlayer.videoSurface().set(ImageViewVideoSurfaceFactory.videoSurfaceForImageView(videoImageView));
        // Установка обработчиков
        videoImageView.setOnMousePressed(this::videoImageViewMousePressedHandler);
        videoImageView.setOnKeyPressed(this::videoImageViewKeyPressedHandler);
        messageTextArea.setOnKeyPressed(this::messageTextAreaKeyPressedHandler);
        disconnectButton.setOnAction(this::disconnectButtonActionHandler);
    }

    private void videoImageViewMousePressedHandler(MouseEvent event) {
        videoImageView.requestFocus();
    }

    private void videoImageViewKeyPressedHandler(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            pausePlayer();
            event.consume();
        }
    }

    private void pausePlayer() {
        // Создание сообщения
        PauseMessage pauseMessage = new PauseMessage(inetAddress);
        // Обновление состояние сети
        playerApi.pause(pauseMessage);
        // Пауза
        if (isStreamer) {
            embeddedMediaPlayer.controls().pause();
        } else if (embeddedMediaPlayer.status().state() == State.PLAYING) {
            embeddedMediaPlayer.controls().stop();
        } else {
            embeddedMediaPlayer.media().start("rtsp://" + streamerInetAddress.getHostAddress() + ":53129/");
        }
    }

    private void messageTextAreaKeyPressedHandler(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                messageTextArea.appendText("\n");
            } else {
                String text = messageTextArea.getText();
                sendTextMessage(text);
            }
            // Уничтожение события
            event.consume();
        }
    }

    private void sendTextMessage(String text) {
        if (!text.isBlank()) {
            // Создание сообщения
            TextMessage textMessage = new TextMessage(inetAddress, name, text);
            // Отправка сообщения
            chatApi.sendTextMessage(textMessage);
            // Отображение сообщения
            displayTextMessage(textMessage);
        }
        messageTextArea.clear();
    }

    private void disconnectButtonActionHandler(ActionEvent event) {
        // Остановка сервера
        server.stop();
        // Освобождение ресурсов плеера
        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
        // Переход в главное меню
        Controller controller = new MainMenuController(inetAddress, name);
        App.getPrimaryScene().setRoot(controller.getRoot());
    }
}
