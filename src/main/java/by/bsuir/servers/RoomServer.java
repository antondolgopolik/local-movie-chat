package by.bsuir.servers;

import by.bsuir.events.*;
import by.bsuir.messages.*;
import by.bsuir.servers.api.ChatApi;
import by.bsuir.servers.api.PlayerApi;
import by.bsuir.servers.api.RoomApi;
import by.bsuir.servers.services.ConnectionHandlingService;
import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RoomServer extends Server implements RoomApi.RoomApiProvider, ChatApi.ChatApiProvider, PlayerApi.PlayerApiProvider {
    protected final ConnectionHandlingService connectionHandlingService;
    protected MulticastService roomMulticastService;

    protected final RoomApi roomApi = new RoomApi(this);
    protected final ChatApi chatApi = new ChatApi(this);
    protected final PlayerApi playerApi = new PlayerApi(this);

    protected final InetAddress streamerInetAddress;
    protected final AtomicInteger online = new AtomicInteger(1);

    public RoomServer(InetAddress inetAddress, InetAddress streamerInetAddress) {
        super(inetAddress);
        this.streamerInetAddress = streamerInetAddress;
        connectionHandlingService = new ConnectionHandlingService(
                this::handleConnection
        );
    }

    /*
    Обрабатывает сохранённое TCP-соединение
     */
    private void handleConnection(ConnectionHandlingService.ConnectionHandler connectionHandler) {
        try {
            // Получение потока чтения
            ObjectInputStream inputStream = connectionHandler.getInputStream();
            // Чтение сообщения
            Message message = (Message) inputStream.readObject();
            // Обработка сообщения
            switch (message.getMessageType()) {
                case TEXT_MESSAGE -> serveTextMessage(connectionHandler, (TextMessage) message);
                case PAUSE_MESSAGE -> servePauseMessage(connectionHandler, (PauseMessage) message);
                case DISCONNECT_MESSAGE -> serveDisconnectMessage(connectionHandler, (DisconnectMessage) message);
                case ROOM_CLOSE_MESSAGE -> serveRoomCloseMessage(connectionHandler, (RoomCloseMessage) message);
            }
        } catch (SocketException ignored) {
            // Соединение было закрыто
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    Обслуживает принятый TextMessage
     */
    private void serveTextMessage(ConnectionHandlingService.ConnectionHandler connectionHandler,
                                  TextMessage message) {
        chatApi.getOnTextMessageReceived().handle(new TextEvent(message));
    }

    /*
    Обслуживает принятый PlayerMessage
     */
    private void servePauseMessage(ConnectionHandlingService.ConnectionHandler connectionHandler,
                                   PauseMessage pauseMessage) {
        playerApi.getOnPlayerPaused().handle(new PauseEvent(pauseMessage));
    }

    /*
    Обслуживает принятый DisconnectMessage
     */
    private void serveDisconnectMessage(ConnectionHandlingService.ConnectionHandler connectionHandler,
                                        DisconnectMessage message) {
        try {
            // Закрыть TCP-соединение
            connectionHandler.stop();
            // Уменьшить online
            online.decrementAndGet();
            // Регистрация события
            roomApi.getOnViewerDisconnected().handle(new DisconnectEvent(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Обслуживает принятый RoomCloseMessage
     */
    private void serveRoomCloseMessage(ConnectionHandlingService.ConnectionHandler connectionHandler,
                                       RoomCloseMessage message) {
        // Остановить получение multicast'ов
        roomMulticastService.stopReceivingMulticasts();
        // Закрыть TCP-соединения с другими зрителями
        connectionHandlingService.stop();
        // Регистрация события
        roomApi.getOnRoomClosed().handle(new RoomCloseEvent(message));
    }

    /*
    Обслуживает полученный от зрителя multicast
     */
    protected void serveRoomMulticast(MulticastEvent event) {
        try {
            // Установка соединения
            InetAddress inetAddress = event.getInetAddress();
            Socket socket = new Socket(inetAddress, ViewerServer.CONNECTION_SERVICE_PORT);
            // Открытие потока записи
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Отправка сообщения
            ViewerInfoMessage viewerInfoMessage = new ViewerInfoMessage(inetAddress, streamerInetAddress);
            outputStream.writeObject(viewerInfoMessage);
            outputStream.flush();
            // Открытие потока чтения
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Чтение сообщения
            StatusMessage statusMessage = (StatusMessage) inputStream.readObject();
            // Обработка сообщения
            if (statusMessage.getStatusMessageType() == StatusMessage.StatusMessageType.OK) {
                // Сохранение соединения
                connectionHandlingService.addConnection(socket);
                // Увеличить online
                online.incrementAndGet();
                // Регистрация события
                roomApi.getOnViewerConnected().handle(new ConnectEvent());
            } else {
                // Закрытие соединения
                socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RoomApi getRoomApi() {
        return roomApi;
    }

    @Override
    public int getOnline() {
        return online.get();
    }

    @Override
    public ChatApi getChatApi() {
        return chatApi;
    }

    @Override
    public void sendTextMessage(TextMessage textMessage) {
        connectionHandlingService.handle(connectionHandler -> {
            try {
                // Получение потока записи
                ObjectOutputStream outputStream = connectionHandler.getOutputStream();
                // Отправка TextMessage
                outputStream.writeObject(textMessage);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public PlayerApi getPlayerApi() {
        return playerApi;
    }

    @Override
    public void pause(PauseMessage pauseMessage) {
        connectionHandlingService.handle(connectionHandler -> {
            try {
                // Получение потока записи
                ObjectOutputStream outputStream = connectionHandler.getOutputStream();
                // Отправка PlayerMessage
                outputStream.writeObject(pauseMessage);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
