package bsk.project.Messages;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends Message implements Serializable {

    private File file;
    private String fileName;
    private byte[] fileBytes;

    public FileMessage(File file, String fileName, MessageType type, Algorithm algorithm) throws IOException {
        super(type, algorithm);
        this.file = file;
        this.fileName = fileName;
        this.fileBytes = Files.readAllBytes(file.toPath());
    }

    public File getFile() { return this.file; }

    public String getFileName() { return this.fileName; }

    public byte[] getFileBytes() { return fileBytes; }

    public void setFile(File file) { this.file = file; }

    public void setMessage(FileMessage fileMessage) throws IOException {
        this.file = fileMessage.getFile();
        this.type = fileMessage.getType();
        this.fileBytes = Files.readAllBytes(file.toPath());
    }

    public void deleteFileFromDisk() {
        File file = new File(fileName);
        if (file.delete()) {
            System.out.println("Deleted the file: " + fileName);
        } else {
            System.out.println("Failed to delete the file: " + fileName);
        }
    }
}
