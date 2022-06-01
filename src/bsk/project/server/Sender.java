package bsk.project.server;

import bsk.project.Messages.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Sender implements Runnable {
    private Socket socket;
    private boolean firstClient;
    private ArrayList<Message> messages;
    private String userName;

    public Sender(Socket socket, boolean firstClient) {
        this.socket = socket;
        this.firstClient = firstClient;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            boolean userNameSended = false;
            while (!userNameSended) {
                userName = Server.getUserName(firstClient);
                if (userName != null) {
                    oos.writeObject(userName);
                    userNameSended = true;
                    System.out.println("User name send: " + userName);
                }
            }

            while(true) {
                messages = Server.getMessagesFrom(!firstClient);
                if (messages.size() > 0) {
                    Message mess = messages.remove(0);
                    oos.writeObject(mess);

                    if (mess instanceof ContentMessage) {
                        System.out.println("Message send: " + ((ContentMessage) mess).getContent());
                    } else if (mess instanceof KeyMessage) {
                        System.out.println("Key send: " + ((KeyMessage) mess).getKey());
                    } else if (mess instanceof FileMessage) {
                        System.out.println("File send: " + ((FileMessage) mess).getFile());
                    }

                    oos.reset();
                    oos.flush();
                    Server.setMessFrom(!firstClient, null);
                }
            }

//        oos.close();
//        Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
