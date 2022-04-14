package bsk.project.Messages;

import bsk.project.CONSTANTS;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        PUBLIC_KEY, SESSION_KEY, FILE, TEXT;
    }

    protected MessageType type;
    protected String encryptionMode;

    public Message() {}

    public Message(MessageType type, String encryptionMode) {
        this.type = type;
        this.encryptionMode = encryptionMode;
    }

    public MessageType getType() { return this.type; }

    public String getEncryptionMode() { return this.encryptionMode; }

    public void setType(MessageType type) { this.type = type; }
    public void setEncryptionMode(String encryptionMode) { this.encryptionMode = encryptionMode; }
}
