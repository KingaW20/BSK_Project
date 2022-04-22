package bsk.project;

import bsk.project.Encryption.*;
import bsk.project.Messages.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class ClientData {
    private SecretKey localKey;
    private String userName;
    private KeyPair keyPair;
    private int keyPairSize;
    private String path;
    private String localIvPath;
    private String localKeyPath;
    private String privateKeyPath;
    private String publicKeyPath;

    private SecretKey sessionKey;
    private int sessionKeySize;
    private byte[] iv;
    private byte[] ivLocal;

    public ClientData(String userName, boolean generation, int sessionKeySize, int keyPairSize)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchProviderException,
            IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        this.sessionKeySize = sessionKeySize;
        this.userName = userName;
        this.path = CONSTANTS.keyPath + userName;
        this.localIvPath = path + "\\Local\\iv.par";
        this.localKeyPath = path + "\\Local\\local.key";
        this.privateKeyPath = path + "\\Private\\private.key";
        this.publicKeyPath = path + "\\Public\\public.key";
        this.ivLocal = null;

        if (generation) {
            iv = generateIv();
            localKey = generateLocalKey();
            sessionKey = generateSessionKey(sessionKeySize, CONSTANTS.AesAlgName);
            keyPair = generateKeyPair(keyPairSize, CONSTANTS.RsaAlgName);
        } else {
            localKey = null;
            sessionKey = null;
            iv = null;
            keyPair = null;
        }
    }

    public String getUserName() { return userName; }
    public KeyPair getKeyPair() { return keyPair; }
    public int getKeyPairSize() { return keyPairSize; }
    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    public SecretKey getSessionKey() { return sessionKey; }
    public int getSessionKeySize() { return sessionKeySize; }
    public byte[] getIv() { return iv; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(this.iv); }

    public void setUserName(String userName) { this.userName = userName; }
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

    private SecretKey generateLocalKey()
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        SecretKey localKey = null;

        // read iv
        File localIvFile = new File(localIvPath);
        if (Files.exists(Paths.get(String.valueOf(localIvFile)))) {
            ivLocal = Files.readAllBytes(localIvFile.toPath());
            System.out.println("ClientData - iv readed: " + ivLocal);
        }

        // read local key
        File localKeyFile = new File(localKeyPath);
        if (Files.exists(Paths.get(String.valueOf(localKeyFile)))) {
            byte[] localKeyBytes = Files.readAllBytes(localKeyFile.toPath());
            localKey = new SecretKeySpec(localKeyBytes, 0, localKeyBytes.length, "AES");

            System.out.println("ClientData - local key readed: " + localKey);
            return localKey;
        }

        // if local key doesn't exist
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(userName.toCharArray(), salt, 65536, 256);
        localKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        saveToFile(localKeyPath, localKey.getEncoded());
        System.out.println("Local key: " + localKey);

        return localKey;
    }

    private SecretKey generateSessionKey(int size, String algorithmName) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);
        keyGenerator.init(size);
        //return keyGenerator.generateKey();

        SecretKey key = keyGenerator.generateKey();
        System.out.println("ClientData - session key generated: " + key);
        return key;
    }

    private KeyPair generateKeyPair(int size, String algorithm)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException {
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

    private KeyPair readKeys(String algorithm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        PublicKey publicKey = null;
        PrivateKey privateKey = null;

        File publicKeyFile = new File(publicKeyPath);
        if (Files.exists(Paths.get(publicKeyPath))) {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

            publicKey = (PublicKey) Decryptor.decryptKey(
                    new ContentMessage(
                            Base64.getEncoder().encodeToString(publicKeyBytes),
                            Message.MessageType.PUBLIC_KEY,
                            new Algorithm(CONSTANTS.AesAlgCBCMode, 128, new IvParameterSpec(ivLocal))),
                    localKey
            );

            System.out.println("ClientData - public key readed: " + publicKey);
        }

        File privateKeyFile = new File(privateKeyPath);
        if (Files.exists(Paths.get(privateKeyPath))) {
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

            privateKey = (PrivateKey) Decryptor.decryptKey(
                    new ContentMessage(
                            Base64.getEncoder().encodeToString(privateKeyBytes),
                            Message.MessageType.PRIVATE_KEY,
                            new Algorithm(CONSTANTS.AesAlgCBCMode, 128, new IvParameterSpec(ivLocal))),
                    localKey
            );

            System.out.println("ClientData - private key readed: " + privateKey);
        }

        if (publicKey != null && privateKey != null) {
            System.out.println("ClientData - keys founded!");
            return new KeyPair(publicKey, privateKey);
        }

        return null;
    }

    private KeyPair generateNewKeyPair(int keySize)
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(CONSTANTS.RsaAlgName);
        generator.initialize(keySize);
        KeyPair keyPair = generator.generateKeyPair();

        byte[] encryptedPublicKey = Encryptor.encryptKey(
                new KeyMessage(keyPair.getPublic(),
                        Message.MessageType.PUBLIC_KEY,
                        new Algorithm(CONSTANTS.AesAlgCBCMode, 128, getIvParameter())),
                localKey);

        byte[] encryptedPrivateKey = Encryptor.encryptKey(
                new KeyMessage(keyPair.getPrivate(),
                        Message.MessageType.PRIVATE_KEY,
                        new Algorithm(CONSTANTS.AesAlgCBCMode, 128, getIvParameter())),
                localKey);

        saveToFile(publicKeyPath, encryptedPublicKey);
        saveToFile(privateKeyPath, encryptedPrivateKey);
        saveToFile(localIvPath, getIv());
        System.out.println("Public key saved: " + encryptedPublicKey);
        System.out.println("Private key saved: " + encryptedPrivateKey);
        System.out.println("Local iv saved: " + getIv());

        return keyPair;
    }

    private void saveToFile(String filePath, byte[] key) {
        File targetFile = new File(filePath);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
