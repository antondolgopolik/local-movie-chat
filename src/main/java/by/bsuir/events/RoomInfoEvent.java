package by.bsuir.events;

import by.bsuir.messages.RoomInfoMessage;

public class RoomInfoEvent implements Event {
    private final RoomInfoMessage roomInfoMessage;

    public RoomInfoEvent(RoomInfoMessage roomInfoMessage) {
        this.roomInfoMessage = roomInfoMessage;
    }

    public RoomInfoMessage getRoomInfoMessage() {
        return roomInfoMessage;
    }
}
