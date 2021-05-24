import by.bsuir.events.MulticastEvent;
import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MulticastServer2 {

    public static void main(String[] args) throws IOException {
        InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
        InetAddress groupInetAddress = InetAddress.getByName("228.228.228.228");
        int port = 53138;

        MulticastService multicastService = new MulticastService(inetAddress, groupInetAddress, port, MulticastServer2::handle);
        multicastService.startReceivingMulticasts();
        while (true) {

        }

//        MulticastSocket multicastSocket = new MulticastSocket(port);
//        // Создание адреса группы и просоединение к ней
//        SocketAddress groupSocketAddress = new InetSocketAddress(groupInetAddress, port);
//        multicastSocket.joinGroup(groupSocketAddress, NetworkInterface.getByName("wlp2s0"));
//        // Создание буферного UDP-пакета
//        DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0);
//        // Получение
//        while (true) {
//            System.out.println("+");
//            multicastSocket.receive(datagramPacket);
//            System.out.println("-");
//        }
    }

    public static void handle(MulticastEvent event) {
        System.out.println("Received form " + event.getInetAddress());
    }
}
