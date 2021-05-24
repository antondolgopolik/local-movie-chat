package by.bsuir.events;

import java.net.Socket;

public class SocketEvent implements Event {
    private final Socket socket;

    public SocketEvent(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
