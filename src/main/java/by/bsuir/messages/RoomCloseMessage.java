package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение о закрытии комнаты
 */
public class RoomCloseMessage extends Message {

    public RoomCloseMessage(InetAddress address) {
        super(MessageType.ROOM_CLOSE_MESSAGE, address);
    }
}
