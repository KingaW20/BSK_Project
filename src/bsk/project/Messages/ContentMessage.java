package bsk.project.Messages;

import java.io.Serializable;

public class ContentMessage extends Message implements Serializable {

    private String content;

    public ContentMessage(String content, MessageType type, String encryptionMode) {
        super(type, encryptionMode);
        this.content = content;
    }

    public String getContent() { return this.content; }

    public void setContent(String content) { this.content = content; }

    public void setMessage(ContentMessage mess) {
        this.content = mess.getContent();
        this.type = mess.getType();
        this.encryptionMode = mess.getEncryptionMode();
    }
}
