package bsk.project;

import bsk.project.Encryption.Encryptor;
import bsk.project.Messages.*;
import bsk.project.Messages.Message.*;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.*;

public class ClientSender implements Runnable {
    private static ArrayList<Message> clientMessages;
    private Socket server;
    private ClientData clientData;
    private ClientData clientData2;
    private static Encryptor encryptor;

    public ClientSender(Socket server, ClientData clientData, ClientData clientData2) {
        this.server = server;
        this.clientData = clientData;
        this.clientData2 = clientData2;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());

            //sending user name
            oos.writeObject(clientData.getUserName());
            System.out.println("ClientSender - user name sended: " + clientData.getUserName());
            oos.reset();
            oos.flush();

            sendPublicKey(oos);
            sendSessionKey(oos);
            sendMessages(oos);

        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException |
                InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendPublicKey(ObjectOutputStream oos) throws IOException {
        oos.writeObject(new KeyMessage(
                clientData.getPublicKey(), ContentMessage.MessageType.PUBLIC_KEY, null));
        System.out.println("ClientSender - public key sended: " + clientData.getPublicKey());
        oos.reset();
        oos.flush();
    }

    private void sendSessionKey(ObjectOutputStream oos) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {

        boolean sended = false;

        while (!sended) {
            encryptor = App.getEncryptor();
            if (clientData2.getKeyPair() != null) {
                ContentMessage contentMessage = encryptor.encryptMessage(
                        new KeyMessage(clientData.getSessionKey(), MessageType.SESSION_KEY,
                                new Algorithm(CONSTANTS.RsaAlgName, clientData.getSessionKeySize(), null)),
                        clientData2.getPublicKey());

                oos.writeObject(contentMessage);
                System.out.println("ClientSender - encrypted session key sended: " + contentMessage.getContent());
                oos.reset();
                oos.flush();

                sended = true;
            }
        }
    }

    private void sendMessages(ObjectOutputStream oos) throws IOException, InterruptedException {

        while (true) {
            clientMessages = App.getMessages();
            if (clientMessages.size() > 0) {
                Message mess = clientMessages.remove(0);

                if (mess instanceof ContentMessage) {
                    oos.writeObject(mess);
                    System.out.println("ClientSender - encrypted message sended: " + ((ContentMessage)mess).getContent());
                    oos.reset();
                    oos.flush();
                } else if (mess instanceof FileMessage) {
                    FileMessage fileMessage = (FileMessage) mess;

                    if (fileMessage.getPartNumber() == 0)
                        App.startSendingTime();

                    oos.writeObject(mess);
                    System.out.println("ClientSender - encrypted file sended: " + ((FileMessage)mess).getFileName());
                    oos.reset();
                    oos.flush();

                    if (fileMessage.getPartNumber() == (int)fileMessage.getAllPartsNumber() - 1)
                        App.stopSendingTime();
                    App.setSendingProgressBar(
                            (int)(100 * (float)(fileMessage.getPartNumber() + 1)/(float)fileMessage.getAllPartsNumber()));
                }
            } else if (App.getEncryptionProgressBarValue() == 100 && App.getSendingProgressBarValue() == 100) {
                Thread.sleep(1000);
                App.setSendingProgressBar(0);
                App.setEncryptionProgressBar(0);
            }
        }
    }
}
