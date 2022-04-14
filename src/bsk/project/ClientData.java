package bsk.project;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ClientData {
    private SecretKey sessionKey;
    private int sessionKeySize;
    private byte[] iv;

    public ClientData(boolean generation, int sessionKeySize) throws NoSuchAlgorithmException {
        this.sessionKeySize = sessionKeySize;
        if (generation) {
            sessionKey = generateKey(sessionKeySize);
            iv = generateIv();
        } else {
            sessionKey = null;
            iv = null;
        }
    }

    public SecretKey getSessionKey() { return sessionKey; }
    public int getSessionKeySize() { return sessionKeySize; }

    public byte[] getIv() { return iv; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(this.iv); }

    public void setSessionKey(SecretKey key) { this.sessionKey = key; }
    public void setSessionKeySize(int size) { this.sessionKeySize = size; }
    public void setIv(byte[] iv) { this.iv = iv; }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(CONSTANTS.AesAlgName);
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }
}
