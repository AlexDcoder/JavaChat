package javachat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ConnectionHandler implements Runnable {
    private Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
                Peer.history.getOrDefault(Peer.hostAtual, new ArrayList<>()).add(message);
            }
        } catch (IOException e) {
            System.out.println("Conexão encerrada com " + socket.getRemoteSocketAddress());
        }
    }
}
