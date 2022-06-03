package bsk.project.Encryption;

import bsk.project.*;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class Decryptor {

    public static Message decryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        ContentMessage contentMessage = (ContentMessage) message;
        Algorithm algorithm = message.getAlgorithm();

        if (!App.authorized) {
            if (message.getType().equals(MessageType.SESSION_KEY)) {
                byte[] encryptedPublicKeyBytes = Base64.getDecoder().decode(contentMessage.getContent());
                SecretKey sessionKey = new SecretKeySpec(
                        encryptedPublicKeyBytes, 0, encryptedPublicKeyBytes.length, CONSTANTS.AesAlgName);
                return new KeyMessage(sessionKey, message.getType(), algorithm);
            }

            return message;
        }

        if (message.getType().equals(MessageType.TEXT)) {

            System.out.println("Decrypt algorithm: " + algorithm.getEncryptionType());
            Cipher cipher = Encryptor.setupCipher(algorithm, key, Cipher.DECRYPT_MODE);
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(contentMessage.getContent()));
            System.out.println("Decryptor - decrypted message: " + new String(plainText));
            contentMessage.setContent(new String(plainText));
            return contentMessage;

        } else if (message.getType().equals(MessageType.SESSION_KEY)) {

            byte[] encryptedPublicKeyBytes = Base64.getDecoder().decode(contentMessage.getContent());
            Cipher cipher = Encryptor.setupCipher(algorithm, key, Cipher.DECRYPT_MODE);
            byte[] decryptedSessionKeyBytes = cipher.doFinal(encryptedPublicKeyBytes);
            SecretKey sessionKey = new SecretKeySpec(
                    decryptedSessionKeyBytes, 0, decryptedSessionKeyBytes.length, CONSTANTS.AesAlgName);
            System.out.println("Decryptor - decrypted session key: " + sessionKey);

            return new KeyMessage(sessionKey, message.getType(), algorithm);
        }

        return contentMessage;
    }

    public static Key decryptKey(ContentMessage message, Key key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {

        String type = message.getAlgorithm().getEncryptionType();
        System.out.println("Decryptor - Decrypt algorithm: " + type);
        Key result = null;

        Cipher cipher = Encryptor.setupCipher(message.getAlgorithm(), key, Cipher.DECRYPT_MODE);
        byte[] decryptedKeyBytes = cipher.doFinal(Base64.getDecoder().decode(message.getContent()));

        KeyFactory keyFactory = KeyFactory.getInstance(CONSTANTS.RsaAlgName);
        if (type.equals(MessageType.PRIVATE_KEY))
            result = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedKeyBytes));
        else if (type.equals(MessageType.PUBLIC_KEY))
            result = keyFactory.generatePublic(new X509EncodedKeySpec(decryptedKeyBytes));

        System.out.println("Decryptor - decrypted key: " + result);

        return result;
    }

    public static byte[] decryptFile(Message message, Key key) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException,
            InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {

        FileMessage fileMessage = (FileMessage) message;

        if (!App.authorized)
            return fileMessage.getFileBytes();

        byte[] result = null;
        if (message.getType().equals(MessageType.FILE)) {
            Cipher decryptCipher = Encryptor.setupCipher(message.getAlgorithm(), key, Cipher.DECRYPT_MODE);
            result = decryptCipher.doFinal(fileMessage.getFileBytes());
            System.out.println("Decryptor - decrypted file part: " + fileMessage.getFileName());
        }
        return result;
    }
}
