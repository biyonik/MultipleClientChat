/**
 * Author: Ahmet Altun
 * ID: 201913709075
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5173;

    public Client(Socket socket, String clientUsername) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = clientUsername;
        } catch (IOException e) {
            handleException("Client",e);
        }
    }

    private void handleException(String name, IOException e) {
        System.out.println("Sunucu ile bağlantı kurulamadı!");
        System.out.printf("%sError: %s\n", name, e.getMessage());
        closeAll(socket, bufferedReader, bufferedWriter);
        e.printStackTrace();
    }

    private void sendMessageGlobal() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (socket.isConnected()) {
            String message = scanner.nextLine();
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

    public void sendClientNameMessage() {
        try {
            bufferedWriter.write(clientUsername);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            handleException("sendClientNameMessage",e);
        }
    }

    public void listenMessage() {
        new Thread(() -> {
            String message;
            try {
                while (socket.isConnected() && (message = bufferedReader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                handleException("listenMessage",e);
            }
        }).start();
    }

    private void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            System.out.printf("ClientClass_closeAllError: %s\n", e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Kullanıcı adınızı giriniz: ");
        String username = scanner.nextLine();
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            Client client = new Client(socket, username);
            client.listenMessage();
            // Print other connected users

            client.sendClientNameMessage();
            client.sendMessageGlobal();
        } catch (IOException e) {
            System.out.printf("ClientClass_mainError: %s\n", e.getMessage());
        }
    }
}