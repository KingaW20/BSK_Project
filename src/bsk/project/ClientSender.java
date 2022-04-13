package bsk.project;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSender implements Runnable {
    private static ArrayList<Message> clientMessages;
    private Socket server;

    public ClientSender(Socket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
            //ObjectInputStream ois = new ObjectInputStream(server.getInputStream());

            while (true) {
                clientMessages = App.getMessages();
                if (clientMessages.size() > 0) {
                    Message mess = clientMessages.remove(0);
                    oos.writeObject(new Message(mess.getContent(), mess.ifFile(), mess.ifReadyToSend()));
                    System.out.println("Send");
                    oos.reset();
                    oos.flush();
                }
            }

//        ois.close();
//        oos.close();
//        Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
