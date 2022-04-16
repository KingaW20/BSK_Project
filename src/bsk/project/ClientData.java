package bsk.project;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

public class ClientData {
    private KeyPair keyPair;
    private int keyPairSize;
    private String path;
    private String privateKeyPath;
    private String publicKeyPath;

    private SecretKey sessionKey;
    private int sessionKeySize;
    private byte[] iv;

    public ClientData(boolean generation, int sessionKeySize, int keyPairSize)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.sessionKeySize = sessionKeySize;
        if (generation) {
            sessionKey = generateKey(sessionKeySize, CONSTANTS.AesAlgName);
            iv = generateIv();
            //TODO: correct to user name instead of UUID
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
        byte[] iv = new byte[CONSTANTS.ivSize];
        new SecureRandom().nextBytes(iv);
        System.out.println("ClientData - iv generated: " + iv);
        return iv;
    }

    private SecretKey generateKey(int size, String algorithmName) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);
        keyGenerator.init(size);
        //return keyGenerator.generateKey();

        SecretKey key = keyGenerator.generateKey();
        System.out.println("ClientData - session key generated: " + key);
        return key;
    }

    private KeyPair generateKeyPair(int size, String algorithm)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        // if keys exist, read them
        KeyPair kp = readKeys(algorithm);
        if (kp != null) {
            System.out.println("ClientData - keyPair readed!");
            return kp;
        }

        // if keys do not exist, generate new ones
        System.out.println("ClientData - keyPair generation!");
        return generateNewKeyPair(size);
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
            System.out.println("ClientData - public key readed: " + publicKey);
        }
        int x = 1;

        File privateKeyFile = new File(privateKeyPath);
        if (Files.exists(Paths.get(privateKeyPath))) {
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
            System.out.println("ClientData - private key readed: " + privateKey);
        }

        if (publicKey != null && privateKey != null) {
            System.out.println("ClientData - keys founded!");
            return new KeyPair(publicKey, privateKey);
        }

        return null;
    }

    private KeyPair generateNewKeyPair(int keySize)
            throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(CONSTANTS.RsaAlgName);
        generator.initialize(keySize);
        KeyPair keyPair = generator.generateKeyPair();
        if (!Files.exists(Paths.get(CONSTANTS.keyPath))) {
            Files.createDirectory(Paths.get(CONSTANTS.keyPath));
        }
        if (!Files.exists(Paths.get(path))) {
            Files.createDirectory(Paths.get(path));
        }

        try (FileOutputStream fosPub = new FileOutputStream(publicKeyPath);
             FileOutputStream fosPriv = new FileOutputStream(privateKeyPath)) {
            fosPub.write(keyPair.getPublic().getEncoded());
            System.out.println("ClientData - public key generated: " + keyPair.getPublic());
            fosPriv.write(keyPair.getPrivate().getEncoded());
            System.out.println("ClientData - private key generated: " + keyPair.getPrivate());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return keyPair;
    }
}
