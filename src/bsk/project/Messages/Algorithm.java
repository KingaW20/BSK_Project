package bsk.project.Messages;

import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;

public class Algorithm implements Serializable {

    private String encryptionType;
    private int keySize;
    private byte[] iv;

    public Algorithm(String encryptionType, int keySize, IvParameterSpec iv) {
        this.encryptionType = encryptionType;
        this.keySize = keySize;
        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = iv.getIV();
        }
    }

    public String getEncryptionType() { return encryptionType; }
    public int getKeySize() { return keySize; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(iv); }
    public byte[] getIv() { return iv; }

    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }
    public void setKeySize(int keySize) { this.keySize = keySize; }
    public void setIv(IvParameterSpec iv) {
        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = iv.getIV();
        }
    }
    public void setIv(byte[] iv) { this.iv = iv; }
}
