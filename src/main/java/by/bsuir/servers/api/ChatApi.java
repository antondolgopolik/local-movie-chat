package by.bsuir.servers.api;

import by.bsuir.events.EventHandler;
import by.bsuir.events.TextEvent;
import by.bsuir.messages.TextMessage;

public class ChatApi {
    private final ChatApiProvider chatApiProvider;

    private EventHandler<TextEvent> onTextMessageReceived;

    public ChatApi(ChatApiProvider chatApiProvider) {
        this.chatApiProvider = chatApiProvider;
    }

    public void sendTextMessage(TextMessage textMessage) {
        chatApiProvider.sendTextMessage(textMessage);
    }

    public EventHandler<TextEvent> getOnTextMessageReceived() {
        return onTextMessageReceived;
    }

    public void setOnTextMessageReceived(EventHandler<TextEvent> onTextMessageReceived) {
        this.onTextMessageReceived = onTextMessageReceived;
    }

    public interface ChatApiProvider {

        ChatApi getChatApi();

        void sendTextMessage(TextMessage textMessage);
    }
}
