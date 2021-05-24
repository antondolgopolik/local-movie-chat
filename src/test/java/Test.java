import by.bsuir.servers.StreamerServer;
import by.bsuir.servers.ViewerServer;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) throws Exception {
        System.out.println(InetAddress.getByName("192.168.0.105").getHostAddress());
    }
}
