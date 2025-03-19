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

    private PeerDiscovery peerDiscovery; // Objeto para gerenciamento de descoberta de peers
    private ServerListener serverListener; // Objeto para escutar as conexões de entrada

    // Construtor da classe Peer que inicializa o nome do usuário e a porta do servidor
    public Peer(String username, int port) {
        this.username = username;
        try {
            serverSocket = new ServerSocket(port); // Inicializa o ServerSocket na porta especificada
            this.port = serverSocket.getLocalPort(); // Atribui a porta local ao peer
            System.out.println("Peer " + username + " ouvindo na porta " + this.port);
            System.out.println("Endereço IP: " + PeerUtils.getLocalIPAddress());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao iniciar o servidor.");
        }

        this.peerDiscovery = new PeerDiscovery(username, this.port); // Inicializa o processo de descoberta de peers
        this.serverListener = new ServerListener(serverSocket, connections); // Inicializa o listener de conexões
    }

    public void start() {
        new Thread(serverListener).start(); // Inicia a thread para escutar conexões de entrada
        new Thread(this::listenForUserInput).start(); // Inicia a thread para escutar a entrada do usuário
    }

    // Método que inicia o processo de responder a descoberta de peers
    public void startDiscoveryResponder() {
        new Thread(peerDiscovery::startResponder).start(); // Inicia a thread para responder a solicitações de descoberta
    }

    public List<PeerInfo> discoverPeers() {
        return peerDiscovery.discoverPeers(); // Chama o método de descoberta de peers
    }

    // Método que escuta a entrada do usuário e envia as mensagens
    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                if (!is_connecting) { // Verifica se não há conexão ativa
                    System.out.println("Nenhuma conexão estabelecida");
                    NewConnection(); // Chama o método para tentar estabelecer uma nova conexão
                } else {
                    String message = userInput.readLine(); // Lê a entrada do usuário
                    MessageHandler.broadcastMessage(message, connections, username); // Envia a mensagem para todos os peers conectados
                    if (history.containsKey(hostAtual) && is_chatting) {
                        history.get(hostAtual).add(message); // Adiciona a mensagem ao histórico se estiver conversando
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler entrada do usuário.");
        }
    }

    // Método que conecta o peer atual a outro peer dado o endereço e a porta
    public void connectToPeer(String host, int port) {
        try {
            hostAtual = host; // Armazena o host com o qual está se conectando
            Socket socket = new Socket(host, port); // Cria o socket para conexão com o peer
            connections.add(socket); // Adiciona a conexão à lista de conexões
            new Thread(new ConnectionHandler(socket)).start(); // Inicia a thread para gerenciar a conexão
            System.out.println("Conectado ao peer em " + host + ":" + port);
            if (!history.containsKey(host)) {
                history.put(host, new ArrayList<>()); // Cria um novo histórico de mensagens se não existir
            } else {
                for (String msg : history.get(host)) {
                    System.out.println(msg); // Exibe o histórico de mensagens do host
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

        hostPeer = new Peer(username, 0); // Cria uma instância do Peer com o nome de usuário
        hostPeer.startDiscoveryResponder(); // Inicia o processo de descoberta de peers
        NewConnection(); // Estabelece uma nova conexão
    }

    // Método que tenta estabelecer uma nova conexão com outro peer
    public static void NewConnection() {
        Scanner scanner = new Scanner(System.in);
        is_connecting = true; // Marca que está tentando conectar
        hostPeer.start();

        System.out.print("Deseja conectar a outro peer? (s/n): ");
        String resposta = scanner.nextLine();
        if (resposta.equalsIgnoreCase("s")) { // Se o usuário quer conectar a outro peer
            List<PeerInfo> availablePeers = hostPeer.discoverPeers(); // Descobre os peers disponíveis
            if (availablePeers.isEmpty()) {
                System.out.println("Nenhum peer disponível no momento.");
            } else {
                for (int i = 0; i < availablePeers.size(); i++) {
                    System.out.println(i + ": " + availablePeers.get(i)); // Exibe a lista de peers encontrados
                }
                System.out.print("Escolha o número do peer: ");
                int choice = scanner.nextInt();
                if (choice >= 0 && choice < availablePeers.size()) {
                    hostPeer.connectToPeer(availablePeers.get(choice).ip, availablePeers.get(choice).port); // Conecta ao peer escolhido
                    is_chatting = true; // Marca que a conversa foi iniciada
                } else {
                    System.out.println("Opção inválida."); // Caso a opção seja inválida
                }
            }
        }
    }
}
