package by.bsuir.events;

import by.bsuir.messages.RoomCloseMessage;

public class RoomCloseEvent implements Event {
    private final RoomCloseMessage roomCloseMessage;

    public RoomCloseEvent(RoomCloseMessage roomCloseMessage) {
        this.roomCloseMessage = roomCloseMessage;
    }

    public RoomCloseMessage getRoomCloseMessage() {
        return roomCloseMessage;
    }
}
