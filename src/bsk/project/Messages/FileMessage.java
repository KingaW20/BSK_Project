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
                       Algorithm algorithm) {
        super(MessageType.FILE, algorithm);
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

    public void setFilesBytes(byte[] fileBytes) { this.fileBytes = fileBytes; }

    public void setPartNumber(int partNumber) { this.partNumber = partNumber; }

    public static void splitAndAddPartsToSend(File file, String encryptionMode, IvParameterSpec ivParam)
            throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        double remainingFileLength = file.length();
        double partsNumber = Math.ceil(file.length() / CONSTANTS.partFileMaxLength);
        FileInputStream fileInputStream = new FileInputStream(file);

        FileMessage fileMessage = new FileMessage(file.getName(), null, 0, partsNumber,
                new Algorithm(encryptionMode, CONSTANTS.sessionKeySize, ivParam));

        App.setSendingProgressBar(0);
        App.setEncryptionProgressBar(0);
        App.startEncryptionTime();
        for (int i = 0; i < partsNumber; i++) {
            byte[] inputBytes = new byte[(int)(Math.min(remainingFileLength, CONSTANTS.partFileMaxLength))];
            fileInputStream.read(inputBytes);

            fileMessage.setFilesBytes(inputBytes);
            fileMessage.setPartNumber(i);
            App.getMessages().add(App.getEncryptor().encryptFile(fileMessage, App.clientData.getSessionKey()));

            remainingFileLength -= CONSTANTS.partFileMaxLength;
        }
        App.stopEncryptionTime();
        System.out.println("splitAndAddPartsToSend - Parts number: " + partsNumber);
    }
}
