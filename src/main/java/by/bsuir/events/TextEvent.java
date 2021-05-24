package by.bsuir.events;

import by.bsuir.messages.TextMessage;

public class TextEvent implements Event {
    private final TextMessage textMessage;

    public TextEvent(TextMessage textMessage) {
        this.textMessage = textMessage;
    }

    public TextMessage getTextMessage() {
        return textMessage;
    }
}
