package bsk.project.Messages;

import bsk.project.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;

public class FileMessage extends Message implements Serializable {

    private File file;
    private String fileName;
    private byte[] fileBytes;
    private int partNumber;
    private double allPartsNumber;

    public FileMessage(String fileName, byte[] fileBytes, int partNumber, double allPartsNumber,
                       MessageType type, Algorithm algorithm) {
        super(type, algorithm);
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.partNumber = partNumber;
        this.allPartsNumber = allPartsNumber;
    }

    public File getFile() { return this.file; }

    public String getFileName() { return this.fileName; }

    public byte[] getFileBytes() { return this.fileBytes; }

    public int getPartNumber() { return this.partNumber; }

    public double getAllPartsNumber() { return this.allPartsNumber; }

    public void setFile(File file) { this.file = file; }

    public static void splitAndAddPartsToSend(File file, String encryptionMode, IvParameterSpec ivParam)
            throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        double remainingFileLength = file.length();
        double partsNumber = Math.ceil(file.length() / CONSTANTS.partFileMaxLength);
        FileInputStream fileInputStream = new FileInputStream(file);

        for (int i = 0; i < partsNumber; i++) {
            byte[] inputBytes = new byte[(int)(Math.min(remainingFileLength, CONSTANTS.partFileMaxLength))];
            fileInputStream.read(inputBytes);

            App.getMessages().add(App.getEncryptor().encryptFile(
                    new FileMessage(file.getName(), inputBytes, i, partsNumber, MessageType.FILE,
                            new Algorithm(encryptionMode, CONSTANTS.sessionKeySize, ivParam)),
                    App.clientData.getSessionKey()));

            remainingFileLength -= CONSTANTS.partFileMaxLength;
        }
        System.out.println("splitAndAddPartsToSend - Parts number: " + partsNumber);
    }
}
