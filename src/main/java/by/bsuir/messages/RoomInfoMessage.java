package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение с информацией о комнате
 */
public class RoomInfoMessage extends Message {
    private final String name;
    private final String media;
    private final int online;

    public RoomInfoMessage(InetAddress address, String name, String media, int online) {
        super(MessageType.ROOM_INFO_MESSAGE, address);
        this.name = name;
        this.media = media;
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public String getMedia() {
        return media;
    }

    public int getOnline() {
        return online;
    }
}
