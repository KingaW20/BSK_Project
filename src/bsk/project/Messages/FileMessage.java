package bsk.project.Messages;

import bsk.project.CONSTANTS;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileMessage extends Message implements Serializable {

    private File file;
    private String fileName;
    private Map<Integer, byte[]> fileBytes;

    public FileMessage(File file, String fileName, MessageType type, Algorithm algorithm,
                       Map<Integer, byte[]> fileBytes) throws IOException {
        super(type, algorithm);
        this.file = file;
        this.fileName = fileName;
        if (fileBytes != null)
            this.fileBytes = fileBytes;
        else
            this.fileBytes = splitReadFile();
    }

    public File getFile() { return this.file; }

    public String getFileName() { return this.fileName; }

    public Map<Integer, byte[]> getFileBytes() { return fileBytes; }

    public byte[] getFileBytesByIndex(int index) { return fileBytes.get(index); }

    public void setFile(File file) { this.file = file; }

    public void setMessage(FileMessage fileMessage) throws IOException {
        this.file = fileMessage.getFile();
        this.type = fileMessage.getType();
        this.fileBytes.put(0, Files.readAllBytes(file.toPath()));
    }

    public void deleteFileFromDisk() {
        File file = new File(fileName);
        if (file.delete()) {
            System.out.println("Deleted the file: " + fileName);
        } else {
            System.out.println("Failed to delete the file: " + fileName);
        }
    }

    public Map<Integer, byte[]> splitReadFile() throws IOException {
        Map<Integer, byte[]> result = new HashMap<>();
        int x = 0;

        double fileLength = file.length();
        double remainingFileLength = fileLength;
        double partsNumber = Math.ceil(fileLength / CONSTANTS.partFileMaxLength);
        FileInputStream fileInputStream = new FileInputStream(file);

        for (int i = 0; i < partsNumber; i++) {
            byte[] inputBytes = new byte[(int)(remainingFileLength >= CONSTANTS.partFileMaxLength ?
                    CONSTANTS.partFileMaxLength : remainingFileLength)];
            fileInputStream.read(inputBytes);
            result.put(i, inputBytes);
            remainingFileLength -= CONSTANTS.partFileMaxLength;
            x++;
        }
        System.out.println("Parts: " + x);

        return result;
    }

    public static File saveFile(String path, Map<Integer, byte[]> fileBytes) throws IOException {
        File file = new File(path);
        OutputStream out = new FileOutputStream(file);

        // write to file all parties
        for (Map.Entry<Integer, byte[]> entry : fileBytes.entrySet()) {
            out.write(entry.getValue());
        }

        out.close();
        return file;
    }
}
