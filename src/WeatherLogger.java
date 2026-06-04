import se.mau.DA343A.VT25.assignment2.Logger;
import se.mau.DA343A.VT25.assignment2.Buffer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * WeatherLogger hanterar loggning av väderdata med en trådsäker buffer.
 */

public class WeatherLogger extends Logger {
    private static final String SHUTDOWN_MESSAGE = "\0SHUTDOWN\0";

    private final String logFilePath;
    private final Buffer<String> logBuffer = new Buffer<>();
    private volatile boolean running = true;
    private final Thread logThread;

    /**
     * Skapar en WeatherLogger som använder Buffer<T> för trådsäker loggning.
     * @param logFilePath Filvägen där loggdata ska sparas.
     */
    public WeatherLogger(String logFilePath) {
        this.logFilePath = logFilePath;

        // Startar en separat tråd för att hantera loggningen
        logThread = new Thread(this::processLogQueue);
        logThread.setDaemon(true);
        logThread.start();
    }

    @Override
    protected void writeMessage(String message) {
        if (running) {
            logBuffer.put(message);
        }
    }

    /**
     * Hämtar loggmeddelanden från bufferten och skriver dem till filen.
     */
    private void processLogQueue() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            while (true) {
                try {
                    String message = logBuffer.get();
                    if (SHUTDOWN_MESSAGE.equals(message)) {
                        break;
                    }
                    writer.println(message);
                    writer.flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Fel vid loggning: " + e.getMessage());
        }
    }

    /**
     * Stänger ner loggern och ser till att alla meddelanden loggas innan avslut.
     */
    public void shutdown() {
        running = false;
        logBuffer.put(SHUTDOWN_MESSAGE);
        try {
            logThread.join(); // Väntar på att loggtråden avslutas
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
