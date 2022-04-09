package bsk.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class App {
    private JPanel window;
    private JButton sendButton;
    private JTextArea input;
    private JTextArea communication;
    private JScrollPane scrollCommunication;
    private JLabel messageLabel;

    private static ArrayList<Message> clientMessages;

    public App(Frame frame) {
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

                //TODO: send message to another person
                clientMessages.add(new Message(messageContent, false, true));
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(0, 0);
        frame.setContentPane(new App(frame).window);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Thread clientThread = new Thread(new Client());
        clientThread.start();

        //runProtocol();
    }

//    public static void runProtocol() {
//        while(true) {
//            System.out.println("Size: " + clientMessages.size());
//        }
//    }

    public static ArrayList<Message> getMessages() {
        return clientMessages;
    }
}
