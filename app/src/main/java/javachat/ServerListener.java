package javachat;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class ServerListener implements Runnable {
    private ServerSocket serverSocket;
    private List<Socket> connections;

    public ServerListener(ServerSocket serverSocket, List<Socket> connections) {
        this.serverSocket = serverSocket;
        this.connections = connections;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                connections.add(clientSocket);
                System.out.println("Novo peer conectado: " + clientSocket.getRemoteSocketAddress());
                new Thread(new ConnectionHandler(clientSocket)).start();
            } catch (IOException e) {
                System.out.println("Erro ao aceitar nova conexão.");
                break;
            }
        }
    }
}
