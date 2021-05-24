package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение со статусом
 */
public class StatusMessage extends Message {
    private final StatusMessageType statusMessageType;

    public StatusMessage(InetAddress address, StatusMessageType statusMessageType) {
        super(MessageType.STATUS_MESSAGE, address);
        this.statusMessageType = statusMessageType;
    }

    public StatusMessageType getStatusMessageType() {
        return statusMessageType;
    }

    public enum StatusMessageType {
        OK, WRONG
    }
}
