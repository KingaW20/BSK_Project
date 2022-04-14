package bsk.project;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ClientData {
    private SecretKey sessionKey;
    private byte[] iv;

    public ClientData(boolean generation) throws NoSuchAlgorithmException {
        if (generation) {
            generateSessionKey(CONSTANTS.sessionKeySize);
            iv = generateIv();
        } else {
            sessionKey = null;
            iv = null;
        }
    }

    public SecretKey getSessionKey() { return sessionKey; }

    public byte[] getIv() { return iv; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(this.iv); }

    public void setSessionKey(SecretKey key) { this.sessionKey = key; }
    public void setIv(byte[] iv) { this.iv = iv; }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private void generateSessionKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(CONSTANTS.AesAlgName);
        keyGenerator.init(n);
        sessionKey = keyGenerator.generateKey();
    }
}
