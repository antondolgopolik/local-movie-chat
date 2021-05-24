package by.bsuir.messages;

import java.net.InetAddress;

/*
Сообщение с текстом
 */
public class TextMessage extends Message {
    private final String name;
    private final String text;

    public TextMessage(InetAddress address, String name, String text) {
        super(MessageType.TEXT_MESSAGE, address);
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
