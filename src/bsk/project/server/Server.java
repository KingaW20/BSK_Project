package bsk.project.server;

import bsk.project.CONSTANTS;
import bsk.project.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static Message messFrom1;
    private static Message messFrom2;

    public static void main(String[] args) {

        try(ServerSocket server = new ServerSocket(CONSTANTS.port)) {
            Socket socket1 = server.accept();
            Socket socket2 = server.accept();
            System.out.println("Connected");
            //try {
            Thread receiver1 = new Thread(new Receiver(socket1, true));
            Thread receiver2 = new Thread(new Receiver(socket2, false));
            Thread sender1 = new Thread(new Sender(socket1, true));
            Thread sender2 = new Thread(new Sender(socket2, false));
            //inne watki
            receiver1.start();
            receiver2.start();
            sender1.start();
            sender2.start();
            //inne watki start
            runProtocol();
            //inne watki trzeba thread.join();
            //} catch(ClassNotFoundException | IOException e) {
            //    e.printStackTrace();
            //}
        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(1);
        }

    }

    public static void runProtocol() throws IOException {
        while(true) {}
    }

    public static Message getMessFrom(boolean firstClient) {
        Message mess = firstClient ? messFrom1 : messFrom2;
        return mess;
    }

    public static void setMessFrom(boolean firstClient, Message mess) {
        if (firstClient) {
            messFrom1 = mess;
        } else {
            messFrom2 = mess;
        }

        if (messFrom1 != null) System.out.println("First message: " + messFrom1);
        if (messFrom2 != null) System.out.println("Second message: " + messFrom2);
    }
}
