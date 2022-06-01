package bsk.project.Encryption;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class Decryptor {

    public static Message decryptMessage(Message message, Key key)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
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

        } else if (message.getType().equals(MessageType.FILE)) {

            FileMessage fileMessage = null;
            if (message instanceof FileMessage)
                fileMessage = (FileMessage) message;

            Cipher encryptCipher = Cipher.getInstance(message.getAlgorithm().getEncryptionType());
            encryptCipher.init(Cipher.DECRYPT_MODE, key);

            Files.write(fileMessage.getFile().toPath(), fileMessage.getFileBytes());
            FileInputStream inputStream = new FileInputStream(fileMessage.getFile());
            byte[] inputBytes = new byte[(int) fileMessage.getFile().length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = encryptCipher.doFinal(inputBytes);

            File outputFile = new File(fileMessage.getFileName());
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

            System.out.println("Decryptor - decrypted file: " + outputFile);
            FileMessage result = new FileMessage(
                    outputFile, fileMessage.getFileName(), message.getType(), message.getAlgorithm());
            result.deleteFileFromDisk();
            return result;
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
}
