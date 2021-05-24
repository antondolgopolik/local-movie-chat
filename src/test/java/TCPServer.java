import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Создание сервера
        InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
        ServerSocket serverSocket = new ServerSocket(53137, 50, inetAddress);
        // Приём соединения
        Socket socket = serverSocket.accept();
        // Ожидание момента, пока клиент не разорвёт соединение
        Thread.sleep(10000);
        // Чтение
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
