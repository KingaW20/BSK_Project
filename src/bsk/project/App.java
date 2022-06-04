package bsk.project;

import bsk.project.Encryption.*;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class App {
    private JPanel window;
    private JButton sendButton;
    private JTextArea input;
    private JTextArea communication;
    private JScrollPane scrollCommunication;
    private JLabel messageLabel;
    private JRadioButton ecbMode;
    private JRadioButton cbcMode;
    private JButton addFileButton;
    private JLabel fileName;
    private JProgressBar sendingProgressBar;
    private JLabel sengingProgressText;
    private JLabel encryptionProgressText;
    private JProgressBar encryptionProgressBar;
    private JFileChooser fileChooser = new JFileChooser();
    private String noFileLoaded = "no file added";
    private File file;

    private static App singleton;
    private static String userName;
    public static String userPassword;
    public static boolean authorized = true;
    public static ClientData clientData;
    private static ClientData clientData2;
    private static Decryptor decryptor;

    private static Encryptor encryptor;
    private static ArrayList<Message> clientMessages;
    private static String fileToSavePath;
    private static OutputStream out;

    private static long encryptionTime;
    private static long sendingTime;
    private static long decryptionTime;
    private static long receivingTimeBefore;
    private static long receivingTimeAfter;
    private static long savingTime;

    public App() {
        singleton = this;
        clientMessages = new ArrayList<>();
        communication.setLineWrap(true);
        input.setLineWrap(true);
        singleton.sendingProgressBar.setStringPainted(true);
        singleton.encryptionProgressBar.setStringPainted(true);

        encryptor = new Encryptor();
        decryptor = new Decryptor();
        try {
            clientData = new ClientData(
                    userName, true, CONSTANTS.sessionKeySize, CONSTANTS.keyPairSize);
            clientData2 = new ClientData(
                    null, false, CONSTANTS.sessionKeySize, CONSTANTS.keyPairSize);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException |
                IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        fileName.setText(noFileLoaded);
        file = null;

        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Choose a file to send");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnVal = fileChooser.showOpenDialog(fileChooser.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    System.out.println("Opening: " + file.getName());
                    System.out.println("File length: " + file.length());
                    fileName.setText(file.getName());
                } else {
                    System.out.println("Open command cancelled by user");
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageContent = input.getText();
                File messageFile = file;
                input.setText("");
                fileName.setText(noFileLoaded);
                file = null;

                String encryptionMode = CONSTANTS.AesAlgECBMode;
                IvParameterSpec ivParam = null;
                if (cbcMode.isSelected()) {
                    encryptionMode = CONSTANTS.AesAlgCBCMode;
                    ivParam = clientData.getIvParameter();
                }

                try {
                    if (!messageContent.equals("") && messageContent != null) {
                        communication.append(clientData.getUserName() + ": " + messageContent + "\n");
                        clientMessages.add(encryptor.encryptMessage(
                                new ContentMessage(messageContent, MessageType.TEXT,
                                        new Algorithm(encryptionMode, CONSTANTS.sessionKeySize, ivParam)),
                                clientData.getSessionKey()));
                    }
                    if (messageFile != null) {
                        communication.append(clientData.getUserName() + " send file " + messageFile.getName() + "\n");
                        FileMessage.splitAndAddPartsToSend(messageFile, encryptionMode, ivParam);
                        System.out.println("File size: " + messageFile.length());
                    }
                }
                catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                        InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame logInPanelFrame = new JFrame("App");
        logInPanelFrame.setSize(400, 300);
        logInPanelFrame.setResizable(false);
        LogInPanel logInPanel = new LogInPanel(logInPanelFrame);
        logInPanelFrame.setContentPane(logInPanel.logInPanel);

        while(userName == null)
            userName = logInPanel.getUserName();
        while (userPassword == null)
            userPassword = logInPanel.getUserPassword();
        logInPanelFrame.dispose();

        JFrame frame = new JFrame("App " + userName);

        final JFileChooser fileChooser = new JFileChooser();
        frame.add(fileChooser);
        frame.setSize(new Dimension(900, 650));
        frame.setLocation(0, 0);
        frame.setContentPane(new App().window);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try(Socket server = new Socket(CONSTANTS.ipAddr, CONSTANTS.port)) {
            Thread sender = new Thread(new ClientSender(server, clientData, clientData2));
            Thread receiver = new Thread(new ClientReceiver(server));
            sender.start();
            receiver.start();
            while(true);
        } catch(IOException ex) {
            System.err.println(ex);
        }
    }

    public static ArrayList<Message> getMessages() {
        return clientMessages;
    }

    public static Encryptor getEncryptor() {
        return encryptor;
    }

    public static int getSendingProgressBarValue() {
        return singleton.sendingProgressBar.getValue();
    }

    public static int getEncryptionProgressBarValue() {
        return singleton.encryptionProgressBar.getValue();
    }

    public static void setUserNameMessage(String name) {
        clientData2.setUserName(name);
    }

    public static void setSendingProgressBar(int percent) {
        singleton.sendingProgressBar.setValue(percent);
        singleton.sendingProgressBar.repaint();
        singleton.sendingProgressBar.update(singleton.sendingProgressBar.getGraphics());
    }

    public static void setEncryptionProgressBar(int percent) {
        singleton.encryptionProgressBar.setValue(percent);
        singleton.encryptionProgressBar.repaint();
        singleton.encryptionProgressBar.update(singleton.encryptionProgressBar.getGraphics());
    }

    public static void startEncryptionTime() {
        encryptionTime = System.nanoTime();
    }

    public static void startSendingTime() {
        sendingTime = System.nanoTime();
    }

    public static void startDecryptionTime() {
        decryptionTime = System.nanoTime();
    }

    public static void startSavingTime() {
        savingTime = System.nanoTime();
    }

    public static void startReceivingTime() {
        receivingTimeBefore = System.nanoTime();
    }

    public static void stopTemporarilyReceivingTime() {
        receivingTimeBefore = System.nanoTime() - receivingTimeBefore;
    }

    public static void startTemporarilyReceivingTime() {
        receivingTimeAfter = System.nanoTime();
    }

    public static void stopReceivingTime() {
        receivingTimeAfter = System.nanoTime() - receivingTimeAfter + receivingTimeBefore;
        System.out.println("Receiving time: " + receivingTimeAfter / 10e6 + " ms");
    }

    public static void stopEncryptionTime() {
        encryptionTime = System.nanoTime() - encryptionTime;
        System.out.println("Encryption time: " + encryptionTime / 10e6 + " ms");
    }

    public static void stopSendingTime() {
        sendingTime = System.nanoTime() - sendingTime;
        System.out.println("Sending time: " + sendingTime / 10e6 + " ms");
    }

    public static void stopDecryptionTime() {
        decryptionTime = System.nanoTime() - decryptionTime;
        System.out.println("Decryption time: " + decryptionTime / 10e6 + " ms");
    }

    public static void stopSavingTime() {
        savingTime = System.nanoTime() - savingTime;
        System.out.println("Saving time: " + savingTime / 10e6 + " ms");
    }

    public static void setMessage(Message mess) {
        try {
            if (mess instanceof ContentMessage && mess.getType().equals(MessageType.TEXT)) {

                singleton.communication.append(clientData2.getUserName() + ": " +
                        ((ContentMessage)decryptor.decryptMessage(
                                mess, clientData2.getSessionKey())).getContent() + "\n");

            } else if (mess instanceof ContentMessage && mess.getType().equals(MessageType.SESSION_KEY)) {

                clientData2.setSessionKey((SecretKey) ((KeyMessage)
                        decryptor.decryptMessage(mess, clientData.getPrivateKey())).getKey());
                byte[] iv2 = mess.getAlgorithm().getIv();
                System.out.println("App - iv received: " + iv2);
                clientData2.setIv(mess.getAlgorithm().getIv());

            } else if (mess instanceof FileMessage) {

                FileMessage file = (FileMessage) mess;
                if (file.getPartNumber() == 0)
                    startDecryptionTime();
                byte[] decryptedMessage = decryptor.decryptFile(mess, clientData2.getSessionKey());
                if (file.getPartNumber() == (int)file.getAllPartsNumber() - 1)
                    stopDecryptionTime();

                checkIfAllPartsReceived(decryptedMessage, file.getFileName(),file.getPartNumber(),
                        file.getAllPartsNumber());
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException | InvalidAlgorithmParameterException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkIfAllPartsReceived(byte[] decryptedMessage, String fileName,
                                               int partNumber, double allPartsNumber) throws IOException {

        // first part of file -> select place to save
        if (partNumber == 0) {
            stopTemporarilyReceivingTime();
            singleton.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            singleton.fileChooser.setDialogTitle("Specify a directory to save received file");
            int userSelection = singleton.fileChooser.showDialog(
                    singleton.fileChooser.getParent(), "Save");

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = singleton.fileChooser.getSelectedFile();
                fileToSavePath = fileToSave.getAbsolutePath() + "/" + fileName;
            }

            File file = new File(fileToSavePath);
            out = new FileOutputStream(file);
            startTemporarilyReceivingTime();
            startSavingTime();
        }

        // save file part
        out.write(decryptedMessage);

        if (partNumber == (int)allPartsNumber - 1) {
            stopSavingTime();
            System.out.println("Save as file: " + fileToSavePath);
            fileToSavePath = null;
            singleton.communication.append(clientData2.getUserName() + " send file " + fileName + "\n");
            out.close();
        }
    }

    public static void setKeyMessage(KeyMessage mess) {
        if (mess.getType() == MessageType.PUBLIC_KEY)
            clientData2.setPrivatePublicKey(null, (PublicKey) mess.getKey());
    }
}
