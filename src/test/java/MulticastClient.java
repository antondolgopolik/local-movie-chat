import by.bsuir.servers.services.MulticastService;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MulticastClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
        InetAddress groupInetAddress = InetAddress.getByName("228.228.228.228");
        int port = 53138;

        MulticastService.sendMulticast(inetAddress, groupInetAddress, port);
        MulticastService.sendMulticast(inetAddress, groupInetAddress, port);
        MulticastService.sendMulticast(inetAddress, groupInetAddress, port);

//        DatagramSocket datagramSocket = new DatagramSocket(0, inetAddress);
//        DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, groupInetAddress, port);
//        // Отправка
//        datagramSocket.send(datagramPacket);
//        datagramSocket.close();
    }
}
