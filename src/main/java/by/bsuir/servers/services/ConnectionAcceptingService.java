package by.bsuir.servers.services;

import by.bsuir.events.EventHandler;
import by.bsuir.events.SocketEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectionAcceptingService {
    private static final int TIMEOUT = 1500;

    private final ServerSocket serverSocket;
    private final EventHandler<SocketEvent> onConnectionAccepted;
    private final Thread thread = new Thread(this::accept);

    public ConnectionAcceptingService(InetAddress inetAddress, int port, EventHandler<SocketEvent> onConnectionAccepted) {
        try {
            // Создание сервера
            serverSocket = new ServerSocket(port, 50, inetAddress);
            serverSocket.setSoTimeout(TIMEOUT);
            // Установка обработчика
            this.onConnectionAccepted = onConnectionAccepted;
            // Настройка потока
            thread.setDaemon(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void accept() {
        // Начать приём
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // Обработка
                SocketEvent socketEvent = new SocketEvent(socket);
                onConnectionAccepted.handle(socketEvent);
            } catch (SocketTimeoutException ignored) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Освобождение ресурсов
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAcceptingConnections() {
        thread.start();
    }

    public void waitForConnectionsToBeAccepted() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
