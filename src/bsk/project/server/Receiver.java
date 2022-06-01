package bsk.project.server;

import bsk.project.CONSTANTS;
import bsk.project.Messages.*;

import java.io.*;
import java.net.*;

public class Receiver implements Runnable {
    private Socket socket;
    private boolean firstClient;

    public Receiver(Socket socket, boolean firstClient) {
        this.socket = socket;
        this.firstClient = firstClient;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            boolean userNameReceived = false;
            while (!userNameReceived) {
                String userName = (String) ois.readObject();
                if (userName != null) {
                    Server.setUserName(!firstClient, userName);
                    System.out.println("Received user name: " + userName);
                    userNameReceived = true;
                }
            }

            while(true) {
                Message mess = (Message) ois.readObject();
                if (mess != null) {
                    if (mess instanceof ContentMessage) {
                        System.out.println("Received mess: " + ((ContentMessage) mess).getContent());
                    } else if (mess instanceof KeyMessage) {
                        System.out.println("Received key: " + ((KeyMessage) mess).getKey());
                    } else if (mess instanceof FileMessage) {
                        System.out.println("Received file: " + ((FileMessage) mess).getFile());
                    }

                    Server.setMessFrom(firstClient, mess);
                }
            }

//        ois.close();
//        Thread.currentThread().interrupt();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
