package by.bsuir.servers.api;

import by.bsuir.events.EventHandler;
import by.bsuir.events.RoomCloseEvent;
import by.bsuir.events.RoomInfoEvent;

public class NetworkStateApi {
    private final NetworkStateApiProvider networkStateApiProvider;

    private EventHandler<RoomInfoEvent> onRoomOpened;
    private EventHandler<RoomCloseEvent> onRoomClosed;

    public NetworkStateApi(NetworkStateApiProvider networkStateApiProvider) {
        this.networkStateApiProvider = networkStateApiProvider;
    }

    public void requestUpdateForAvailableRooms() {
        networkStateApiProvider.requestUpdateForAvailableRooms();
    }

    public EventHandler<RoomInfoEvent> getOnRoomOpened() {
        return onRoomOpened;
    }

    public void setOnRoomOpened(EventHandler<RoomInfoEvent> onRoomOpened) {
        this.onRoomOpened = onRoomOpened;
    }

    public EventHandler<RoomCloseEvent> getOnRoomClosed() {
        return onRoomClosed;
    }

    public void setOnRoomClosed(EventHandler<RoomCloseEvent> onRoomClosed) {
        this.onRoomClosed = onRoomClosed;
    }

    public interface NetworkStateApiProvider {

        NetworkStateApi getNetworkStateApi();

        void requestUpdateForAvailableRooms();
    }
}
