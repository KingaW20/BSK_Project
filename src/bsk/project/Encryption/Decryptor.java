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
import java.util.HashMap;
import java.util.Map;

public class Decryptor {

    public static Message decryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        ContentMessage contentMessage = null;
        if (message instanceof ContentMessage)
            contentMessage = (ContentMessage) message;
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

            String algorithmType = algorithm.getEncryptionType();
            System.out.println("Decrypt algorithm: " + algorithmType);
            Cipher cipher = Cipher.getInstance(algorithmType);

            if (algorithmType.equals(CONSTANTS.AesAlgECBMode)) {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } else if (algorithmType.equals(CONSTANTS.AesAlgCBCMode)) {
                cipher.init(Cipher.DECRYPT_MODE, key, algorithm.getIvParameter());
            }

            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(contentMessage.getContent()));
            System.out.println("Decryptor - decrypted message: " + new String(plainText));
            contentMessage.setContent(new String(plainText));
            return contentMessage;

        } else if (message.getType().equals(MessageType.SESSION_KEY)) {

            byte[] encryptedPublicKeyBytes = Base64.getDecoder().decode(contentMessage.getContent());
            Cipher decryptCipher = Cipher.getInstance(algorithm.getEncryptionType());
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedSessionKeyBytes = decryptCipher.doFinal(encryptedPublicKeyBytes);
            SecretKey sessionKey = new SecretKeySpec(
                    decryptedSessionKeyBytes, 0, decryptedSessionKeyBytes.length, CONSTANTS.AesAlgName);
            System.out.println("Decryptor - decrypted session key: " + sessionKey);

            return new KeyMessage(sessionKey, message.getType(), algorithm);

        }

        return contentMessage;
    }

    public static Key decryptKey(ContentMessage message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {

        Algorithm algorithm = message.getAlgorithm();
        String type = algorithm.getEncryptionType();
        System.out.println("Decryptor - Decrypt algorithm: " + type);
        Key result = null;

        if (algorithm != null) {

            Cipher decryptCipher = Cipher.getInstance(algorithm.getEncryptionType());
            decryptCipher.init(Cipher.DECRYPT_MODE, key, algorithm.getIvParameter());
            byte[] decryptedKeyBytes = decryptCipher.doFinal(Base64.getDecoder().decode(message.getContent()));

            KeyFactory keyFactory = KeyFactory.getInstance(CONSTANTS.RsaAlgName);
            if (type.equals(MessageType.PRIVATE_KEY)) {
                result = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedKeyBytes));
            } else if (type.equals(MessageType.PUBLIC_KEY)) {
                result = keyFactory.generatePublic(new X509EncodedKeySpec(decryptedKeyBytes));
            }

            System.out.println("Decryptor - decrypted key: " + result);
        }

        return result;
    }

    public static Map<Integer, byte[]> decryptFile(Message message, Key key) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException, BadPaddingException, IllegalBlockSizeException {

        Map<Integer, byte[]> result = new HashMap<>();
        FileMessage fileMessage = null;
        if (message instanceof FileMessage)
            fileMessage = (FileMessage) message;

        Algorithm algorithm = message.getAlgorithm();

        if (!App.authorized) {
            return fileMessage.getFileBytes();
        }

        if (message.getType().equals(MessageType.FILE)) {
            Cipher encryptCipher = Cipher.getInstance(message.getAlgorithm().getEncryptionType());
            if (message.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgECBMode)) {
                encryptCipher.init(Cipher.DECRYPT_MODE, key);
            } else if (message.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgCBCMode)) {
                encryptCipher.init(Cipher.DECRYPT_MODE, key, algorithm.getIvParameter());
            }

            for (Map.Entry<Integer, byte[]> entry : fileMessage.getFileBytes().entrySet()) {
                result.put(entry.getKey(), encryptCipher.doFinal(entry.getValue()));
            }

            System.out.println("Decryptor - decrypted file block size: " + result.size());
            fileMessage.deleteFileFromDisk();
        }

        return result;
    }
}
