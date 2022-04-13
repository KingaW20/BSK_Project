package bsk.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class App {
    private JPanel window;
    private JButton sendButton;
    private JTextArea input;
    private JTextArea communication;
    private JScrollPane scrollCommunication;
    private JLabel messageLabel;
    private static App singleton;

    private static ArrayList<Message> clientMessages;

    public App() {
        singleton = this;
        clientMessages = new ArrayList<>();
        communication.setLineWrap(true);
        input.setLineWrap(true);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(null, "Hello World");
                String messageContent = input.getText();
                communication.append("Me: " + messageContent + "\n");
                input.setText("");

                clientMessages.add(new Message(messageContent, Message.MessageType.TEXT));
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
            Thread sender = new Thread(new ClientSender(server));
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

    public static ArrayList<Message> getMessages() {
        return clientMessages;
    }

    public static void setMessage(Message mess) {
        System.out.println("Received: " + mess.getContent());
        singleton.communication.append("You: " + mess.getContent() + "\n");
    }
}
