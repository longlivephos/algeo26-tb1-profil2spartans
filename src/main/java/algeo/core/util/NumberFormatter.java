package algeo.core.util;
import java.util.Locale;
public class NumberFormatter {
    /**
     * Membulatkan angka ke 3 angka di belakang koma
     * @param value angka yang mau dibulatkan
     * @return angka hasil pembulatan
     */
    public static double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        if (Math.abs(value) >= 1e15) {
            return value;
        }
        double hasil = Math.round(value * 1000.0) / 1000.0;
        if (hasil == 0) {
            return 0.0;
        }
        return hasil;
    }

    /**
     * Mengubah angka jadi teks untuk ditampilkan (maksimal 3 angka di belakang koma)
     * Angka bukan nol yang terlalu kecil ditulis dengan notasi e supaya tidak terbaca 0, berlaku juga dengan angka yang sayngat besar
     * @param value angka yang mau ditampilkan
     * @return teks angka, contoh: "2.5", "-3", "1.235e-05"
     */
    public static String teks(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        if (value != 0 && (Math.abs(value) < 0.0005 || Math.abs(value) >= 1e15)) {
            return String.format(Locale.US, "%.3e", value);
        }

        String s = String.format(Locale.US, "%.3f", round(value));
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.equals("-0")) {
            s = "0";
        }
        return s;
    }

    /**
     * Mengubah koefisien yang sangat kecil dibanding koefisien terbesar menjadi 0.
     * @param koef array koefisien
     */
    public static void bersihkanGalat(double[] koef) {
        double terbesar = 0;
        for (double k : koef) {
            if (Math.abs(k) > terbesar) {
                terbesar = Math.abs(k);
            }
        }
        for (int i = 0; i < koef.length; i++) {
            if (Math.abs(koef[i]) < 1e-12 * terbesar) {
                koef[i] = 0;
            }
        }
    }
}