package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;

import javax.crypto.*;
import java.security.*;
import java.util.Base64;

public class Encryptor {

    public static ContentMessage encryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        String algorithm = message.getAlgorithm().getEncryptionType();
        System.out.println("Encrypt algorithm: " + algorithm);
        ContentMessage result = null;
        if (message instanceof ContentMessage)
            result = (ContentMessage) message;

        if (algorithm != null) {
            if (message.getType().equals(Message.MessageType.TEXT)) {

                Cipher cipher = Cipher.getInstance(algorithm);

                if (algorithm.equals(CONSTANTS.AesAlgECBMode)) {
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                } else if (algorithm.equals(CONSTANTS.AesAlgCBCMode)) {
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
}
