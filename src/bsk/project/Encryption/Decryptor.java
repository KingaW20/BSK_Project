package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class Decryptor {

    public static Message decryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        ContentMessage contentMessage = null;
        if (message instanceof ContentMessage)
            contentMessage = (ContentMessage) message;
        Algorithm algorithm = message.getAlgorithm();

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
}
