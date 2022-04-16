package bsk.project.server;

import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;
import bsk.project.Messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
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

            while(true) {
                Message mess = (Message) ois.readObject();
                if (mess != null) {
                    if (mess instanceof ContentMessage) {
                        System.out.println("Received mess: " + ((ContentMessage) mess).getContent());
                    } else if (mess instanceof KeyMessage) {
                        System.out.println("Received key: " + ((KeyMessage) mess).getKey());
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
