import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Запрос соединения
        InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, 53137);
        Socket socket = new Socket(inetAddress, 53);
        socket.connect(socketAddress);
        // Отправка сообщения
        if (!socket.isConnected()) {
            System.out.println("KUDAAAA");
        }
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write("Hello, World!");
        bufferedWriter.newLine();
        bufferedWriter.write("How are u?");
        bufferedWriter.flush();
        // Разрываем соединение
        socket.close();
    }
}
