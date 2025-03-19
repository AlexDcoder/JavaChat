package javachat;

import java.io.IOException;
import java.net.*;
import java.util.List;

// Classe responsável por escutar conexões de novos peers no servidor.
public class ServerListener implements Runnable {
    private ServerSocket serverSocket; // Socket do servidor para aceitar conexões
    private List<Socket> connections; // Lista que armazena as conexões ativas

    // Construtor que recebe o socket do servidor e a lista de conexões
    public ServerListener(ServerSocket serverSocket, List<Socket> connections) {
        this.serverSocket = serverSocket;
        this.connections = connections;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Aguarda e aceita novas conexões de peers
                Socket clientSocket = serverSocket.accept();
                connections.add(clientSocket); // Adiciona o novo peer à lista
                System.out.println("Novo peer conectado: " + clientSocket.getRemoteSocketAddress());

                // Inicia uma thread para lidar com a conexão do novo peer
                new Thread(new ConnectionHandler(clientSocket)).start();
            } catch (IOException e) {
                System.out.println("Erro ao aceitar nova conexão.");
                break; // Sai do loop se ocorrer um erro
            }
        }
    }
}
