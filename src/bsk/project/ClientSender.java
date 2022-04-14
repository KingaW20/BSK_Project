package bsk.project;

import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSender implements Runnable {
    private static ArrayList<ContentMessage> clientMessages;
    private Socket server;
    private ClientData clientData;

    public ClientSender(Socket server, ClientData clientData) {
        this.server = server;
        this.clientData = clientData;

    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());

            sendSessionKey(oos);

            while (true) {
                clientMessages = App.getMessages();
                if (clientMessages.size() > 0) {
                    ContentMessage mess = clientMessages.remove(0);
                    oos.writeObject(mess);
                    System.out.println("Send message: " + mess.getContent());
                    oos.reset();
                    oos.flush();
                }
            }

//        oos.close();
//        Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSessionKey(ObjectOutputStream oos) {
        try {
            oos.writeObject(new KeyMessage(clientData.getSessionKey(), clientData.getSessionKeySize(),
                    clientData.getIv(), ContentMessage.MessageType.SESSION_KEY, null));
            System.out.println("Key sended");
            oos.reset();
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
