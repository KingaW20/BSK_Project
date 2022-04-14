package bsk.project.Messages;

import javax.crypto.SecretKey;
import java.io.Serializable;

public class KeyMessage extends Message implements Serializable {

    private SecretKey key;
    private byte[] iv;

    public KeyMessage(SecretKey key, byte[] iv, MessageType type, String encryptionMode) {
        super(type, encryptionMode);
        this.key = key;
        this.iv = iv;
    }

    public SecretKey getKey() { return this.key; }
    public byte[] getIv() { return this.iv; }

    public void setKey(SecretKey key) { this.key = key; }
    public void setIv(byte[] iv) { this.iv = iv; }

    public void setMessage(KeyMessage mess) {
        this.key = mess.getKey();
        this.iv = mess.getIv();
        this.type = mess.getType();
        this.encryptionMode = mess.getEncryptionMode();
    }
}
