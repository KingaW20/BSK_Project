package bsk.project.Messages;

import java.io.Serializable;
import java.security.Key;

public class KeyMessage extends Message implements Serializable {

    private Key key;

    public KeyMessage(Key key, MessageType type, Algorithm algorithm) {
        super(type, algorithm);
        this.key = key;
    }

    public Key getKey() { return this.key; }

    public void setKey(Key key) { this.key = key; }

    public void setMessage(KeyMessage mess) {
        this.key = mess.getKey();
        this.type = mess.getType();
    }
}
