package by.bsuir.servers.api;

import by.bsuir.events.ConnectEvent;
import by.bsuir.events.DisconnectEvent;
import by.bsuir.events.EventHandler;
import by.bsuir.events.RoomCloseEvent;

public class RoomApi {
    private final RoomApiProvider roomApiProvider;

    private EventHandler<ConnectEvent> onViewerConnected;
    private EventHandler<DisconnectEvent> onViewerDisconnected;
    private EventHandler<RoomCloseEvent> onRoomClosed;

    public RoomApi(RoomApiProvider roomApiProvider) {
        this.roomApiProvider = roomApiProvider;
    }

    public int getOnline() {
        return roomApiProvider.getOnline();
    }

    public EventHandler<ConnectEvent> getOnViewerConnected() {
        return onViewerConnected;
    }

    public void setOnViewerConnected(EventHandler<ConnectEvent> onViewerConnected) {
        this.onViewerConnected = onViewerConnected;
    }

    public EventHandler<DisconnectEvent> getOnViewerDisconnected() {
        return onViewerDisconnected;
    }

    public void setOnViewerDisconnected(EventHandler<DisconnectEvent> onViewerDisconnected) {
        this.onViewerDisconnected = onViewerDisconnected;
    }

    public EventHandler<RoomCloseEvent> getOnRoomClosed() {
        return onRoomClosed;
    }

    public void setOnRoomClosed(EventHandler<RoomCloseEvent> onRoomClosed) {
        this.onRoomClosed = onRoomClosed;
    }

    public interface RoomApiProvider {

        RoomApi getRoomApi();

        int getOnline();
    }
}
