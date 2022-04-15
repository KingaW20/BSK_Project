package bsk.project;

import bsk.project.Messages.ContentMessage;
import bsk.project.Messages.KeyMessage;
import bsk.project.Messages.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

public class ClientSender implements Runnable {
    private static ArrayList<ContentMessage> clientMessages;
    private Socket server;
    private ClientData clientData;
    private ClientData clientData2;

    public ClientSender(Socket server, ClientData clientData, ClientData clientData2) {
        this.server = server;
        this.clientData = clientData;
        this.clientData2 = clientData2;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());

            sendPublicKey(oos);
            sendSessionKey(oos);
            sendMessages(oos);

//        oos.close();
//        Thread.currentThread().interrupt();
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private void sendPublicKey(ObjectOutputStream oos) throws IOException {
        oos.writeObject(new KeyMessage(clientData.getPublicKey(), clientData.getKeyPairSize(), null,
                ContentMessage.MessageType.PUBLIC_KEY, null));
        System.out.println("Public key sended: " + clientData.getPublicKey());
        oos.reset();
        oos.flush();
    }

    private void sendSessionKey(ObjectOutputStream oos) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance(CONSTANTS.RsaAlgName);
        boolean sended = false;

        while (!sended) {
            if (clientData2.getKeyPair() != null) {
                encryptCipher.init(Cipher.ENCRYPT_MODE, clientData2.getPublicKey());

                byte[] sessionKeyBytes = clientData.getSessionKey().getEncoded();
                byte[] encryptedSessionKeyBytes = encryptCipher.doFinal(sessionKeyBytes);
                String encryptedSessionKeyString = Base64.getEncoder().encodeToString(encryptedSessionKeyBytes);

                //TODO: delete
                oos.writeObject(new KeyMessage(clientData.getSessionKey(), clientData.getSessionKeySize(),
                        clientData.getIv(), ContentMessage.MessageType.SESSION_KEY, null));
                System.out.println("Session key sended: " + clientData.getSessionKey());
                oos.reset();
                oos.flush();

                oos.writeObject(new ContentMessage(
                        encryptedSessionKeyString, Message.MessageType.SESSION_KEY, null));
                System.out.println("Encrypted session key sended: " + encryptedSessionKeyString);
                oos.reset();
                oos.flush();

                sended = true;
            }
        }

        System.out.println("Ended");
    }

    private void sendMessages(ObjectOutputStream oos) throws IOException {
        while (true) {
            clientMessages = App.getMessages();
            if (clientMessages.size() > 0) {
                ContentMessage mess = clientMessages.remove(0);
                oos.writeObject(mess);
                System.out.println("Send message: " + mess.getContent());
                oos.reset();
                oos.flush();
            }
        }
    }
}
