package by.bsuir.events;

import java.net.InetAddress;

public class MulticastEvent implements Event {
    private final InetAddress inetAddress;

    public MulticastEvent(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
}
