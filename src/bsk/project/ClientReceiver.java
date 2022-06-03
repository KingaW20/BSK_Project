package bsk.project;

import bsk.project.Messages.*;

import java.io.*;
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

            getUserName(ois);
            getPublicKey(ois);
            getSessionKey(ois);
            getMessages(ois);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getUserName(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        String userName = (String) ois.readObject();
        if (userName != null) {
            App.setUserNameMessage(userName);
            System.out.println("ClientReceiver - user name received: " + userName);
        } else {
            System.out.println("Name empty");
        }
    }

    private void getPublicKey(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        boolean keyMessageExists = false;

        while (!keyMessageExists) {
            KeyMessage keyMessage = (KeyMessage) ois.readObject();
            if (keyMessage != null) {
                App.setKeyMessage(keyMessage);
                keyMessageExists = true;
                System.out.println("ClientReceiver - public key received: " + keyMessage.getKey().toString());
            }
        }
    }

    private void getSessionKey(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        boolean keyMessageExists = false;

        while (!keyMessageExists) {
            ContentMessage keyMessage = (ContentMessage) ois.readObject();
            if (keyMessage != null) {
                System.out.println("ClientReceiver - encrypted session key received: " + keyMessage.getContent());
                App.setMessage(keyMessage);
                keyMessageExists = true;
            }
        }
    }

    private void getMessages(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        while(true) {
            Message mess = (Message) ois.readObject();
            if (mess != null) {
                if (mess instanceof ContentMessage) {
                    System.out.println("CilentReceiver - encrypted message received: " + ((ContentMessage) mess).getContent());
                    App.setMessage(mess);
                } else if (mess instanceof FileMessage) {
                    System.out.println("CilentReceiver - encrypted file received: " + ((FileMessage) mess).getFile());
                    App.setMessage(mess);
                }
            }
        }
    }
}

