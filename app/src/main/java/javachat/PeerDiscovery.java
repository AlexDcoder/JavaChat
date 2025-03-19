package javachat;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class PeerDiscovery {
    private static final int DISCOVERY_PORT = 8888;
    private static final String DISCOVERY_REQUEST = "PEER_DISCOVERY_REQUEST";
    private static final String DISCOVERY_RESPONSE = "PEER_DISCOVERY_RESPONSE";

    private String username;
    private int port;

    public PeerDiscovery(String username, int port) {
        this.username = username;
        this.port = port;
    }

    public void startResponder() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
            socket.setBroadcast(true);
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals(DISCOVERY_REQUEST)) {
                    String response = DISCOVERY_RESPONSE + ":" + username + ":" + PeerUtils.getLocalIPAddress() + ":"
                            + port;
                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                            packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PeerInfo> discoverPeers() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] requestData = DISCOVERY_REQUEST.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(requestPacket);

            socket.setSoTimeout(3000);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                try {
                    byte[] buf = new byte[256];
                    DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                    socket.receive(responsePacket);
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    if (response.startsWith(DISCOVERY_RESPONSE)) {
                        String[] parts = response.split(":");
                        if (parts.length == 4) {
                            PeerInfo peerInfo = new PeerInfo(parts[1], parts[2], Integer.parseInt(parts[3]));
                            discoveredPeers.add(peerInfo);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return discoveredPeers;
    }
}
