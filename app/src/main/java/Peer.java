import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Peer {
    // Nome de usuário para identificação
    private final String username;

    // Socket do servidor para aceitar conexões
    private final ServerSocket serverSocket;

    // Lista de conexões abertas (usando CopyOnWriteArrayList para segurança em threads)
    private final List<Socket> connections = new CopyOnWriteArrayList<>();

    // Construtor para inicializar o peer
    public Peer(String username, int port) throws IOException {
        this.username = username;
        this.serverSocket = new ServerSocket(port); // Cria um socket para escutar na porta especificada
        System.out.printf("✅ Peer '%s' ouvindo na porta %d%n", username, port);
    }

    // Método para iniciar o peer e começar a aceitar conexões
    public void start() {
        // Cria uma thread para escutar conexões de outros peers
        new Thread(this::listenForConnections, "ConnectionListener").start();

        // Cria uma thread para capturar mensagens do usuário pelo console
        new Thread(this::listenForUserInput, "UserInputListener").start();
    }

    // Método para escutar conexões de outros peers
    private void listenForConnections() {
        while (!serverSocket.isClosed()) {
            try {
                // Aceita uma nova conexão
                Socket socket = serverSocket.accept();

                // Adiciona a conexão na lista
                connections.add(socket);

                System.out.println("🔗 Nova conexão estabelecida com " + socket.getRemoteSocketAddress());

                // Cria uma nova thread para lidar com essa conexão
                new Thread(() -> handleConnection(socket), "PeerHandler-" + socket.getPort()).start();
            } catch (IOException e) {
                System.err.println("❌ Erro ao aceitar conexão: " + e.getMessage());
            }
        }
    }

    // Método para lidar com a comunicação com o peer conectado
    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;

            // Lê mensagens enviadas pelo peer até que a conexão seja encerrada
            while ((message = in.readLine()) != null) {
                System.out.println("\uD83D\uDCE2 " + message); // 📢 Exibe a mensagem recebida
            }
        } catch (IOException e) {
            System.err.println("❌ Erro na conexão com " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            // Fecha a conexão quando terminar
            closeConnection(socket);
        }
    }

    // Método para capturar mensagens do usuário e enviá-las para os peers conectados
    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            String message;

            // Lê mensagens digitadas pelo usuário
            while ((message = userInput.readLine()) != null) {
                broadcastMessage(message); // Envia a mensagem para todos os peers conectados
            }
        } catch (IOException e) {
            System.err.println("❌ Erro na leitura da entrada do usuário: " + e.getMessage());
        }
    }

    // Método para enviar uma mensagem para todos os peers conectados
    private void broadcastMessage(String message) {
        for (Socket socket : connections) {
            try {
                // Usa PrintWriter para enviar a mensagem pela conexão
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(username + ": " + message);
            } catch (IOException e) {
                System.err.println("❌ Erro ao enviar mensagem para " + socket.getRemoteSocketAddress());
                closeConnection(socket); // Fecha a conexão caso ocorra um erro
            }
        }
    }

    // Método para conectar-se a outro peer usando o endereço e porta fornecidos
    public void connectToPeer(String host, int port) {
        try {
            // Cria um novo socket para se conectar ao peer
            Socket socket = new Socket(host, port);

            // Adiciona o socket à lista de conexões
            connections.add(socket);

            System.out.printf("✅ Conectado ao peer em %s:%d%n", host, port);

            // Cria uma nova thread para lidar com essa conexão
            new Thread(() -> handleConnection(socket), "PeerConnector-" + port).start();
        } catch (IOException e) {
            System.err.printf("❌ Erro ao conectar ao peer em %s:%d - %s%n", host, port, e.getMessage());
        }
    }

    // Método para fechar uma conexão com um peer
    private void closeConnection(Socket socket) {
        try {
            // Remove a conexão da lista
            connections.remove(socket);

            // Fecha o socket se ainda estiver aberto
            if (!socket.isClosed()) {
                socket.close();
            }

            System.out.println("❎ Conexão encerrada com " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("❌ Erro ao fechar conexão: " + e.getMessage());
        }
    }

    // Método principal para iniciar o peer e configurar conexões
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Solicita o nome de usuário
            System.out.print("Digite seu nome de usuário: ");
            String username = scanner.nextLine();

            // Solicita a porta para escutar conexões
            System.out.print("Digite a porta para escutar: ");
            int port = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer de entrada

            // Cria um novo peer
            Peer peer = new Peer(username, port);
            peer.start(); // Inicia o peer (aceita conexões e permite envio de mensagens)

            // Pergunta se o usuário deseja se conectar a outro peer
            System.out.print("Deseja conectar a outro peer? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                System.out.print("Digite o endereço do peer (host): ");
                String peerHost = scanner.nextLine();

                System.out.print("Digite a porta do peer: ");
                int peerPort = scanner.nextInt();

                // Conecta ao peer especificado
                peer.connectToPeer(peerHost, peerPort);
            }

            System.out.println("✅ Chat iniciado! Digite mensagens para começar...");
        } catch (IOException e) {
            // Captura erro ao criar o peer ou socket
            System.err.println("❌ Erro ao iniciar o peer: " + e.getMessage());
        } catch (Exception e) {
            // Captura erros inesperados
            System.err.println("❌ Erro inesperado: " + e.getMessage());
        }
    }
}