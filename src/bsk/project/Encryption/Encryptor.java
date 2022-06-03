package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.util.Base64;

public class Encryptor {

    public static ContentMessage encryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {

        System.out.println("Encrypt algorithm: " + message.getAlgorithm().getEncryptionType());
        ContentMessage result = null;

        if (message.getType().equals(Message.MessageType.TEXT)) {
            result = (ContentMessage) message;
            Cipher cipher = setupCipher(message.getAlgorithm(), key, Cipher.ENCRYPT_MODE);
            byte[] cipherText = cipher.doFinal(result.getContent().getBytes());
            String encryptedMessageContent = Base64.getEncoder().encodeToString(cipherText);

            result.setContent(encryptedMessageContent);
            System.out.println("Encryptor - encrypted message: " + encryptedMessageContent);

        } else if (message.getType().equals(Message.MessageType.SESSION_KEY)) {
            Cipher ciper = setupCipher(message.getAlgorithm(), key, Cipher.ENCRYPT_MODE);
            byte[] sessionKeyBytes = ((KeyMessage) message).getKey().getEncoded();
            byte[] encryptedSessionKeyBytes = ciper.doFinal(sessionKeyBytes);
            String encryptedSessionKeyString = Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);

            result = new ContentMessage(encryptedSessionKeyString, message.getType(), message.getAlgorithm());
            System.out.println("Encryptor - encrypted session key: " + encryptedSessionKeyString);
        }

        return result;
    }

    public static FileMessage encryptFile(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        System.out.println("Encrypt algorithm: " + message.getAlgorithm().getEncryptionType());
        FileMessage result = null;

        if (message.getType().equals(Message.MessageType.FILE)) {
            FileMessage fileMessage = (FileMessage) message;
            Cipher ciper = setupCipher(message.getAlgorithm(), key, Cipher.ENCRYPT_MODE);
            byte[] encryptedFileBytes = ciper.doFinal(fileMessage.getFileBytes());

            result = new FileMessage(fileMessage.getFileName(), encryptedFileBytes, fileMessage.getPartNumber(),
                    fileMessage.getAllPartsNumber(), fileMessage.getType(), fileMessage.getAlgorithm());
            System.out.println("Encryptor - encrypted file: " + fileMessage.getFileName());
        }

        return result;
    }

    public static byte[] encryptKey(KeyMessage keyMessage, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        System.out.println("Encryptor - Encrypt algorithm: " + keyMessage.getAlgorithm().getEncryptionType());
        Cipher ciper = setupCipher(keyMessage.getAlgorithm(), key, Cipher.ENCRYPT_MODE);
        byte [] result = ciper.doFinal(keyMessage.getKey().getEncoded());
        System.out.println("Encryptor - Encrypted key: " + result);

        return result;
    }

    public static Cipher setupCipher(Algorithm algorithm, Key key, int cipherMode) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(algorithm.getEncryptionType());
        if (algorithm.getEncryptionType().equals(CONSTANTS.AesAlgCBCMode))
            cipher.init(cipherMode, key, algorithm.getIvParameter());
        else
            cipher.init(cipherMode, key);

        return cipher;
    }
}
