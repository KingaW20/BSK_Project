package bsk.project;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ClientData {
    private SecretKey sessionKey;
    private IvParameterSpec iv;

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

    public IvParameterSpec getIv() { return iv; }

    public void setSessionKey(SecretKey key) { sessionKey = key; }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private void generateSessionKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(CONSTANTS.AesAlgName);
        keyGenerator.init(n);
        sessionKey = keyGenerator.generateKey();
    }
}
