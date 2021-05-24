package by.bsuir.servers.services;

import by.bsuir.events.EventHandler;
import by.bsuir.events.MulticastEvent;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;

public class MulticastService {
    private final MulticastSocket multicastSocket;
    private final EventHandler<MulticastEvent> onMulticastReceived;
    private final Thread thread = new Thread(this::receive);
    private boolean isStopped;

    public MulticastService(InetAddress inetAddress, InetAddress groupInetAddress,
                            int port, EventHandler<MulticastEvent> onMulticastReceived) {
        try {
            // Создание сокета
            multicastSocket = new MulticastSocket(port);
            // Просоединение к группе
            SocketAddress socketAddress = new InetSocketAddress(groupInetAddress, port);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            multicastSocket.joinGroup(socketAddress, networkInterface);
            // Установка обработчика
            this.onMulticastReceived = onMulticastReceived;
            // Настройка потока
            thread.setDaemon(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receive() {
        // Создание буфера
        DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
        // Контейнер обработанных пакетов
        HashSet<String> packets = new HashSet<>();
        // Начать получение
        while (!isStopped) {
            try {
                multicastSocket.receive(datagramPacket);
                // Получение ID
                byte[] bytes = datagramPacket.getData();
                int offset = datagramPacket.getOffset();
                int length = datagramPacket.getLength();
                String id = new String(bytes, offset, length, StandardCharsets.UTF_8);
                // Был ли уже обработан
                if (!packets.contains(id)) {
                    // Сохранение
                    packets.add(id);
                    // Обработка
                    InetAddress inetAddress = datagramPacket.getAddress();
                    MulticastEvent multicastEvent = new MulticastEvent(inetAddress);
                    onMulticastReceived.handle(multicastEvent);
                }
            } catch (SocketException ignored) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendMulticast(InetAddress inetAddress, InetAddress groupInetAddress, int port) {
        try {
            // Создание UDP-сокета
            DatagramSocket datagramSocket = new DatagramSocket(0, inetAddress);
            // Создание ID
            Date date = new Date();
            long time = date.getTime();
            String address = inetAddress.getHostAddress();
            String id = address + " " + time;
            // Создание пакета
            byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, groupInetAddress, port);
            // Отправка пакета
            datagramSocket.send(datagramPacket);
            datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startReceivingMulticasts() {
        thread.start();
    }

    public void stopReceivingMulticasts() {
        isStopped = true;
        multicastSocket.close();
    }
}
