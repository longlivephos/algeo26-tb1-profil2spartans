package algeo.core.util;

public class NumberFormatter {
    public static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}