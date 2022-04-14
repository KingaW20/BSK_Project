package bsk.project.Messages;

import javax.crypto.SecretKey;
import java.io.Serializable;

public class KeyMessage extends Message implements Serializable {

    private SecretKey key;
    private int sessionKeySize;
    private byte[] iv;

    public KeyMessage(SecretKey key, int sessionKeySize, byte[] iv, MessageType type, String encryptionMode) {
        super(type, encryptionMode);
        this.key = key;
        this.sessionKeySize = sessionKeySize;
        this.iv = iv;
    }

    public SecretKey getKey() { return this.key; }
    public byte[] getIv() { return this.iv; }
    public int getSessionKeySize() { return this.sessionKeySize; }

    public void setKey(SecretKey key) { this.key = key; }
    public void setIv(byte[] iv) { this.iv = iv; }
    public void setSessionKeySize(int sessionKeySize) { this.sessionKeySize = sessionKeySize; }

    public void setMessage(KeyMessage mess) {
        this.key = mess.getKey();
        this.sessionKeySize = mess.getSessionKeySize();
        this.iv = mess.getIv();
        this.type = mess.getType();
        this.encryptionMode = mess.getEncryptionMode();
    }
}
