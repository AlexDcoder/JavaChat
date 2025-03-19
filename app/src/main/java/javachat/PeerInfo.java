package javachat;

// Classe que armazena informações sobre um peer na rede.
public class PeerInfo {
    public String username; // Nome do usuário do peer
    public String ip; // Endereço IP do peer
    public int port; // Porta utilizada pelo peer

    // Construtor que inicializa os dados do peer
    public PeerInfo(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    // Método para representar o peer como string de forma legível
    @Override
    public String toString() {
        return username + " (" + ip + ":" + port + ")";
    }
}
