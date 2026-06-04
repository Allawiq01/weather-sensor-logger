import se.mau.DA343A.VT25.assignment2.*;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WeatherGUI hanterar vädersimuleringen och interaktion med användaren.
 */
public class WeatherGUI extends GUI {
    private static final long UPDATE_INTERVAL_MS = 500;

    private boolean simulationIsActive = false;
    private final Weather weatherSystem;
    private Timer updateTimer = new Timer("Weather-Update-Timer", true);
    private final List<IPlayPauseButtonPressedCallback> buttonListeners = new ArrayList<>();
    private final List<Integer> activeSensors = new ArrayList<>();
    private final WeatherLogger eventLogger;


    /**
     * Skapar och startar WeatherGUI.
     *
     * @param mapImage Kartbilden för simuleringen.
     * @param weather  Vädersystemet som ger data.
     * @param logger   Logger för att spara data.
     */
    @SuppressWarnings("this-escape")
    public WeatherGUI(BufferedImage mapImage, Weather weather, WeatherLogger logger) {
        super(mapImage);
        this.weatherSystem = weather;
        this.eventLogger = logger;
        startGUIOnNewThread();
        Thread weatherThread = new Thread(weatherSystem, "Weather-Simulation");
        weatherThread.start();
        addPlayPauseButtonCallback(this::toggleWeatherUpdates);
        fetchAvailableSensors();
    }

    /**
     * Hämtar tillgängliga sensorer från Weather.
     */
    private void fetchAvailableSensors() {
        try {
            DataOutputStream output = (DataOutputStream) weatherSystem.getOutput();
            DataInputStream input = (DataInputStream) weatherSystem.getInput();

            output.writeInt(WeatherProtocol.VERSION);
            output.writeInt(WeatherProtocol.QUERY_SENSOR_LIST);
            output.flush();

            int version = input.readInt();
            int messageType = input.readInt();

            if (!isExpectedResponse(version, messageType, WeatherProtocol.RESPONSE_SENSOR_LIST)) {
                System.err.println("Felaktigt svar vid hämtning av sensorlista: version=" + version + ", messageType=" + messageType);
                return;
            }

            int sensorCount = input.readInt();
            for (int i = 0; i < sensorCount; i++) {
                activeSensors.add(input.readInt());
                input.readUTF(); // Sensor namn (används ej)
            }
        } catch (IOException e) {
            System.err.println("Kunde inte hämta sensorlista: " + e.getMessage());
        }
    }

    /**
     * Startar eller stoppar uppdateringen av väderdata.
     */
    private void toggleWeatherUpdates() {
        simulationIsActive = !simulationIsActive;
        if (simulationIsActive) {
            updateTimer = new Timer("Weather-Update-Timer", true);
            updateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    retrieveWeatherData();
                }
            }, 0, UPDATE_INTERVAL_MS);
        } else {
            updateTimer.cancel();
        }
    }

    /**
     * Hämtar temperaturdata och uppdaterar GUI.
     */
    private void retrieveWeatherData() {
        try {
            DataOutputStream output = (DataOutputStream) weatherSystem.getOutput();
            DataInputStream input = (DataInputStream) weatherSystem.getInput();
            List<GridTemperature> temperatures = new ArrayList<>();

            for (int sensorID : activeSensors) {
                output.writeInt(WeatherProtocol.VERSION);
                output.writeInt(WeatherProtocol.QUERY_SENSOR);
                output.writeInt(sensorID);
                output.flush();

                int version = input.readInt();
                int messageType = input.readInt();

                if (!isExpectedResponse(version, messageType, WeatherProtocol.RESPONSE_SENSOR)) {
                    System.err.println("Felaktigt svar från Weather: version=" + version + ", messageType=" + messageType);
                    continue;
                }

                int receivedSensorID = input.readInt();
                int row = input.readInt();
                int col = input.readInt();
                double temperature = input.readDouble();
                System.out.println(" Uppdaterar kartan vid (" + row + ", " + col + ") med temperatur: " + temperature);

                temperatures.add(new GridTemperature(row, col, temperature));
                eventLogger.log("Sensor " + receivedSensorID + ": " + temperature + "°C");
            }

            if (!temperatures.isEmpty()) {
                setTemperatures(temperatures);
                repaint();
            }
        } catch (IOException e) {
            System.err.println("Fel vid hämtning av temperaturdata: " + e.getMessage());
        }
    }

    /**
     * Lägger till en lyssnare för play/pause-knappen.
     */
    @Override
    public void addPlayPauseButtonCallback(IPlayPauseButtonPressedCallback callback) {
        if (!buttonListeners.contains(callback)) {
            buttonListeners.add(callback);
        }
    }

    /**
     * Tar bort en lyssnare för play/pause-knappen.
     */
    @Override
    public void removePlayPauseButtonCallback(IPlayPauseButtonPressedCallback callback) {
        buttonListeners.remove(callback);
    }

    /**
     * Anropar alla registrerade lyssnare för play/pause-knappen.
     */
    @Override
    protected void invokePlayPauseButtonCallbacks() {
        for (IPlayPauseButtonPressedCallback callback : new ArrayList<>(buttonListeners)) {
            callback.playPauseButtonPressed();
        }
    }

    /**
     * Körs när programmet stängs.
     */
    @Override
    protected void onExiting() {
        simulationIsActive = false;
        updateTimer.cancel();
        eventLogger.shutdown();
        System.out.println("Avslutar WeatherGUI...");
    }

    private boolean isExpectedResponse(int version, int messageType, int expectedMessageType) {
        return version == WeatherProtocol.VERSION && messageType == expectedMessageType;
    }
}
