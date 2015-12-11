package client;

/**
 * Utility methods
 */
public class Util {

    /**
     * Convert from "username (ip:port)" format to "username"
     */
    public static String humanReadableUsername(String original) {
        return original.split("\\(")[0].trim();
    }

    /**
     * Returns a formatted time String in milliseconds
     */
    public static String formatTime(long nanoSeconds) {
        return Double.toString((double) nanoSeconds / 1000000);
    }
}
