package bsk.project;

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

    private static ArrayList<ContentMessage> clientMessages;

    public App() {
        singleton = this;
        clientMessages = new ArrayList<>();
        communication.setLineWrap(true);
        input.setLineWrap(true);
        try {
            clientData = new ClientData(true);
            System.out.println("Key: " + clientData.getSessionKey().toString());
            clientData2 = new ClientData(false);
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
                    encryptMessage(new ContentMessage(messageContent, ContentMessage.MessageType.TEXT, encryptionMode));
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
            runProtocol();
        } catch(IOException ex) {
            System.err.println(ex);
        }

    }

    public static void runProtocol() {
        while(true) {}
    }

    public static ArrayList<ContentMessage> getMessages() {
        return clientMessages;
    }

    public static void setMessage(ContentMessage mess) {
        try {
            if (mess.getType() == ContentMessage.MessageType.TEXT) {
                singleton.communication.append("You encrypted: " + mess.getContent() + "\n");
                singleton.communication.append("You decrypted: " + decryptMessage(mess).getContent() + "\n");
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

    private static void encryptMessage(ContentMessage message)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        String algorithm = message.getEncryptionMode();

        if (algorithm != null) {
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm == CONSTANTS.AesAlgECBMode) {
                cipher.init(Cipher.ENCRYPT_MODE, clientData.getSessionKey());
            } else if (algorithm == CONSTANTS.AesAlgCBCMode) {
                cipher.init(Cipher.ENCRYPT_MODE, clientData.getSessionKey(), clientData.getIvParameter());
            }
            System.out.println("Encrypt algorithm: " + algorithm);

            byte[] cipherText = cipher.doFinal(message.getContent().getBytes());
            String encryptedMessageContent = Base64.getEncoder().encodeToString(cipherText);
            message.setContent(encryptedMessageContent);
            clientMessages.add(message);
        }
    }

    private static ContentMessage decryptMessage(ContentMessage message)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (message.getType() == ContentMessage.MessageType.TEXT) {
            String algorithm = message.getEncryptionMode();
            System.out.println("Decrypt algorithm: " + algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm.equals(CONSTANTS.AesAlgECBMode)) {
                cipher.init(Cipher.DECRYPT_MODE, clientData2.getSessionKey());
            } else if (algorithm.equals(CONSTANTS.AesAlgCBCMode)) {
                try {
                    cipher.init(Cipher.DECRYPT_MODE, clientData2.getSessionKey(), clientData2.getIvParameter());
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }

            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(message.getContent()));
            message.setContent(new String(plainText));
            return message;
        }

        return null;
    }
}
