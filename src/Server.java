/**
 * Author: Ahmet Altun
 * ID: 201913709075
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ServerSocket serverSocket;
    private static final int SERVER_PORT = 5173;
    private final ExecutorService executorService;
    private final Map<String, ClientHandler> clients;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.executorService = Executors.newCachedThreadPool();
        this.clients = new ConcurrentHashMap<>();
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket, this);
                System.out.printf("Yeni bir kullanıcı bağlandı. Kullanıcı adı: %s\n", clientHandler.getClientUsername());
                sendPrivateMessage("Server", clientHandler.getClientUsername(), "Hoşgeldin " + clientHandler.getClientUsername() + "!");
                var onlineUsers = getOnlineUsers(clientHandler.getClientUsername());
                if (getOnlineUsersCount(onlineUsers) > 0) {
                    sendPrivateMessage("Server", clientHandler.getClientUsername(), "Online kullanıcılar: " + String.join(", ", onlineUsers));
                }
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.printf("startError: %s\n", e.getMessage());
            closeServerSocket();
        }
    }

    private ArrayList<String> getOnlineUsers(String currentClientName) {
        ArrayList<String> onlineUsers = new ArrayList<>(clients.keySet());
        int indexOfCurrentClient = onlineUsers.indexOf(currentClientName);
        if (indexOfCurrentClient != -1) {
            onlineUsers.remove(indexOfCurrentClient);
        }
        return onlineUsers;
    }

    private int getOnlineUsersCount(ArrayList<String> onlineUsers) {
        return onlineUsers.size();
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.printf("closeServerSocketError: %s\n", e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    public void sendPrivateMessage(String sender, String recipient, String message) {
        if (clients.containsKey(recipient)) {
            ClientHandler recipientHandler = clients.get(recipient);
            recipientHandler.sendMessage(sender + " size bir mesaj gönderdi: " + message);
            System.out.printf("%s, %s kullanıcısına bir mesaj gönderdi: %s\n", sender, recipient, message);
        } else {
            System.out.println("User not found: " + recipient);
            ClientHandler senderHandler = clients.get(sender);
            senderHandler.sendMessage(recipient + " kullanıcısı bulunamadı.");
        }
    }

    public void removeClient(String username) {
        clients.remove(username);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Server server = new Server(serverSocket);
            server.start();
        } catch (IOException e) {
            System.out.printf("mainError: %s\n", e.getMessage());
        }
    }
}
