package algeo.core.regression;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.GaussEliminationSolver;
import algeo.core.spl.SPLResult;
import algeo.core.spl.SolverOutput;

public class SplineRegression {
    private static final int BATAS_TITIK = 10;
    private static final double BATAS_PUSAT = 10;

    /**
     * Regresi spline kubik (derajat 3) dengan truncated power basis
     * @param titik matriks titik data berukuran n x 2 (kolom 1 = x, kolom 2 = y)
     * @param knot posisi knot, harus terurut naik (boleh kosong atau null)
     * @return koefisien regresi beserta langkah-langkahnya
     * @throws MatrixException jika input tidak valid atau X^T X tidak punya solusi tunggal
     */

    public RegressionOutput solve(Matrix titik, double[] knot) throws MatrixException {
        return solve(titik, knot, 3);
    }

    /**
     * Regresi spline dengan truncated power basis dan persamaan normal (X^T X) b = X^T y
     * @param titik matriks titik data berukuran n x 2 (kolom 1 = x, kolom 2 = y)
     * @param knot posisi knot, harus terurut naik (boleh kosong atau null)
     * @param derajat derajat spline (1 = linear, 2 = kuadrat, 3 = kubik)
     * @return koefisien regresi beserta langkah-langkahnya
     * @throws MatrixException jika input tidak valid atau X^T X tidak punya solusi tunggal
     */
    public RegressionOutput solve(Matrix titik, double[] knot, int derajat) throws MatrixException {
        if (knot == null) {
            knot = new double[0];
        }
        validasi(titik, knot, derajat);

        int n = titik.getRows();
        int k = knot.length;
        int m = derajat + 1 + k;
        List<String> steps = new ArrayList<>();
        steps.add("Jumlah titik N = " + n + ", jumlah knot K = " + k + ", derajat = " + derajat + ", jumlah basis M = " + m);

        double xMin = titik.get(0, 0);
        double xMax = titik.get(0, 0);
        for (int i = 1; i < n; i++) {
            xMin = Math.min(xMin, titik.get(i, 0));
            xMax = Math.max(xMax, titik.get(i, 0));
        }
        double c = (xMin + xMax) / 2;
        double s = (xMax - xMin) / 2;
        steps.add("Transformasi u = (x - " + RegressionOutput.teksAngka(c) + ") / " + RegressionOutput.teksAngka(s) + " supaya persamaan normal stabil secara numerik");

        double[] knotU = new double[k];
        for (int j = 0; j < k; j++) {
            knotU[j] = (knot[j] - c) / s;
        }

        Matrix x = new Matrix(n, m);
        Matrix y = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            double u = (titik.get(i, 0) - c) / s;
            for (int j = 0; j < m; j++) {
                x.set(i, j, basis(j, u, knotU, derajat));
            }
            y.set(i, 0, titik.get(i, 1));
        }
        steps.add("Matriks desain X (dalam u):\n" + x);
        steps.add("Vektor y:\n" + y);

        Matrix xt = x.transpose();
        Matrix xtx = xt.multiply(x);
        Matrix xty = xt.multiply(y);
        steps.add("X^T X:\n" + xtx);
        steps.add("X^T y:\n" + xty);

        Matrix aug = new Matrix(m, m + 1);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                aug.set(i, j, xtx.get(i, j));
            }
            aug.set(i, m, xty.get(i, 0));
        }
        steps.add("Selesaikan persamaan normal (X^T X) b = X^T y dengan eliminasi Gauss:");
        SolverOutput hasil = new GaussEliminationSolver().solve(aug);
        steps.addAll(hasil.getSteps());
        if (hasil.getResult().getType() != SPLResult.ResultType.UNIQUE) {
            throw new MatrixException("Regresi tidak bisa dipakai karena X^T X tidak punya balikan " + "(pastikan tiap selang antar-knot berisi cukup titik data)");
        }
        double[] bU = hasil.getResult().getSolution();
        StringBuilder sb = new StringBuilder("Koefisien dalam u (x1, x2, ... pada SPL di atas):");
        for (int j = 0; j < m; j++) {
            sb.append(" b").append(j).append(" = ").append(RegressionOutput.teksAngka(bU[j])).append(";");
        }
        steps.add(sb.toString());
        double[] polinom = new double[derajat + 1];
        for (int j = 0; j <= derajat; j++) {
            polinom[j] = bU[j] / pangkat(s, j);
        }
        double[] koefKnot = new double[k];
        for (int j = 0; j < k; j++) {
            koefKnot[j] = bU[derajat + 1 + j] / pangkat(s, derajat);
        }

        boolean bentukGeser = Math.abs(c) > BATAS_PUSAT * s;
        RegressionOutput output = new RegressionOutput(polinom, koefKnot, knot, derajat, c, bentukGeser, steps);
        steps.add("Persamaan regresi: " + output.getPersamaan());
        return output;
    }

    private void validasi(Matrix titik, double[] knot, int derajat) throws MatrixException {
        if (titik.getCols() != 2) {
            throw new MatrixException("Data titik harus terdiri dari 2 kolom (x dan y)");
        }
        if (derajat < 1 || derajat > 3) {
            throw new MatrixException("Derajat spline hanya boleh 1, 2, atau 3");
        }
        int n = titik.getRows();
        if (n > BATAS_TITIK) {
            throw new MatrixException("Jumlah titik data maksimal " + BATAS_TITIK);
        }
        int m = derajat + 1 + knot.length;
        if (n < m) {
            throw new MatrixException("Regresi tidak bisa dipakai karena jumlah titik (" + n + ") kurang dari jumlah basis (" + m + ")");
        }

        double xMin = titik.get(0, 0);
        double xMax = titik.get(0, 0);
        for (int i = 0; i < n; i++) {
            double xi = titik.get(i, 0);
            double yi = titik.get(i, 1);
            if (Double.isNaN(xi) || Double.isInfinite(xi) || Double.isNaN(yi) || Double.isInfinite(yi)) {
                throw new MatrixException("Data titik tidak boleh berisi NaN atau Infinity");
            }
            xMin = Math.min(xMin, xi);
            xMax = Math.max(xMax, xi);
        }
        if (xMin == xMax) {
            throw new MatrixException("Nilai x tidak boleh sama semua");
        }

        for (int j = 0; j < knot.length; j++) {
            if (Double.isNaN(knot[j]) || Double.isInfinite(knot[j])) {
                throw new MatrixException("Posisi knot tidak boleh NaN atau Infinity");
            }
            if (j > 0 && knot[j] <= knot[j - 1]) {
                throw new MatrixException("Posisi knot harus terurut naik dan tidak boleh sama");
            }
            if (knot[j] <= xMin || knot[j] >= xMax) {
                throw new MatrixException("Knot " + RegressionOutput.teksAngka(knot[j]) + " harus berada di antara x terkecil (" + RegressionOutput.teksAngka(xMin) + ") dan x terbesar (" + RegressionOutput.teksAngka(xMax) + ")");
            }
        }
    }

    /**
     * Menghitung nilai fungsi basis ke-j di titik t
     * @param j indeks basis (0 sampai derajat untuk polinomial, sisanya untuk knot)
     * @param t nilai x (atau u)
     * @param knot posisi knot (dalam satuan yang sama dengan t)
     * @param derajat derajat spline
     * @return nilai basis ke-j
     */
    public static double basis(int j, double t, double[] knot, int derajat) {
        if (j <= derajat) {
            return pangkat(t, j);
        }
        double selisih = t - knot[j - derajat - 1];
        if (selisih <= 0) {
            return 0;
        }
        return pangkat(selisih, derajat);
    }

    static double pangkat(double a, int p) {
        double hasil = 1;
        for (int i = 0; i < p; i++) {
            hasil = hasil * a;
        }
        return hasil;
    }
}