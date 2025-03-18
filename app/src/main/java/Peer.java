import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Peer {
    private final String username;
    private final ServerSocket serverSocket;
    private final List<Socket> connections = new CopyOnWriteArrayList<>();
    private final MessageLogger logger = new MessageLogger();

    public Peer(String username, int port) throws IOException {
        this.username = username;
        this.serverSocket = new ServerSocket(port);
        System.out.printf("✅ Peer '%s' ouvindo na porta %d%n", username, port);
        new Thread(new PeerDiscovery(port)).start();
    }

    public void start() {
        new Thread(this::listenForConnections, "ConnectionListener").start();
        new Thread(this::listenForUserInput, "UserInputListener").start();
    }

    private void listenForConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                System.out.println("🔗 Nova conexão: " + socket.getRemoteSocketAddress());
                new Thread(new PeerHandler(socket, this), "PeerHandler").start();
            } catch (IOException e) {
                System.err.println("❌ Erro ao aceitar conexão: " + e.getMessage());
            }
        }
    }

    private void listenForUserInput() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("/sair")) {
                    shutdown();
                    break;
                }
                broadcastMessage(username + ": " + message);
            }
        }
    }

    public void broadcastMessage(String message) {
        logger.logMessage(message);
        for (Socket socket : connections) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                closeConnection(socket);
            }
        }
    }

    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            connections.add(socket);
            System.out.printf("✅ Conectado a %s:%d%n", host, port);
            new Thread(new PeerHandler(socket, this), "PeerConnector").start();
        } catch (IOException e) {
            System.err.printf("❌ Erro ao conectar com %s:%d%n", host, port);
        }
    }

    private void closeConnection(Socket socket) {
        try {
            connections.remove(socket);
            socket.close();
        } catch (IOException e) {
            System.err.println("❌ Erro ao fechar conexão: " + e.getMessage());
        }
    }

    private void shutdown() {
        try {
            System.out.println("🔌 Encerrando conexões...");
            for (Socket socket : connections) {
                socket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("❌ Erro ao encerrar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Nome de usuário: ");
            String username = scanner.nextLine();
            System.out.print("Porta: ");
            int port = scanner.nextInt();
            scanner.nextLine();

            Peer peer = new Peer(username, port);
            peer.start();
            System.out.println("✅ Chat iniciado!");
        } catch (IOException e) {
            System.err.println("❌ Erro ao iniciar o peer: " + e.getMessage());
        }
    }
}