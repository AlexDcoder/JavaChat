package javachat;

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private String username;
    private int port;
    private ServerSocket serverSocket;
    private List<Socket> connections = new ArrayList<>();
    public static boolean is_chatting = false;
    public static boolean is_connecting = false;
    public static Peer hostPeer;
    public static Map<String, List<String>> history = new HashMap<>();
    public static String hostAtual;

    private PeerDiscovery peerDiscovery;
    private ServerListener serverListener;

    public Peer(String username, int port) {
        this.username = username;
        try {
            serverSocket = new ServerSocket(port);
            this.port = serverSocket.getLocalPort();
            System.out.println("Peer " + username + " ouvindo na porta " + this.port);
            System.out.println("Endereço IP: " + PeerUtils.getLocalIPAddress());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao iniciar o servidor.");
        }

        this.peerDiscovery = new PeerDiscovery(username, this.port);
        this.serverListener = new ServerListener(serverSocket, connections);
    }

    public void start() {
        new Thread(serverListener).start();
        new Thread(this::listenForUserInput).start();
    }

    public void startDiscoveryResponder() {
        new Thread(peerDiscovery::startResponder).start();
    }

    public List<PeerInfo> discoverPeers() {
        return peerDiscovery.discoverPeers();
    }

    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                if (!is_connecting) {
                    System.out.println("Nenhuma conexão estabelecida");
                    NewConnection();
                } else {
                    String message = userInput.readLine();
                    MessageHandler.broadcastMessage(message, connections, username);
                    if (history.containsKey(hostAtual) && is_chatting) {
                        history.get(hostAtual).add(message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler entrada do usuário.");
        }
    }

    public void connectToPeer(String host, int port) {
        try {
            hostAtual = host;
            Socket socket = new Socket(host, port);
            connections.add(socket);
            new Thread(new ConnectionHandler(socket)).start();
            System.out.println("Conectado ao peer em " + host + ":" + port);
            if (!history.containsKey(host)) {
                history.put(host, new ArrayList<>());
            } else {
                for (String msg : history.get(host)) {
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao peer em " + host + ":" + port);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome de usuário: ");
        String username = scanner.nextLine();

        hostPeer = new Peer(username, 0);
        hostPeer.startDiscoveryResponder();
        NewConnection();
    }

    public static void NewConnection() {
        Scanner scanner = new Scanner(System.in);
        is_connecting = true;
        hostPeer.start();

        System.out.print("Deseja conectar a outro peer? (s/n): ");
        String resposta = scanner.nextLine();
        if (resposta.equalsIgnoreCase("s")) {
            List<PeerInfo> availablePeers = hostPeer.discoverPeers();
            if (availablePeers.isEmpty()) {
                System.out.println("Nenhum peer disponível no momento.");
            } else {
                for (int i = 0; i < availablePeers.size(); i++) {
                    System.out.println(i + ": " + availablePeers.get(i));
                }
                System.out.print("Escolha o número do peer: ");
                int choice = scanner.nextInt();
                if (choice >= 0 && choice < availablePeers.size()) {
                    hostPeer.connectToPeer(availablePeers.get(choice).ip, availablePeers.get(choice).port);
                    is_chatting = true;
                } else {
                    System.out.println("Opção inválida.");
                }
            }
        }
    }
}
