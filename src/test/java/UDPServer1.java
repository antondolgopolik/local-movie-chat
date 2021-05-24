import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPServer1 {

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(12345));

        byte[] bytes = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        String message;
        do {
            datagramSocket.receive(datagramPacket);
            message = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
            System.out.println(message);
        } while (!message.equals("q"));
    }
}
