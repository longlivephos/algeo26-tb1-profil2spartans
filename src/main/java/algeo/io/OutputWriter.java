package algeo.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.spl.SPLResult;
import algeo.core.util.NumberFormatter;
import algeo.inverse.InverseResult;

public class OutputWriter {

    private OutputWriter() {}

    /** Menyusun hasil SPL. */
    public static String teksSPL(String metode, Matrix augmented, List<String> steps, SPLResult hasil) {
        return teksUmum(metode, augmented.toString(), steps, formatHasilSPL(hasil));
    }

    /** Menyusun hasil Determinan. */
    public static String teksDeterminan(String metode, Matrix input, List<String> steps, double determinan) {
        String hasilAkhir = "det(A) = " + NumberFormatter.teks(NumberFormatter.round(determinan));
        return teksUmum(metode, input.toString(), steps, hasilAkhir);
    }

    /** Menyusun hasil Inverse. */
    public static String teksInvers(String metode, Matrix input, List<String> steps, InverseResult hasil) {
        String hasilAkhir;
        if (hasil.getType() == InverseResult.ResultType.HAS_INVERSE) {
            hasilAkhir = "A^-1 =\n" + hasil.getInverse().toString();
        } else {
            hasilAkhir = "matriks tidak memiliki balikan (" + hasil.getErrorMessage() + ")";
        }
        return teksUmum(metode, input.toString(), steps, hasilAkhir);
    }

    // Tulis teks ke file

    public static void simpan(String path, String isi) throws IOException {
        try (PrintWriter pw = new PrintWriter(path)) {
            pw.print(isi);
        }
    }

    private static String formatHasilSPL(SPLResult result) {
        switch (result.getType()) {
            case UNIQUE:
                StringBuilder sb = new StringBuilder();
                double[] x = result.getSolution();
                for (int i = 0; i < x.length; i++) {
                    sb.append("x").append(i + 1).append(" = ").append(x[i]).append("\n");
                }
                return sb.toString().trim();
            case NO_SOLUTION:
                return "solusi tidak ada";
            case INFINITE:
                return result.getParametricDescription();
            default:
                return "";
        }
    }

    private static String teksUmum(String metode, String inputTeks, List<String> steps, String hasilAkhir) {
        StringBuilder sb = new StringBuilder();
        sb.append("Metode: ").append(metode).append("\n\n");
        sb.append("Input matriks:\n").append(inputTeks).append("\n");
        sb.append("\nLangkah-langkah:\n");
        for (String s : steps) {
            sb.append(s).append("\n");
        }
        sb.append("\nHasil:\n").append(hasilAkhir);
        return sb.toString();
    }
}