package bsk.project.server;

import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;
import bsk.project.Messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Sender implements Runnable {
    private Socket socket;
    private boolean firstClient;
    private ArrayList<Message> messages;

    public Sender(Socket socket, boolean firstClient) {
        this.socket = socket;
        this.firstClient = firstClient;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            while(true) {
                messages = Server.getMessagesFrom(!firstClient);
                if (messages.size() > 0) {
                    Message mess = messages.remove(0);
                    oos.writeObject(mess);

                    if (mess instanceof ContentMessage) {
                        System.out.println("Send message: " + ((ContentMessage) mess).getContent());
                    } else if (mess instanceof KeyMessage) {
                        System.out.println("Key send: " + ((KeyMessage) mess).getKey());
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
