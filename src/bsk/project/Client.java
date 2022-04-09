package bsk.project;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
    private static ArrayList<Message> clientMessages;

    public Client() {}

    public static void request(Socket server) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
        //ObjectInputStream ois = new ObjectInputStream(server.getInputStream());

        while (true) {

            //TODO: why this line changes size?
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
    }

    @Override
    public void run() {
        try(Socket server = new Socket(CONSTANTS.ipAddr, CONSTANTS.port)) {
            System.out.println("Before");
            request(server);
        } catch(IOException | ClassNotFoundException ex) {
            System.err.println(ex);
        }
    }
}
