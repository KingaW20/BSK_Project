package bsk.project;

import bsk.project.Encryption.Decryptor;
import bsk.project.Encryption.Encryptor;
import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;

import javax.crypto.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

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
    private static ClientData clientData;
    private static ClientData clientData2;
    private static Encryptor encryptor;
    private static Decryptor decryptor;

    private static ArrayList<ContentMessage> clientMessages;

    public App() {
        singleton = this;
        clientMessages = new ArrayList<>();
        communication.setLineWrap(true);
        input.setLineWrap(true);
        encryptor = new Encryptor();
        decryptor = new Decryptor();
        try {
            clientData = new ClientData(true, CONSTANTS.sessionKeySize);
            System.out.println("Key: " + clientData.getSessionKey().toString());
            clientData2 = new ClientData(false, CONSTANTS.sessionKeySize);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(null, "Hello World");
                String messageContent = input.getText();
                communication.append("Me: " + messageContent + "\n");
                input.setText("");

                String encryptionMode = CONSTANTS.AesAlgECBMode;
                if (cbcMode.isSelected()) encryptionMode = CONSTANTS.AesAlgCBCMode;

                try {
                    clientMessages.add(encryptor.encryptMessage(
                            new ContentMessage(messageContent, ContentMessage.MessageType.TEXT, encryptionMode),
                            clientData.getSessionKey(), clientData.getIvParameter()));
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                        InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(0, 0);
        frame.setContentPane(new App().window);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try(Socket server = new Socket(CONSTANTS.ipAddr, CONSTANTS.port)) {
            Thread sender = new Thread(new ClientSender(server, clientData));
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

    public static void setMessage(ContentMessage mess) {
        try {
            if (mess.getType() == ContentMessage.MessageType.TEXT) {
                singleton.communication.append("You encrypted: " + mess.getContent() + "\n");
                singleton.communication.append("You decrypted: " + decryptor.decryptMessage(
                        mess, clientData2.getSessionKey(), clientData2.getIvParameter()).getContent() + "\n");
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public static void setKeyMessage(KeyMessage mess) {
        if (mess.getType() == ContentMessage.MessageType.SESSION_KEY) {
            clientData2.setSessionKey(mess.getKey());
            clientData2.setIv(mess.getIv());
        }
    }
}
