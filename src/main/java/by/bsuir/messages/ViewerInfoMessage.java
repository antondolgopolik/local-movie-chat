package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение с информацией о зрителе
 */
public class ViewerInfoMessage extends Message {
    private final InetAddress streamerInetAddress;

    public ViewerInfoMessage(InetAddress address, InetAddress streamerInetAddress) {
        super(MessageType.VIEWER_INFO_MESSAGE, address);
        this.streamerInetAddress = streamerInetAddress;
    }

    public InetAddress getStreamerInetAddress() {
        return streamerInetAddress;
    }
}
