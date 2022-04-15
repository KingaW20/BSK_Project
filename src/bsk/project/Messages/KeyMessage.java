package bsk.project.Messages;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.Key;

public class KeyMessage extends Message implements Serializable {

    private Key key;
    private int keySize;
    private byte[] iv;

    public KeyMessage(Key key, int keySize, byte[] iv, MessageType type, String encryptionMode) {
        super(type, encryptionMode);
        this.key = key;
        this.keySize = keySize;
        this.iv = iv;
    }

    public Key getKey() { return this.key; }
    public byte[] getIv() { return this.iv; }
    public int getSessionKeySize() { return this.keySize; }

    public void setKey(Key key) { this.key = key; }
    public void setIv(byte[] iv) { this.iv = iv; }
    public void setKeySize(int sessionKeySize) { this.keySize = sessionKeySize; }

    public void setMessage(KeyMessage mess) {
        this.key = mess.getKey();
        this.keySize = mess.getSessionKeySize();
        this.iv = mess.getIv();
        this.type = mess.getType();
        this.encryptionMode = mess.getEncryptionMode();
    }
}
