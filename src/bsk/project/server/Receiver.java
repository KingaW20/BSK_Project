package bsk.project.server;

import bsk.project.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Receiver implements Runnable {
    private Socket socket;
    private Message messageToSend;          //dla drugiego gracza - tutaj odczytac trzeba
    private Message messageReceived;        //dla tego gracza - trzeba mu wyslac

    public Receiver(Socket socket, Message messageToSend, Message messageReceived) {
        this.socket = socket;
        this.messageToSend = messageToSend;
        this.messageReceived = messageReceived;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            while(true) {
                Message mess = (Message) ois.readObject();
                if (mess != null) {
                    System.out.println("Received mess: " + mess.getContent());
                    messageToSend.setMessage(mess);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
