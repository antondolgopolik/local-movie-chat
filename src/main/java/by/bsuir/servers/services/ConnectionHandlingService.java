package by.bsuir.servers.services;

import by.bsuir.datastructures.SynchronizedLinkedList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ConnectionHandlingService {
    private final SynchronizedLinkedList<ConnectionHandler> connectionHandlers = new SynchronizedLinkedList<>();
    private final Consumer<ConnectionHandler> defaultHandler;

    public ConnectionHandlingService(Consumer<ConnectionHandler> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /*
    Добавляет соединение в сервис обработки соединений
     */
    public void addConnection(Socket socket) {
        ConnectionHandler connectionHandler = new ConnectionHandler(socket);
        // Начать обработку соединения
        connectionHandler.start();
    }

    /*
    Выполняет обработку каждого соединений. Запрещается останавливать обработку соединения, для этого
    используй метод stop()
     */
    public void handle(Consumer<ConnectionHandler> handler) {
        synchronized (connectionHandlers) {
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                handler.accept(connectionHandler);
            }
        }
    }

    /*
    Прекщает обработку и закрывает каждое соединение
     */
    public void stop() {
        stop(null);
    }

    /*
    Выполняет обработку каждого соединения, затем прекращает его обработку и закрывает
     */
    public void stop(Consumer<ConnectionHandler> handler) {
        synchronized (connectionHandlers) {
            while (!connectionHandlers.isEmpty()) {
                try {
                    ConnectionHandler connectionHandler = connectionHandlers.peekFirst();
                    // Обработка соединения перед его закрытием
                    if (handler != null) {
                        handler.accept(connectionHandler);
                    }
                    // Закрытие соединения
                    connectionHandler.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class ConnectionHandler {
        private final Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        private final SynchronizedLinkedList<ConnectionHandler>.Node node;
        private final Thread thread = new Thread(this::handle);
        private boolean isStopped;

        private ConnectionHandler(Socket socket) {
            this.socket = socket;
            // Сохранение обработчика
            synchronized (connectionHandlers) {
                node = connectionHandlers.add(this);
            }
            // Настройка потока
            thread.setDaemon(true);
        }

        /*
        Начинает обработку соединения
         */
        private void start() {
            thread.start();
        }

        private void handle() {
            while (!isStopped) {
                defaultHandler.accept(this);
            }
        }

        public Socket getSocket() {
            return socket;
        }

        public ObjectInputStream getInputStream() throws IOException {
            if (inputStream == null) {
                inputStream = new ObjectInputStream(socket.getInputStream());
            }
            return inputStream;
        }

        public ObjectOutputStream getOutputStream() throws IOException {
            if (outputStream == null) {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
            }
            return outputStream;
        }

        /*
        Прекращает обработку соединения, а также закрывает его
         */
        public void stop() throws IOException {
            // Прерывание потока обработки
            isStopped = true;
            // Закрытие соединения
            socket.close();
            // Удаление обработчика
            synchronized (connectionHandlers) {
                connectionHandlers.remove(node);
            }
        }
    }
}
