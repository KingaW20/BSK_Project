package bsk.project;

import bsk.project.Encryption.*;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
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
    private static App singleton;
    private static String userName;
    private static ClientData clientData;
    private static ClientData clientData2;
    private static Decryptor decryptor;

    private static Encryptor encryptor;
    private static ArrayList<ContentMessage> clientMessages;

    public App() {
        singleton = this;
        clientMessages = new ArrayList<>();
        communication.setLineWrap(true);
        input.setLineWrap(true);
        encryptor = new Encryptor();
        decryptor = new Decryptor();
        try {
            clientData = new ClientData(userName, true, CONSTANTS.sessionKeySize, CONSTANTS.keyPairSize);
            clientData2 = new ClientData(null, false, CONSTANTS.sessionKeySize, CONSTANTS.keyPairSize);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException |
                IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageContent = input.getText();
                communication.append(clientData.getUserName() + ": " + messageContent + "\n");
                input.setText("");

                String encryptionMode = CONSTANTS.AesAlgECBMode;
                IvParameterSpec ivParam = null;
                if (cbcMode.isSelected()) {
                    encryptionMode = CONSTANTS.AesAlgCBCMode;
                    ivParam = clientData.getIvParameter();
                }

                try {
                    clientMessages.add(encryptor.encryptMessage(
                            new ContentMessage(messageContent, ContentMessage.MessageType.TEXT,
                                    new Algorithm(encryptionMode, CONSTANTS.sessionKeySize, ivParam)),
                            clientData.getSessionKey()));
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
        logInPanelFrame.setSize(300, 200);
        logInPanelFrame.setResizable(false);
        LogInPanel logInPanel = new LogInPanel(logInPanelFrame);
        logInPanelFrame.setContentPane(logInPanel.logInPanel);

        while(userName == null) { userName = logInPanel.getUserName(); }
        logInPanelFrame.dispose();

        JFrame frame = new JFrame("App " + userName);
        frame.setSize(new Dimension(800, 600));
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

    public static ArrayList<ContentMessage> getMessages() {
        return clientMessages;
    }

    public static Encryptor getEncryptor() {
        return encryptor;
    }

    public static void setUserNameMessage(String name) {
        clientData2.setUserName(name);
    }

    public static void setMessage(ContentMessage mess) {
        try {
            if (mess.getType().equals(MessageType.TEXT)) {
//                singleton.communication.append(clientData2.getUserName() + " encrypted: " + mess.getContent() + "\n");
//                singleton.communication.append(clientData2.getUserName() + " decrypted: " +
//                        ((ContentMessage)decryptor.decryptMessage(
//                                mess, clientData2.getSessionKey())).getContent() + "\n");
                singleton.communication.append(clientData2.getUserName() + ": " +
                        ((ContentMessage)decryptor.decryptMessage(
                                mess, clientData2.getSessionKey())).getContent() + "\n");
            } else if (mess.getType().equals(MessageType.SESSION_KEY)) {
                clientData2.setSessionKey((SecretKey) ((KeyMessage)
                        decryptor.decryptMessage(mess, clientData.getPrivateKey())).getKey());
                byte[] iv2 = mess.getAlgorithm().getIv();
                System.out.println("App - iv received: " + iv2);
                clientData2.setIv(mess.getAlgorithm().getIv());
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public static void setKeyMessage(KeyMessage mess) {
        if (mess.getType() == MessageType.PUBLIC_KEY) {
            clientData2.setPrivatePublicKey(null, (PublicKey) mess.getKey());
        }
    }
}
