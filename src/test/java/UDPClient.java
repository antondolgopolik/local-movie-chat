import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
        DatagramSocket datagramSocket = new DatagramSocket(0, inetAddress);
        datagramSocket.setBroadcast(true);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;
        InetAddress ia = InetAddress.getByName("255.255.255.255");
        do {
            input = reader.readLine();
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, ia, 12345);
            datagramSocket.send(datagramPacket);
        } while (!input.equals("q"));
    }
}
