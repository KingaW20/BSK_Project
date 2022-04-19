package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class Encryptor {

    public static ContentMessage encryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        String algorithm = message.getAlgorithm().getEncryptionType();
        System.out.println("Encrypt algorithm: " + algorithm);
        ContentMessage result = null;
        if (message instanceof ContentMessage)
            result = (ContentMessage) message;

        if (algorithm != null) {
            if (message.getType().equals(MessageType.TEXT)) {

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

            } else if (message.getType().equals(MessageType.SESSION_KEY) ||
                    message.getType().equals(MessageType.PUBLIC_KEY) ||
                    message.getType().equals(MessageType.PRIVATE_KEY)) {
                KeyMessage keyMessage = null;
                if (message instanceof KeyMessage)
                    keyMessage = (KeyMessage) message;

                Cipher encryptCipher = Cipher.getInstance(message.getAlgorithm().getEncryptionType());
                if (message.getType().equals(MessageType.SESSION_KEY)) {
                    encryptCipher.init(Cipher.ENCRYPT_MODE, key);
                } else {
                    encryptCipher.init(Cipher.ENCRYPT_MODE, key, message.getAlgorithm().getIvParameter());
                }

                byte[] keyBytes = keyMessage.getKey().toString().getBytes(StandardCharsets.UTF_8);
                byte[] encryptedKeyBytes = encryptCipher.doFinal(keyBytes);
                String encryptedKeyString = Base64.getEncoder().encodeToString(encryptedKeyBytes);

                result = new ContentMessage(encryptedKeyString, message.getType(), message.getAlgorithm());
                System.out.println("Encryptor - encrypted key: " + encryptedKeyString);
            }
        }

        return result;
    }
}
