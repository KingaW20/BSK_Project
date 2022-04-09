package bsk.project;

import java.io.Serializable;

public class Message implements Serializable {
    private String content;
    private boolean isFile;
    private boolean readyToSend;

    public Message() {
        this.readyToSend = false;
    }

    public Message(String content, boolean isFile, boolean readyToSend) {
        this.content = content;
        this.isFile = isFile;
        this.readyToSend = readyToSend;
    }

    public String getContent() { return this.content; };
    public boolean ifFile() { return this.isFile; };
    public boolean ifReadyToSend() { return this.readyToSend; }

    public void setContent(String content) { this.content = content; }
    public void setIfFile(boolean isFile) { this.isFile = isFile; }
    public void setIfReadyToSend(boolean readyToSend) { this.readyToSend = readyToSend; }

    public void setMessage(Message mess) {
        this.content = mess.content;
        this.isFile = mess.isFile;
        this.readyToSend = mess.readyToSend;
    }
}
