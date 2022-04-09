package bsk.project.server;

import bsk.project.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender implements Runnable {
    private Socket socket;
    private Message messageToSend;          //dla drugiego gracza - tutaj odczytac trzeba
    private Message messageReceived;        //dla tego gracza - trzeba mu wyslac

    public Sender(Socket socket, Message messageToSend, Message messageReceived) {
        this.socket = socket;
        this.messageToSend = messageToSend;
        this.messageReceived = messageReceived;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            while(true) {
                //wyslanie czegos - najlepiej w metodzie, jezeli wiele tego bedzie
                if (messageReceived.ifReadyToSend()) {
                    oos.writeObject(new Message(
                            messageReceived.getContent(), messageReceived.ifFile(), messageReceived.ifReadyToSend()));
                    messageReceived.setMessage(new Message());
                    oos.reset();
                    oos.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
