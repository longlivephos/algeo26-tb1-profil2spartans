package algeo.core.regression;
import java.util.List;

import algeo.core.util.NumberFormatter;

public class RegressionOutput {
    private final double[] koefisien;
    private final double[] knot;
    private final int derajat;
    private final List<String> steps;

    public RegressionOutput(double[] koefisien, double[] knot, int derajat, List<String> steps) {
        this.koefisien = koefisien;
        this.knot = knot;
        this.derajat = derajat;
        this.steps = steps;
    }

    public double[] getKoefisien() { return koefisien; }
    public double[] getKnot() { return knot; }
    public int getDerajat() { return derajat; }
    public List<String> getSteps() { return steps; }

    /**
     * Menghitung prediksi y di titik xt dari persamaan regresi
     * @param xt nilai x yang mau diprediksi
     * @return nilai prediksi (belum dibulatkan)
     */
    public double prediksi(double xt) {
        double hasil = 0;
        for (int j = 0; j < koefisien.length; j++) {
            hasil = hasil + koefisien[j] * SplineRegression.basis(j, xt, knot, derajat);
        }
        return hasil;
    }

    /**
     * Membuat persamaan regresi dalam bentuk teks
     * @return persamaan regresi
     */
    public String getPersamaan() {
        StringBuilder sb = new StringBuilder();
        sb.append("y(x) =");
        boolean adaSuku = false;

        for (int j = 0; j < koefisien.length; j++) {
            double koef = koefisien[j];
            if (koef == 0) continue;
            String angka = NumberFormatter.teks(Math.abs(koef));
            if (j > 0 && angka.equals("1")) angka = "";
            
            if (adaSuku) {
                if (koef < 0) sb.append(" - ");
                else sb.append(" + ");
            } else {
                sb.append(koef < 0 ? " -" : " ");
            }
            sb.append(angka);

            if (j == 1) {
                sb.append("x");
            } else if (j > 1 && j <= derajat) {
                sb.append("x^").append(j);
            } else if (j > derajat) {
                double k = knot[j - derajat - 1];
                if (k < 0) sb.append("(x + ").append(NumberFormatter.teks(-k)).append(")_+");
                else sb.append("(x - ").append(NumberFormatter.teks(k)).append(")_+");
                if (derajat > 1) sb.append("^").append(derajat);
            }
            adaSuku = true;
        }
        if (!adaSuku) sb.append(" 0");
        return sb.toString();
    }
}