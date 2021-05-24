package by.bsuir.servers;

import by.bsuir.events.ConnectEvent;
import by.bsuir.events.SocketEvent;
import by.bsuir.messages.DisconnectMessage;
import by.bsuir.messages.StatusMessage;
import by.bsuir.messages.ViewerInfoMessage;
import by.bsuir.servers.services.ConnectionAcceptingService;
import by.bsuir.servers.services.ConnectionHandlingService;
import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ViewerServer extends RoomServer {
    public static final int CONNECTION_SERVICE_PORT = 53125;

    public static final InetAddress MULTICAST_SERVICE_GROUP;
    public static final int MULTICAST_SERVICE_PORT = 53126;

    static {
        try {
            MULTICAST_SERVICE_GROUP = InetAddress.getByName("224.0.1.3");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public ViewerServer(InetAddress inetAddress, InetAddress streamerInetAddress) {
        super(inetAddress, streamerInetAddress);
    }

    @Override
    public void start() {
        // Создание сервиса
        ConnectionAcceptingService connectionAcceptingService = new ConnectionAcceptingService(
                inetAddress, CONNECTION_SERVICE_PORT, this::serveConnection
        );
        // Начать приём TCP-соединений от других зрителей
        connectionAcceptingService.startAcceptingConnections();
        // Отправить multicast другим зрителям
        MulticastService.sendMulticast(
                inetAddress, MULTICAST_SERVICE_GROUP, MULTICAST_SERVICE_PORT
        );
        // Подождать приёма всех соединений
        connectionAcceptingService.waitForConnectionsToBeAccepted();
        // Именно здесь создать сервис multicast'ов, чтобы избежать получения multicast'a от самого себя
        roomMulticastService = new MulticastService(
                inetAddress, MULTICAST_SERVICE_GROUP, MULTICAST_SERVICE_PORT, this::serveRoomMulticast
        );
        // Начать получение multicast'ов
        roomMulticastService.startReceivingMulticasts();
    }

    /*
    Обслуживает принятое TCP-соединение от другого зрителя
     */
    private void serveConnection(SocketEvent event) {
        try {
            Socket socket = event.getSocket();
            // Открытие потока чтения
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Чтение сообщения
            ViewerInfoMessage viewerInfoMessage = (ViewerInfoMessage) inputStream.readObject();
            // Проверка вещателя
            InetAddress streamerInetAddress = viewerInfoMessage.getStreamerInetAddress();
            if (this.streamerInetAddress.equals(streamerInetAddress)) {
                // Открытие потока записи
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                // Отправка собщения
                StatusMessage statusMessage = new StatusMessage(inetAddress, StatusMessage.StatusMessageType.OK);
                outputStream.writeObject(statusMessage);
                outputStream.flush();
                // Сохранение соединения
                connectionHandlingService.addConnection(socket);
                // Увеличить online
                online.incrementAndGet();
                // Регистрация события
                roomApi.getOnViewerConnected().handle(new ConnectEvent());
            } else {
                // Открытие потока записи
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                // Отправка собщения
                StatusMessage statusMessage = new StatusMessage(inetAddress, StatusMessage.StatusMessageType.WRONG);
                outputStream.writeObject(statusMessage);
                // Закрытие соединения
                socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Остановить получение multicast'ов
        roomMulticastService.stopReceivingMulticasts();
        // Закрыть TCP-соединения с другими зрителями
        connectionHandlingService.stop(this::disconnect);
    }

    private void disconnect(ConnectionHandlingService.ConnectionHandler connectionHandler) {
        try {
            // Получение потока записи
            ObjectOutputStream outputStream = connectionHandler.getOutputStream();
            // Отправка DisconnectMessage
            DisconnectMessage disconnectMessage = new DisconnectMessage(inetAddress);
            outputStream.writeObject(disconnectMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}