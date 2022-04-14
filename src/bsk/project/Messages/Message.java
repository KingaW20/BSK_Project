package bsk.project.Messages;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        PUBLIC_KEY, SESSION_KEY, FILE, TEXT;
    }

    protected MessageType type;

    public Message() {}

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() { return this.type; }
    public void setType(MessageType type) { this.type = type; }
}
