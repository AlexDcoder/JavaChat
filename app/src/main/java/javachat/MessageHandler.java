package javachat;

import java.io.*;
import java.net.*;
import java.util.List;

// Classe responsável por gerenciar o envio de mensagens para múltiplos peers.
public class MessageHandler {

    // Método que envia uma mensagem para todos os peers conectados.
    public static void broadcastMessage(String message, List<Socket> connections, String username) {
        // Loop que percorre todas as conexões (sockets) ativas
        for (Socket socket : connections) {
            try {
                // PrintWriter para enviar a mensagem através do socket
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(username + ": " + message);
            } catch (IOException e) {
                // Mensagem de erro caso ocorra um problema ao enviar a mensagem
                System.out.println("Erro ao enviar mensagem para " + socket.getRemoteSocketAddress());
            }
        }
    }
}
