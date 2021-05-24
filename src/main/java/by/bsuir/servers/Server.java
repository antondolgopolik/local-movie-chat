package by.bsuir.servers;

import java.net.InetAddress;

public abstract class Server {
    protected final InetAddress inetAddress;

    public Server(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public abstract void start();

    public abstract void stop();
}
