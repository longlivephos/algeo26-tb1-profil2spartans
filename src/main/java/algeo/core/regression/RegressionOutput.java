package algeo.core.regression;
import java.util.List;
import java.util.Locale;

import algeo.core.util.NumberFormatter;

public class RegressionOutput {
    private final double[] polinom;
    private final double[] koefKnot;
    private final double[] knot;
    private final int derajat;
    private final double pusat;
    private final boolean bentukGeser;
    private final double[] koefisienTampil;
    private final List<String> steps;

    /**
     * @param polinom koefisien suku (x - pusat)^j untuk j = 0..derajat
     * @param koefKnot koefisien suku (x - knot)_+^derajat
     * @param knot posisi knot
     * @param derajat derajat spline
     * @param pusat titik tengah data (c)
     * @param bentukGeser true jika persamaan ditampilkan dalam (x - c)
     * @param steps langkah-langkah perhitungan
     */
    public RegressionOutput(double[] polinom, double[] koefKnot, double[] knot, int derajat, double pusat, boolean bentukGeser, List<String> steps) {
        this.polinom = polinom;
        this.koefKnot = koefKnot;
        this.knot = knot;
        this.derajat = derajat;
        this.pusat = pusat;
        this.bentukGeser = bentukGeser;
        this.steps = steps;

        double[] polTampil = bentukGeser ? polinom.clone() : keBentukBiasa(polinom, pusat);
        koefisienTampil = new double[derajat + 1 + koefKnot.length];
        System.arraycopy(polTampil, 0, koefisienTampil, 0, derajat + 1);
        System.arraycopy(koefKnot, 0, koefisienTampil, derajat + 1, koefKnot.length);
        NumberFormatter.bersihkanGalat(koefisienTampil);
    }

    /**
     * Koefisien regresi sesuai bentuk persamaan yang ditampilkan, contohnya seperti [b0, b1, ..., b_derajat, koefisien knot 1, ..., koefisien knot K]
     * @return array koefisien (sisa galat sangat kecil sudah dibuat 0)
     */
    public double[] getKoefisien() { return koefisienTampil; }
    public double[] getKnot() { return knot; }
    public int getDerajat() { return derajat; }
    public List<String> getSteps() { return steps; }

    /** @return true jika persamaan ditulis dalam (x - c), bukan x biasa */
    public boolean isBentukGeser() { return bentukGeser; }

    /** @return nilai c yang dipakai pada bentuk (x - c) */
    public double getPusat() { return pusat; }

    /**
     * Menghitung prediksi y di titik xt dari persamaan regresi
     * @param xt nilai x yang mau diprediksi
     * @return nilai prediksi (belum dibulatkan)
     */
    public double prediksi(double xt) {
        double hasil = 0;
        for (int j = 0; j <= derajat; j++) {
            hasil = hasil + polinom[j] * SplineRegression.pangkat(xt - pusat, j);
        }
        for (int j = 0; j < knot.length; j++) {
            double selisih = xt - knot[j];
            if (selisih > 0) {
                hasil = hasil + koefKnot[j] * SplineRegression.pangkat(selisih, derajat);
            }
        }
        return hasil;
    }

    /**
     * Membuat persamaan regresi dalam bentuk teks
     * @return persamaan regresi, contohnya seperti "y(x) = 2x - 3(x - 3)_+"
     */
    public String getPersamaan() {
        StringBuilder sb = new StringBuilder();
        sb.append("y(x) =");
        boolean adaSuku = false;

        String var = "x";
        if (bentukGeser) {
            if (pusat < 0) var = "(x + " + teksAngka(-pusat) + ")";
            else var = "(x - " + teksAngka(pusat) + ")";
        }

        for (int j = 0; j < koefisienTampil.length; j++) {
            double koef = koefisienTampil[j];
            if (koef == 0) continue;
            String angka = teksAngka(Math.abs(koef));
            if (j > 0 && angka.equals("1")) angka = "";

            if (adaSuku) {
                sb.append(koef < 0 ? " - " : " + ");
            } else {
                sb.append(koef < 0 ? " -" : " ");
            }
            sb.append(angka);

            if (j == 1) {
                sb.append(var);
            } else if (j > 1 && j <= derajat) {
                sb.append(var).append("^").append(j);
            } else if (j > derajat) {
                double k = knot[j - derajat - 1];
                if (k < 0) sb.append("(x + ").append(teksAngka(-k)).append(")_+");
                else sb.append("(x - ").append(teksAngka(k)).append(")_+");
                if (derajat > 1) sb.append("^").append(derajat);
            }
            adaSuku = true;
        }
        if (!adaSuku) sb.append(" 0");
        return sb.toString();
    }

    static String teksAngka(double v) {
        if (v != 0 && NumberFormatter.round(v) == 0) {
            return String.format(Locale.US, "%.3e", v);
        }
        return NumberFormatter.teks(v);
    }

    private static double[] keBentukBiasa(double[] b, double c) {
        int d = b.length - 1;
        double[] a = new double[d + 1];
        for (int j = 0; j <= d; j++) {
            double kombinasi = 1; // (x - c)^j = sum_{i=0..j} C(j,i) x^i (-c)^(j-i)
            for (int i = 0; i <= j; i++) {
                a[i] = a[i] + b[j] * kombinasi * SplineRegression.pangkat(-c, j - i);
                kombinasi = kombinasi * (j - i) / (i + 1);
            }
        }
        return a;
    }
}