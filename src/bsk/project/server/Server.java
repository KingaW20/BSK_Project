package bsk.project.server;

import bsk.project.CONSTANTS;
import bsk.project.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static ArrayList<Message> messFrom1;
    private static ArrayList<Message> messFrom2;

    public static void main(String[] args) {
        messFrom1 = new ArrayList<>();
        messFrom2 = new ArrayList<>();

        try(ServerSocket server = new ServerSocket(CONSTANTS.port)) {
            Socket socket1 = server.accept();
            Socket socket2 = server.accept();
            System.out.println("Connected");
            Thread receiver1 = new Thread(new Receiver(socket1, true));
            Thread receiver2 = new Thread(new Receiver(socket2, false));
            Thread sender1 = new Thread(new Sender(socket1, true));
            Thread sender2 = new Thread(new Sender(socket2, false));
            receiver1.start();
            sender2.start();
            receiver2.start();
            sender1.start();
            runProtocol();
        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(1);
        }

    }

    public static void runProtocol() throws IOException {
        while(true) {}
    }

    public static ArrayList<Message> getMessagesFrom(boolean firstClient) {
        if (firstClient) {
            return messFrom1;
        } else {
            return messFrom2;
        }
    }

    public static void setMessFrom(boolean firstClient, Message mess) {
        if (mess != null)
        {
            if (firstClient) {
                messFrom1.add(mess);
            } else {
                messFrom2.add(mess);
            }
        }
    }
}
