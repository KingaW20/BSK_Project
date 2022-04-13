package bsk.project;

import java.io.Serializable;

public class Message implements Serializable {
    private String content;
    private boolean isFile;

    public Message() { }

    public Message(String content, boolean isFile) {
        this.content = content;
        this.isFile = isFile;
    }

    public String getContent() { return this.content; };
    public boolean ifFile() { return this.isFile; };

    public void setContent(String content) { this.content = content; }
    public void setIfFile(boolean isFile) { this.isFile = isFile; }

    public void setMessage(Message mess) {
        this.content = mess.content;
        this.isFile = mess.isFile;
    }
}
