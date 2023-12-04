/**
 * Author: Ahmet Altun
 * ID: 201913709075
 */

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = StringUtils.generateNickName(bufferedReader.readLine());
            clientHandlers.add(this);
            server.addClient(clientUsername, this);
            sendMessageToAll(clientUsername + " sohbete katıldı. Mesaj göndermek için @"+clientUsername+" ve sonrasında mesajınızı yazabilirsiniz.");

        } catch (IOException e) {
            System.out.printf("ClientHandlerError: %s\n", e.getMessage());
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.printf("sendMessageError: %s\n", e.getMessage());
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToAll(String message) {
        if (!clientHandlers.isEmpty()) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.sendMessage(message);
                }
            }
        }
    }
    @Override
    public void run() {
        String message;
        try {
            while (socket.isConnected() && (message = bufferedReader.readLine()) != null) {
                if (message.startsWith("@")) {
                    String[] messageArray = message.split(" ", 2);
                    System.out.println("MessageArray: "+ Arrays.toString(messageArray));
                    String recipient = messageArray[0].substring(1);
                    String privateMessage = messageArray[1];
                    server.sendPrivateMessage(clientUsername, recipient, privateMessage);
                } else {
                    for (ClientHandler clientHandler : clientHandlers) {
                        clientHandler.sendMessage(clientUsername + ": " + message);
                    }
                }
            }
        } catch (IOException e) {
            System.out.printf("runError: %s\n", e.getMessage());
            closeAll(socket, bufferedReader, bufferedWriter);
            server.removeClient(clientUsername);
            removeClient();
        }
    }

    public void removeClient() {
        clientHandlers.remove(this);
        sendMessageToAll(clientUsername + " sohbetten ayrıldı.");
    }

    private void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClient();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.printf("closeAllError: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }
}
