package javachat;
import java.net.*;

public class PeerUtils {
    public static String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }
}
