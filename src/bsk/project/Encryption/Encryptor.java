package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.ContentMessage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encryptor {

    public static ContentMessage encryptMessage(ContentMessage message, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        String algorithm = message.getEncryptionMode();

        if (algorithm != null) {
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm == CONSTANTS.AesAlgECBMode) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else if (algorithm == CONSTANTS.AesAlgCBCMode) {
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            }
            System.out.println("Encrypt algorithm: " + algorithm);

            byte[] cipherText = cipher.doFinal(message.getContent().getBytes());
            String encryptedMessageContent = Base64.getEncoder().encodeToString(cipherText);
            message.setContent(encryptedMessageContent);
        }

        return message;
    }
}
