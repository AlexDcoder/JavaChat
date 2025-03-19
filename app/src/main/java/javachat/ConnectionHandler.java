package javachat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Classe responsável por gerenciar a conexão com outro peer e processar mensagens recebidas.
public class ConnectionHandler implements Runnable {
    private Socket socket; // A conexão com o peer (socket)

    // Construtor que recebe o socket da conexão com o peer.
    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    // Método que é executado quando a thread é iniciada
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            // Loop que continua lendo mensagens enquanto houver dados
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
                // Adiciona a mensagem ao histórico do peer conectado
                Peer.history.getOrDefault(Peer.hostAtual, new ArrayList<>()).add(message);
            }
        } catch (IOException e) {
            System.out.println("Conexão encerrada com " + socket.getRemoteSocketAddress()); // Exibe quando a conexão é fechada
        }
    }
}
