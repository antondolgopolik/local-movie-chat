package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение об отключении обычного зрителя от комнаты
 */
public class DisconnectMessage extends Message {

    public DisconnectMessage(InetAddress address) {
        super(MessageType.DISCONNECT_MESSAGE, address);
    }
}
