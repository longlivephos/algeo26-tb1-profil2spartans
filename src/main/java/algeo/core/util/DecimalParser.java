package algeo.core.util;

public class DecimalParser {
    public static double parse(String input) throws NumberFormatException {
        return Double.parseDouble(input.trim().replace(",", "."));
    }
}