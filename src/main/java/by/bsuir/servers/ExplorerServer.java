package by.bsuir.servers;

import by.bsuir.events.MulticastEvent;
import by.bsuir.events.RoomCloseEvent;
import by.bsuir.events.RoomInfoEvent;
import by.bsuir.events.SocketEvent;
import by.bsuir.messages.Message;
import by.bsuir.messages.RoomCloseMessage;
import by.bsuir.messages.RoomInfoMessage;
import by.bsuir.servers.api.NetworkStateApi;
import by.bsuir.servers.services.ConnectionAcceptingService;
import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ExplorerServer extends Server implements NetworkStateApi.NetworkStateApiProvider {
    public static final int CONNECTION_SERVICE_PORT = 53123;

    public static final InetAddress MULTICAST_SERVICE_GROUP;
    public static final int MULTICAST_SERVICE_PORT = 53124;

    private final MulticastService multicastService;

    private final NetworkStateApi networkStateApi = new NetworkStateApi(this);

    static {
        try {
            MULTICAST_SERVICE_GROUP = InetAddress.getByName("224.0.1.2");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public ExplorerServer(InetAddress inetAddress) {
        super(inetAddress);
        multicastService = new MulticastService(
                inetAddress, MULTICAST_SERVICE_GROUP, MULTICAST_SERVICE_PORT, this::serveMulticast
        );
    }

    @Override
    public void start() {
        requestUpdateForAvailableRooms();
        // Начать получение multicast'ов
        multicastService.startReceivingMulticasts();
    }

    /*
    Обслуживает принятое TCP-соединение от вещателя
     */
    private void serveConnection(SocketEvent event) {
        try {
            Socket socket = event.getSocket();
            // Открытие потока чтения
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Чтение сообщения
            RoomInfoMessage roomInfoMessage = (RoomInfoMessage) inputStream.readObject();
            // Закрытие соединения
            socket.close();
            // Обработка сообщения
            serveRoomInfoMessage(roomInfoMessage);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    Обслуживает полученный multicast
     */
    private void serveMulticast(MulticastEvent event) {
        try {
            // Установка соединения
            InetAddress inetAddress = event.getInetAddress();
            Socket socket = new Socket(inetAddress, StreamerServer.CONNECTION_SERVICE_PORT);
            // Открытие потока чтения
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Чтение сообщения
            Message message = (Message) inputStream.readObject();
            // Закрытие соединения
            socket.close();
            // Обработка сообщения
            switch (message.getMessageType()) {
                case ROOM_INFO_MESSAGE -> serveRoomInfoMessage((RoomInfoMessage) message);
                case ROOM_CLOSE_MESSAGE -> serveRoomCloseMessage((RoomCloseMessage) message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
    Обслуживает принятый RoomInfoMessage
     */
    private void serveRoomInfoMessage(RoomInfoMessage message) {
        networkStateApi.getOnRoomOpened().handle(new RoomInfoEvent(message));
    }

    /*
    Обслуживает принятый RoomCloseMessage
     */
    private void serveRoomCloseMessage(RoomCloseMessage message) {
        networkStateApi.getOnRoomClosed().handle(new RoomCloseEvent(message));
    }

    @Override
    public void stop() {
        // Остановить получение multicast'ов
        multicastService.stopReceivingMulticasts();
    }

    @Override
    public NetworkStateApi getNetworkStateApi() {
        return networkStateApi;
    }

    @Override
    public void requestUpdateForAvailableRooms() {
        // Создание сервиса
        ConnectionAcceptingService connectionAcceptingService = new ConnectionAcceptingService(
                inetAddress, CONNECTION_SERVICE_PORT, this::serveConnection
        );
        // Начать приём TCP-соединений от вещателей
        connectionAcceptingService.startAcceptingConnections();
        // Отправить multicast вещателям
        MulticastService.sendMulticast(
                inetAddress, StreamerServer.MULTICAST_SERVICE_GROUP, StreamerServer.MULTICAST_SERVICE_PORT
        );
        // Подождать приёма всех соединений
        connectionAcceptingService.waitForConnectionsToBeAccepted();
    }
}
