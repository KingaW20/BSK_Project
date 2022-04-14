package bsk.project.Messages;

import java.io.Serializable;

public class ContentMessage extends Message implements Serializable {

    private String content;

    public ContentMessage(String content, MessageType type) {
        super(type);
        this.content = content;
    }

    public String getContent() { return this.content; };

    public void setContent(String content) { this.content = content; }

    public void setMessage(ContentMessage mess) {
        this.content = mess.content;
        this.type = mess.type;
    }
}
