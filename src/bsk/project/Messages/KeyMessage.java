package bsk.project.Messages;

import javax.crypto.SecretKey;
import java.io.Serializable;

public class KeyMessage extends Message implements Serializable {

    private SecretKey key;

    public KeyMessage(SecretKey key, MessageType type) {
        super(type);
        this.key = key;
    }

    public SecretKey getKey() { return this.key; };

    public void setKey(SecretKey key) { this.key = key; }

    public void setMessage(KeyMessage mess) {
        this.key = mess.key;
        this.type = mess.type;
    }
}
