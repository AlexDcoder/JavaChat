package javachat;

import java.io.*;
import java.net.*;
import java.util.List;

public class MessageHandler {
    public static void broadcastMessage(String message, List<Socket> connections, String username) {
        for (Socket socket : connections) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(username + ": " + message);
            } catch (IOException e) {
                System.out.println("Erro ao enviar mensagem para " + socket.getRemoteSocketAddress());
            }
        }
    }
}
