package bsk.project.server;

import bsk.project.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender implements Runnable {
    private Socket socket;
    private boolean firstClient;

    public Sender(Socket socket, boolean firstClient) {
        this.socket = socket;
        this.firstClient = firstClient;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            while(true) {
                //wyslanie czegos - najlepiej w metodzie, jezeli wiele tego bedzie
                Message mess = Server.getMessFrom(!firstClient);
                System.out.println("Message: " + mess);             //bez tej linijki nie dziala???
                if (mess != null) {
                    oos.writeObject(new Message(mess.getContent(), mess.ifFile(), mess.ifReadyToSend()));
                    System.out.println("Send");
                    oos.reset();
                    oos.flush();
                    Server.setMessFrom(!firstClient, null);
                }

//                if (messageReceived.ifReadyToSend()) {
//                    oos.writeObject(new Message(
//                            messageReceived.getContent(), messageReceived.ifFile(), messageReceived.ifReadyToSend()));
//                    messageReceived.setMessage(new Message());
//                    oos.reset();
//                    oos.flush();
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
