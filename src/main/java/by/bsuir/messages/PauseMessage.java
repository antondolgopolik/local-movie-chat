package by.bsuir.messages;

import java.net.InetAddress;

public class PauseMessage extends Message {

    public PauseMessage(InetAddress address) {
        super(MessageType.PAUSE_MESSAGE, address);
    }
}
