import java.io.FileWriter;
import java.io.IOException;

class MessageLogger {
    public void logMessage(String message) {
        try (FileWriter writer = new FileWriter("chat_history.txt", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar histórico: " + e.getMessage());
        }
    }
}