package bsk.project.server;

import bsk.project.Message;

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

            int x = 100;

            while(true) {
                Message mess = (Message) ois.readObject();
                if (mess != null) {
                    System.out.println("Received mess: " + mess.getContent());
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
