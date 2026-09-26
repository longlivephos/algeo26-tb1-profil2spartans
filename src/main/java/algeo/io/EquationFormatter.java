package algeo.io;

import algeo.core.util.NumberFormatter;

/**
 * Mengubah koefisien hasil interpolasi menjadi teks persamaan (polinom dan segmen spline).
 */
public class EquationFormatter {

    private EquationFormatter() {}

    /**
     * Ruas kanan polinom interpolasi, contoh: "25 + 2.893x + 0.07x^2".
     * @param koef koefisien dari Interpolasi.hitungInterpolasi, koef[i][0] = a_i
     * @return teks polinom (tanpa "P(x) =" karena sudah ditambahkan OutputWriter)
     */
    public static String polinom(double[][] koef) {
        StringBuilder sb = new StringBuilder();
        boolean adaSuku = false;
        for (int i = 0; i < koef.length; i++) {
            double a = NumberFormatter.round(koef[i][0]);
            if (a == 0) continue;
            String var = (i == 0) ? "" : (i == 1 ? "x" : "x^" + i);
            adaSuku = tambahSuku(sb, a, var, adaSuku);
        }
        if (!adaSuku) sb.append("0");
        return sb.toString().trim();
    }

    /**
     * Persamaan tiap segmen spline kubik beserta domainnya, satu segmen per baris.
     * @param x titik x yang sudah terurut naik
     * @param koef koefisien dari NaturalCubicSpline, koef[i] = {a, b, c, d}
     * @return teks semua segmen
     */
    public static String splineSegmen(double[] x, double[][] koef) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < koef.length; i++) {
            String var = selisih(x[i]);
            StringBuilder segmen = new StringBuilder();
            boolean adaSuku = false;
            for (int p = 0; p < 4; p++) {
                double c = NumberFormatter.round(koef[i][p]);
                if (c == 0) continue;
                String suku = (p == 0) ? "" : (p == 1 ? var : var + "^" + p);
                adaSuku = tambahSuku(segmen, c, suku, adaSuku);
            }
            if (!adaSuku) segmen.append("0");

            sb.append("S_").append(i).append("(x) = ").append(segmen.toString().trim());
            sb.append(",  untuk ").append(NumberFormatter.teks(x[i]))
              .append(" <= x <= ").append(NumberFormatter.teks(x[i + 1])).append("\n");
        }
        return sb.toString();
    }

    // menambahkan satu suku lengkap dengan tandanya, angka 1 di depan variabel tidak ditulis
    private static boolean tambahSuku(StringBuilder sb, double nilai, String var, boolean adaSuku) {
        if (adaSuku) {
            sb.append(nilai < 0 ? " - " : " + ");
        } else if (nilai < 0) {
            sb.append("-");
        }
        double mutlak = Math.abs(nilai);
        if (var.isEmpty() || mutlak != 1) {
            sb.append(NumberFormatter.teks(mutlak));
        }
        sb.append(var);
        return true;
    }

    // (x - xi), atau (x + |xi|) kalau xi negatif
    private static String selisih(double xi) {
        if (xi == 0) return "x";
        if (xi < 0) return "(x + " + NumberFormatter.teks(-xi) + ")";
        return "(x - " + NumberFormatter.teks(xi) + ")";
    }
}
