import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class PeerDiscovery implements Runnable {
    private final int port;

    public PeerDiscovery(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("🌍 Peer descoberto: " + message);
            }
        } catch (IOException e) {
            System.err.println("❌ Erro na descoberta de peers: " + e.getMessage());
        }
    }
}
