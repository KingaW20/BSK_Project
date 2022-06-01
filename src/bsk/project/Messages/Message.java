package bsk.project.Messages;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        PUBLIC_KEY, PRIVATE_KEY, SESSION_KEY, FILE, TEXT, FILENAME;
    }

    protected MessageType type;
    protected Algorithm algorithm;

    public Message() {}

    public Message(MessageType type, Algorithm algorithm) {
        this.type = type;
        this.algorithm = algorithm;
    }

    public MessageType getType() { return this.type; }
    public Algorithm getAlgorithm() { return this.algorithm; }

    public void setType(MessageType type) { this.type = type; }
    public void setAlgorithm(Algorithm algorithm) { this.algorithm = algorithm; }
}
