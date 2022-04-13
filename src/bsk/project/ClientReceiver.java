package bsk.project;

import java.io.IOException;
import java.io.ObjectInputStream;
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

            while(true) {
                Message mess = (Message) ois.readObject();
                if (mess != null) {
                    App.setMessage(mess);
                }
            }

//        ois.close();
//        Thread.currentThread().interrupt();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

