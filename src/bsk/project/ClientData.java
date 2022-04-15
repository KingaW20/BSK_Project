package bsk.project;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

public class ClientData {
    private KeyPair keyPair;
    private int keyPairSize;
    private String path;
    private String privateKeyPath;
    private String publicKeyPath;

    private SecretKey sessionKey;
    private int sessionKeySize;
    private byte[] iv;

    public ClientData(boolean generation, int sessionKeySize, int keyPairSize) throws NoSuchAlgorithmException {
        this.sessionKeySize = sessionKeySize;
        if (generation) {
            sessionKey = generateKey(sessionKeySize);
            iv = generateIv();
            path = CONSTANTS.keyPath + java.util.UUID.randomUUID().toString();
            privateKeyPath = path + "/private.key";
            publicKeyPath = path + "/public.key";
            keyPair = generateKeyPair(keyPairSize, CONSTANTS.RsaAlgName);
        } else {
            sessionKey = null;
            iv = null;
            path = null;
            privateKeyPath = null;
            publicKeyPath = null;
            keyPair = null;
        }
    }

    public KeyPair getKeyPair() { return keyPair; }
    public int getKeyPairSize() { return keyPairSize; }
    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    public SecretKey getSessionKey() { return sessionKey; }
    public int getSessionKeySize() { return sessionKeySize; }
    public byte[] getIv() { return iv; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(this.iv); }

    public void setKeyPair(KeyPair keyPair) { this.keyPair = keyPair; }
    public void setPrivatePublicKey(PrivateKey privateKey, PublicKey publicKey) {
        this.keyPair = new KeyPair(publicKey, privateKey);
    }
    public void setKeyPairSize(int size) { this.keyPairSize = size; }
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

    private KeyPair generateKeyPair(int size, String algorithm) throws NoSuchAlgorithmException {
        try {
            KeyPair kp = readKeys(algorithm);
            if (kp != null) return kp;
        } catch (IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // if keys not exist, generate new ones
        System.out.println("KeyPair generation!");
        KeyPairGenerator generator = KeyPairGenerator.getInstance(CONSTANTS.RsaAlgName);
        generator.initialize(size);
        KeyPair keyPair = generator.generateKeyPair();
        try {
            if (!Files.exists(Paths.get(CONSTANTS.keyPath))) Files.createDirectory(Paths.get(CONSTANTS.keyPath));
            if (!Files.exists(Paths.get(path))) Files.createDirectory(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fosPub = new FileOutputStream(publicKeyPath);
                FileOutputStream fosPriv = new FileOutputStream(privateKeyPath)) {
            fosPub.write(keyPair.getPublic().getEncoded());
            fosPriv.write(keyPair.getPrivate().getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    private KeyPair readKeys(String algorithm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPublicKey publicKey = null;
        RSAPrivateKey privateKey = null;

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        File publicKeyFile = new File(publicKeyPath);
        if (Files.exists(Paths.get(publicKeyPath))) {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            System.out.println("Public key: " + publicKey);
        }

        File privateKeyFile = new File(privateKeyPath);
        if (Files.exists(Paths.get(privateKeyPath))) {
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
            System.out.println("Private key: " + privateKey);
        }

        if (publicKey != null && privateKey != null) {
            System.out.println("Found!");
            return new KeyPair(publicKey, privateKey);
        }

        return null;
    }
}
