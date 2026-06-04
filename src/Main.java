import se.mau.DA343A.VT25.assignment2.*;

import java.awt.image.BufferedImage;

/**
 * Huvudklassen för WeatherGUI-systemet.
 */
public class Main {
    private static final String LOG_FILE = "weather_log.txt";

    public static void main(String[] args) {
        ImageResources resources = new ImageResources();
        BufferedImage mapImage = resources.getMapImage();
        BufferedImage maskedMapImage = resources.getMapIsLandMaskImage();

        IIsLand island = new IsLandFromMaskImage(maskedMapImage);
        Weather weather = new Weather(island);
        WeatherLogger logger = new WeatherLogger(LOG_FILE);

        new WeatherGUI(mapImage, weather, logger);
    }
}
