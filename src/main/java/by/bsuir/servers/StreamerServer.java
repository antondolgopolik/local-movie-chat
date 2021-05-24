package by.bsuir.servers;

import by.bsuir.events.MulticastEvent;
import by.bsuir.events.SocketEvent;
import by.bsuir.messages.RoomCloseMessage;
import by.bsuir.messages.RoomInfoMessage;
import by.bsuir.servers.services.ConnectionAcceptingService;
import by.bsuir.servers.services.ConnectionHandlingService;
import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class StreamerServer extends RoomServer {
    public static final int CONNECTION_SERVICE_PORT = 53127;

    public static final InetAddress MULTICAST_SERVICE_GROUP;
    public static final int MULTICAST_SERVICE_PORT = 53128;

    private final MulticastService networkStateMulticastService;

    private final String name;
    private final String media;

    static {
        try {
            MULTICAST_SERVICE_GROUP = InetAddress.getByName("224.0.1.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public StreamerServer(InetAddress inetAddress, String name, String media) {
        super(inetAddress, inetAddress);
        this.name = name;
        this.media = media;
        roomMulticastService = new MulticastService(
                inetAddress, ViewerServer.MULTICAST_SERVICE_GROUP, ViewerServer.MULTICAST_SERVICE_PORT,
                this::serveRoomMulticast
        );
        networkStateMulticastService = new MulticastService(
                inetAddress, MULTICAST_SERVICE_GROUP, MULTICAST_SERVICE_PORT, this::serveNetworkStateMulticast
        );
    }

    @Override
    public void start() {
        // Создание сервиса
        ConnectionAcceptingService connectionAcceptingService = new ConnectionAcceptingService(
                inetAddress, CONNECTION_SERVICE_PORT, this::roomOpenServeConnection
        );
        // Начать приём TCP-соединений от исследователей
        connectionAcceptingService.startAcceptingConnections();
        // Отправить multicast исследователям
        MulticastService.sendMulticast(
                inetAddress, ExplorerServer.MULTICAST_SERVICE_GROUP, ExplorerServer.MULTICAST_SERVICE_PORT
        );
        // Подождать приёма всех соединений
        connectionAcceptingService.waitForConnectionsToBeAccepted();
        // Начать получение multicast'ов
        roomMulticastService.startReceivingMulticasts();
        networkStateMulticastService.startReceivingMulticasts();
    }

    /*
    Обслуживает принятое TCP-соединение от исследователя при открытии комнаты
     */
    private void roomOpenServeConnection(SocketEvent event) {
        try {
            Socket socket = event.getSocket();
            // Открытие потока записи
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Отправка сообщения
            RoomInfoMessage roomInfoMessage = new RoomInfoMessage(inetAddress, name, media, online.get());
            outputStream.writeObject(roomInfoMessage);
            // Закрытие соединения
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Обслуживает полученный от исследователя multicast
     */
    private void serveNetworkStateMulticast(MulticastEvent event) {
        try {
            // Установка соединения
            InetAddress inetAddress = event.getInetAddress();
            Socket socket = new Socket(inetAddress, ExplorerServer.CONNECTION_SERVICE_PORT);
            // Открытие потока записи
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Отправка сообщения
            RoomInfoMessage roomInfoMessage = new RoomInfoMessage(this.inetAddress, name, media, online.get());
            outputStream.writeObject(roomInfoMessage);
            // Закрытие соединения
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Остановить получение multicast'ов
        roomMulticastService.stopReceivingMulticasts();
        networkStateMulticastService.stopReceivingMulticasts();
        // Создание сервиса
        ConnectionAcceptingService connectionAcceptingService = new ConnectionAcceptingService(
                inetAddress, CONNECTION_SERVICE_PORT, this::roomCloseServeConnection
        );
        // Начать приём TCP-соединений от исследователей
        connectionAcceptingService.startAcceptingConnections();
        // Отправить multicast исследователям
        MulticastService.sendMulticast(
                inetAddress, ExplorerServer.MULTICAST_SERVICE_GROUP, ExplorerServer.MULTICAST_SERVICE_PORT
        );
        // Подождать приёма всех соединений
        connectionAcceptingService.waitForConnectionsToBeAccepted();
        // Закрыть TCP-соединения со зрителями
        connectionHandlingService.stop(this::disconnect);
    }

    /*
    Обслуживает принятое TCP-соединение от исследователя при закрытии комнаты
     */
    private void roomCloseServeConnection(SocketEvent event) {
        try {
            Socket socket = event.getSocket();
            // Открытие потока записи
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Отправка сообщения
            RoomCloseMessage roomCloseMessage = new RoomCloseMessage(inetAddress);
            outputStream.writeObject(roomCloseMessage);
            // Закрытие соединения
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect(ConnectionHandlingService.ConnectionHandler connectionHandler) {
        try {
            // Получение потока записи
            ObjectOutputStream outputStream = connectionHandler.getOutputStream();
            // Отправка RoomCloseMessage
            RoomCloseMessage roomCloseMessage = new RoomCloseMessage(inetAddress);
            outputStream.writeObject(roomCloseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
