package bsk.project;

import java.io.Serializable;

public class Message implements Serializable {

    public enum MessageType {
        PUBLIC_KEY, SESSION_KEY, FILE, TEXT;
    }

    private String content;
    private MessageType type;

    public Message() { }

    public Message(String content, MessageType type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() { return this.content; };
    public MessageType getType() { return this.type; };

    public void setContent(String content) { this.content = content; }
    public void setType(MessageType type) { this.type = type; }

    public void setMessage(Message mess) {
        this.content = mess.content;
        this.type = mess.type;
    }
}
