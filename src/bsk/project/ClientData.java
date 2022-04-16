package bsk.project;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class ClientData {
    private String userName;
    private SecretKey localKey;
    private int localKeySize;
    private KeyPair keyPair;
    private int keyPairSize;
    private String path;
    private String privateKeyFilePath;
    private String publicKeyFilePath;
    private String localKeyFilePath;

    private SecretKey sessionKey;
    private int sessionKeySize;
    private byte[] iv;

    public ClientData(String userName, boolean generation, int sessionKeySize, int keyPairSize, int localKeySize)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.sessionKeySize = sessionKeySize;
        this.userName = userName;
        this.localKeySize = localKeySize;
        path = CONSTANTS.keyPath + userName;
        privateKeyFilePath = path + "\\Private\\private.key";
        publicKeyFilePath = path + "\\Public\\public.key";
        localKeyFilePath = path + "\\Local\\local.key";

        if (generation) {
            localKey = generateLocalKey(CONSTANTS.SHAAlgName, CONSTANTS.SHAAlg, localKeySize);
            sessionKey = generateSessionKey(sessionKeySize, CONSTANTS.AesAlgName);
            iv = generateIv();
            keyPair = generateKeyPair(keyPairSize, CONSTANTS.RsaAlgName);
        } else {
            localKey = null;
            sessionKey = null;
            iv = null;
            keyPair = null;
        }
    }

    public String getUserName() { return userName; }
    public SecretKey getLocalKey() { return localKey; }
    public KeyPair getKeyPair() { return keyPair; }
    public int getKeyPairSize() { return keyPairSize; }
    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    public SecretKey getSessionKey() { return sessionKey; }
    public int getSessionKeySize() { return sessionKeySize; }
    public byte[] getIv() { return iv; }
    public IvParameterSpec getIvParameter() { return new IvParameterSpec(this.iv); }

    public void setUserName(String userName) { this.userName = userName; }
    public void setLocalKey(SecretKey key) { this.localKey = key; }
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

    //TODO: delete algorithm
    private SecretKey generateLocalKey(String shaAlg, String algorithm, int localKeySize)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        //read if exists
        SecretKey localKey = null;

        //KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        File localKeyFile = new File(localKeyFilePath);
        if (Files.exists(Paths.get(localKeyFilePath))) {
            byte[] localKeyBytes = Files.readAllBytes(localKeyFile.toPath());
            localKey = new SecretKeySpec(localKeyBytes, 0, localKeyBytes.length, shaAlg);
            System.out.println("ClientData - local key readed: " + localKey);
        }

        if (localKey != null) {
            System.out.println("ClientData - local key founded!");
            return localKey;
        }

        //generate new one
        byte[] salt = new byte[12];
        new SecureRandom().nextBytes(salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        KeySpec spec = new PBEKeySpec(userName.toCharArray(), salt, 65536, localKeySize);
        localKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), shaAlg);

        createDirectory(localKeyFilePath);

        try (FileOutputStream fos = new FileOutputStream(localKeyFilePath)) {
            fos.write(localKey.getEncoded());
            System.out.println("ClientData - local key generated: " + localKey);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        PublicKey publicKey = null;
        PrivateKey privateKey = null;

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        File publicKeyFile = new File(publicKeyFilePath);
        if (Files.exists(Paths.get(publicKeyFilePath))) {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            System.out.println("ClientData - public key readed: " + publicKey);
        }

        File privateKeyFile = new File(privateKeyFilePath);
        if (Files.exists(Paths.get(privateKeyFilePath))) {
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
            System.out.println("ClientData - private key readed: " + privateKey);
        }

        if (publicKey != null && privateKey != null) {
            System.out.println("ClientData - keys founded!");
            return new KeyPair(publicKey, privateKey);
        }

        return null;
    }

    private KeyPair generateNewKeyPair(int keySize)
            throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(CONSTANTS.RsaAlgName);
        generator.initialize(keySize);
        KeyPair keyPair = generator.generateKeyPair();

        //create Directory
        createDirectory(publicKeyFilePath);
        createDirectory(privateKeyFilePath);

        try (FileOutputStream fosPub = new FileOutputStream(publicKeyFilePath);
             FileOutputStream fosPriv = new FileOutputStream(privateKeyFilePath)) {
            fosPub.write(keyPair.getPublic().getEncoded());
            System.out.println("ClientData - public key generated: " + keyPair.getPublic());
            fosPriv.write(keyPair.getPrivate().getEncoded());
            System.out.println("ClientData - private key generated: " + keyPair.getPrivate());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return keyPair;
    }

    private void createDirectory(String path) {
        File publicKeyFile = new File(path);
        File parent = publicKeyFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
    }
}
