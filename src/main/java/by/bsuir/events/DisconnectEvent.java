package by.bsuir.events;

import by.bsuir.messages.DisconnectMessage;

public class DisconnectEvent implements Event {
    private final DisconnectMessage disconnectMessage;

    public DisconnectEvent(DisconnectMessage disconnectMessage) {
        this.disconnectMessage = disconnectMessage;
    }

    public DisconnectMessage getDisconnectMessage() {
        return disconnectMessage;
    }
}
