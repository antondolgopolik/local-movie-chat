package by.bsuir.messages;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
    protected final MessageType messageType;
    protected final InetAddress address;

    protected Message(MessageType messageType, InetAddress address) {
        this.messageType = messageType;
        this.address = address;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public InetAddress getAddress() {
        return address;
    }

    public enum MessageType {
        ROOM_INFO_MESSAGE, ROOM_CLOSE_MESSAGE, VIEWER_INFO_MESSAGE, STATUS_MESSAGE,
        TEXT_MESSAGE, DISCONNECT_MESSAGE, PAUSE_MESSAGE
    }
}
