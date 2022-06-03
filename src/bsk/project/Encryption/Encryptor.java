package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.util.Base64;

public class Encryptor {

    public static ContentMessage encryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, IOException {

        String algorithmType = message.getAlgorithm().getEncryptionType();
        System.out.println("Encrypt algorithm: " + algorithmType);
        ContentMessage result = null;
        if (message instanceof ContentMessage)
            result = (ContentMessage) message;

        if (algorithmType != null) {
            if (message.getType().equals(Message.MessageType.TEXT)) {

                Cipher cipher = Cipher.getInstance(algorithmType);

                if (algorithmType.equals(CONSTANTS.AesAlgECBMode)) {
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                } else if (algorithmType.equals(CONSTANTS.AesAlgCBCMode)) {
                    cipher.init(Cipher.ENCRYPT_MODE, key, result.getAlgorithm().getIvParameter());
                }

                byte[] cipherText = cipher.doFinal(result.getContent().getBytes());
                String encryptedMessageContent = Base64.getEncoder().encodeToString(cipherText);
                result.setContent(encryptedMessageContent);
                System.out.println("Encryptor - encrypted message: " + encryptedMessageContent);

            } else if (message.getType().equals(Message.MessageType.SESSION_KEY)) {
                KeyMessage keyMessage = null;
                if (message instanceof KeyMessage)
                    keyMessage = (KeyMessage) message;

                Cipher encryptCipher = Cipher.getInstance(message.getAlgorithm().getEncryptionType());
                encryptCipher.init(Cipher.ENCRYPT_MODE, key);

                byte[] sessionKeyBytes = keyMessage.getKey().getEncoded();
                byte[] encryptedSessionKeyBytes = encryptCipher.doFinal(sessionKeyBytes);
                String encryptedSessionKeyString = Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);

                result = new ContentMessage(encryptedSessionKeyString, message.getType(), message.getAlgorithm());
                System.out.println("Encryptor - encrypted session key: " + encryptedSessionKeyString);
            }
        }

        return result;
    }

    public static FileMessage encryptFile(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        String algorithmType = message.getAlgorithm().getEncryptionType();
        System.out.println("Encrypt algorithm: " + algorithmType);
        FileMessage result = null;
        if (message instanceof FileMessage)
            result = (FileMessage) message;

        if (algorithmType != null) {
            if (message.getType().equals(Message.MessageType.FILE)) {
                FileMessage fileMessage = null;
                if (message instanceof FileMessage)
                    fileMessage = (FileMessage) message;

                Cipher encryptCipher = Cipher.getInstance(message.getAlgorithm().getEncryptionType());
                if (message.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgECBMode))
                    encryptCipher.init(Cipher.ENCRYPT_MODE, key);
                else if (message.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgCBCMode))
                    encryptCipher.init(Cipher.ENCRYPT_MODE, key, message.getAlgorithm().getIvParameter());

                byte[] encryptedFileBytes = encryptCipher.doFinal(fileMessage.getFileBytes());

                result = new FileMessage(fileMessage.getFileName(), encryptedFileBytes, fileMessage.getPartNumber(),
                        fileMessage.getAllPartsNumber(), fileMessage.getType(), fileMessage.getAlgorithm());
                System.out.println("Encryptor - encrypted file: " + fileMessage.getFileName());
            }
        }

        return result;
    }

    public static byte[] encryptKey(KeyMessage keyMessage, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Algorithm algorithm = keyMessage.getAlgorithm();
        System.out.println("Encryptor - Encrypt algorithm: " + algorithm.getEncryptionType());
        byte[] result = null;

        if (algorithm != null) {
            Cipher encryptCipher = Cipher.getInstance(algorithm.getEncryptionType());

            if (keyMessage.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgECBMode))
                encryptCipher.init(Cipher.ENCRYPT_MODE, key);
            else if (keyMessage.getAlgorithm().getEncryptionType().equals(CONSTANTS.AesAlgCBCMode))
                encryptCipher.init(Cipher.ENCRYPT_MODE, key, keyMessage.getAlgorithm().getIvParameter());

            result = encryptCipher.doFinal(keyMessage.getKey().getEncoded());
            System.out.println("Encryptor - Encrypted key: " + result);
        }

        return result;
    }
}
