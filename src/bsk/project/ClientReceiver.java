package bsk.project;

import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;
import bsk.project.Messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientReceiver implements Runnable {
    private Socket server;

    public ClientReceiver(Socket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(server.getInputStream());

            getPublicKey(ois);
            getSessionKey(ois);
            getMessages(ois);

//        ois.close();
//        Thread.currentThread().interrupt();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getPublicKey(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        boolean keyMessageExists = false;

        while (!keyMessageExists) {
            KeyMessage keyMessage = (KeyMessage) ois.readObject();
            if (keyMessage != null) {
                App.setKeyMessage(keyMessage);
                keyMessageExists = true;
                System.out.println("Public key received: " + keyMessage.getKey().toString());
            }
        }
    }

    private void getSessionKey(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        boolean keyMessageExists = false;
        boolean keyMessageExists2 = false;

        //TODO: delete
        while (!keyMessageExists) {
            KeyMessage keyMessage = (KeyMessage) ois.readObject();
            if (keyMessage != null) {
                App.setKeyMessage(keyMessage);
                keyMessageExists = true;
                System.out.println("Session key received: " + keyMessage.getKey().toString());
            }
        }

        while (!keyMessageExists2) {
            ContentMessage keyMessage = (ContentMessage) ois.readObject();
            if (keyMessage != null) {
                if (keyMessage.getType().equals(Message.MessageType.SESSION_KEY)) System.out.println("session key");
                App.setMessage(keyMessage);
                keyMessageExists2 = true;
                System.out.println("Encrypted session key received: " + keyMessage.getContent());
            }
        }
    }

    private void getMessages(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        while(true) {
            ContentMessage mess = (ContentMessage) ois.readObject();
            if (mess != null) {
                System.out.println("Message received: " + mess.getContent());
                App.setMessage(mess);
            }
        }
    }
}

