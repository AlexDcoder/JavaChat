package javachat;

public class PeerInfo {
    public String username;
    public String ip;
    public int port;

    public PeerInfo(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return username + " (" + ip + ":" + port + ")";
    }
}
