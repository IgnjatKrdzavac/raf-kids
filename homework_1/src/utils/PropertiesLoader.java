package utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PropertiesLoader {

    private static PropertiesLoader singleInstance = null;

    public final Properties properties = new Properties();

    public final Map<String, Object> propertiesMap = new HashMap<>();

    private PropertiesLoader() {
        loadProperties();
    }

    public static PropertiesLoader getInstance () {
        if (singleInstance == null) {
            singleInstance = new PropertiesLoader();
        }

        return singleInstance;
    }

    public Map<String, Object> getProperties () {
        return propertiesMap;
    }

    private void loadProperties () {
        FileReader fr = null;

        try {
            fr = new FileReader("homework_1/app.properties");

            properties.load(fr);
        } catch (IOException e) {
            System.err.println("Could not read properties file !!");
            System.err.println("Exiting. . .");

            System.exit(-1);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Enumeration<Object> keys = properties.keys();
        Enumeration<Object> values = properties.elements();

        while (values.hasMoreElements() && keys.hasMoreElements()) {
            String key = String.valueOf(keys.nextElement());
            String val = String.valueOf(values.nextElement());

            if (isNumeric(val)) {
                propertiesMap.put(key, Integer.parseInt(val));
            } else if (key.equals("keywords")) {
                propertiesMap.put(key , parseKeywords(val));
            } else {
                propertiesMap.put(key, val);
            }
        }

    }

    private boolean isNumeric (String string) {
        if (string == null || string.equals("")) {
            return false;
        }

        try {
            Integer.parseInt(string);

            return true;
        } catch (NumberFormatException e) {
            // Do nothing
        }

        return false;
    }

    private List<String> parseKeywords (String keywordsRaw) {
        if (!keywordsRaw.contains(",")) {
            System.err.println("Failed to parse keywords !!");
            System.err.println("Exiting. . .");

            System.exit(-1);
        }

        return new ArrayList<>(Arrays.asList(keywordsRaw.split(",")));
    }
}
