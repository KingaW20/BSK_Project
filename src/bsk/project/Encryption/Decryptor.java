package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.ContentMessage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Decryptor {

    public static ContentMessage decryptMessage(ContentMessage message, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (message.getType() == ContentMessage.MessageType.TEXT) {
            String algorithm = message.getEncryptionMode();
            System.out.println("Decrypt algorithm: " + algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm.equals(CONSTANTS.AesAlgECBMode)) {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } else if (algorithm.equals(CONSTANTS.AesAlgCBCMode)) {
                try {
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }

            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(message.getContent()));
            message.setContent(new String(plainText));
        }

        return message;
    }
}
