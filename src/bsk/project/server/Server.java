package bsk.project.server;

import bsk.project.CONSTANTS;
import bsk.project.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static Message messFrom1 = new Message();
    private static Message messFrom2 = new Message();

    public static void main(String[] args) {

        try(ServerSocket server = new ServerSocket(CONSTANTS.port)) {
            Socket socket1 = server.accept();
            System.out.println("Connected");
            //Socket socket2 = server.accept();
            //try {
            Thread receiver1 = new Thread(new Receiver(socket1, messFrom1, messFrom2));
            //Thread receiver2 = new Thread(new Receiver(socket2, messFrom2, messFrom1));
            Thread sender1 = new Thread(new Sender(socket1, messFrom1, messFrom2));
            //Thread sender2 = new Thread(new Sender(socket2, messFrom2, messFrom1));
            //inne watki
            receiver1.start();
            //receiver2.start();
            sender1.start();
            //sender2.start();
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

    public static void runProtocol() throws IOException //, ClassNotFoundException {
    {
        while(true) {
            writeMess(messFrom1);
            if (messFrom1.ifReadyToSend()) System.out.println("1");
            if (messFrom2.ifReadyToSend()) System.out.println("2");
            //messFrom1.setMessage(new Message());
        }
    }

    public static void writeMess(Message mess) {
        if (mess.ifReadyToSend()) {
            System.out.println("Received mess: " + mess.getContent() + "\n");
        }
    }
}
