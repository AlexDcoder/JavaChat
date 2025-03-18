import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class PeerHandler implements Runnable {
    private final Socket socket;
    private final Peer peer;

    public PeerHandler(Socket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📢 " + message);
            }
        } catch (IOException e) {
            System.err.println("❌ Erro na conexão: " + e.getMessage());
        } finally {
            peer.closeConnection(socket);
        }
    }
}

