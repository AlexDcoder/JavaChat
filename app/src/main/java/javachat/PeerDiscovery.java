package javachat;

import java.io.IOException;
import java.net.*;
import java.util.*;

// Classe responsável por descobrir e responder a solicitações de descoberta de peers na rede.
public class PeerDiscovery {
    private static final int DISCOVERY_PORT = 8888; // Porta usada para descoberta de peers
    private static final String DISCOVERY_REQUEST = "PEER_DISCOVERY_REQUEST"; // Mensagem de solicitação de descoberta
    private static final String DISCOVERY_RESPONSE = "PEER_DISCOVERY_RESPONSE"; // Mensagem de resposta

    private String username;
    private int port;

    // Construtor que inicializa o peer com nome de usuário e porta
    public PeerDiscovery(String username, int port) {
        this.username = username;
        this.port = port;
    }

    // Método que escuta por solicitações de descoberta e responde com informações do peer
    public void startResponder() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
            socket.setBroadcast(true); // Permite a comunicação via broadcast
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Aguarda uma solicitação

                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals(DISCOVERY_REQUEST)) {
                    // Cria uma resposta com as informações do peer
                    String response = DISCOVERY_RESPONSE + ":" + username + ":" + PeerUtils.getLocalIPAddress() + ":" + port;
                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                            packet.getAddress(), packet.getPort());
                    socket.send(responsePacket); // Envia a resposta ao peer solicitante
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método que envia um broadcast para encontrar peers na rede
    public List<PeerInfo> discoverPeers() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            // Envia um pacote de descoberta para a rede
            byte[] requestData = DISCOVERY_REQUEST.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(requestPacket);

            socket.setSoTimeout(3000); // Tempo limite para respostas
            long startTime = System.currentTimeMillis();

            // Aguarda respostas dentro do tempo limite
            while (System.currentTimeMillis() - startTime < 3000) {
                try {
                    byte[] buf = new byte[256];
                    DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                    socket.receive(responsePacket);

                    // Processa a resposta recebida
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    if (response.startsWith(DISCOVERY_RESPONSE)) {
                        String[] parts = response.split(":");
                        if (parts.length == 4) {
                            PeerInfo peerInfo = new PeerInfo(parts[1], parts[2], Integer.parseInt(parts[3]));
                            discoveredPeers.add(peerInfo); // Adiciona o peer descoberto à lista
                        }
                    }
                } catch (SocketTimeoutException e) {
                    break; // Sai do loop se o tempo limite for atingido
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return discoveredPeers; // Retorna a lista de peers encontrados
    }
}
