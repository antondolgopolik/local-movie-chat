package by.bsuir.events;

import by.bsuir.messages.PauseMessage;

public class PauseEvent implements Event {
    private final PauseMessage pauseMessage;

    public PauseEvent(PauseMessage pauseMessage) {
        this.pauseMessage = pauseMessage;
    }

    public PauseMessage getPauseMessage() {
        return pauseMessage;
    }
}
