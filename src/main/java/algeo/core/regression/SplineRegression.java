package algeo.core.regression;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.InverseMethod;

public class SplineRegression {

    /**
     * Menyelesaikan regresi spline dengan truncated power basis dan persamaan normal
     * @param titik matriks titik data berukuran n x 2 (kolom 1 = x, kolom 2 = y)
     * @param knot posisi knot, harus terurut naik (boleh kosong)
     * @param derajat derajat spline (1 = linear, 2 = kuadrat, 3 = kubik)
     * @return koefisien regresi beserta langkah-langkahnya
     * @throws MatrixException jika input tidak valid atau X^T X tidak punya balikan
     */
    public RegressionOutput solve(Matrix titik, double[] knot, int derajat) throws MatrixException {
        if (titik.getCols() != 2) {
            throw new MatrixException("Data titik harus terdiri dari 2 kolom (x dan y)");
        }
        if (derajat < 1 || derajat > 3) {
            throw new MatrixException("Derajat spline hanya boleh 1, 2, atau 3");
        }
        for (int k = 1; k < knot.length; k++) {
            if (knot[k] <= knot[k - 1]) {
                throw new MatrixException("Posisi knot harus terurut naik dan tidak boleh sama");
            }
        }

        int n = titik.getRows();
        int m = derajat + 1 + knot.length;
        if (n < m) {
            throw new MatrixException("Regresi tidak bisa dipakai karena jumlah titik (" + n + ") kurang dari jumlah basis (" + m + ")");
        }

        List<String> steps = new ArrayList<>();
        Matrix x = new Matrix(n, m);
        Matrix y = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x.set(i, j, basis(j, titik.get(i, 0), knot, derajat));
            }
            y.set(i, 0, titik.get(i, 1));
        }
        steps.add("Matriks desain X:\n" + x);
        steps.add("Vektor y:\n" + y);

        Matrix xt = x.transpose();
        Matrix xtx = xt.multiply(x);
        Matrix xty = xt.multiply(y);
        steps.add("X^T X:\n" + xtx);
        steps.add("X^T y:\n" + xty);

        Matrix inv;
        try {
            inv = new InverseMethod().cariInvers(xtx, steps, false);
        } catch (MatrixException e) {
            throw new MatrixException("Regresi tidak bisa dipakai karena X^T X tidak punya balikan (cek posisi knot atau nilai x yang kembar)");
        }
        steps.add("(X^T X)^-1:\n" + inv);

        Matrix hasil = inv.multiply(xty);
        double[] beta = new double[m];
        for (int j = 0; j < m; j++) {
            beta[j] = hasil.get(j, 0);
        }
        steps.add("beta = (X^T X)^-1 X^T y:\n" + hasil);
        return new RegressionOutput(beta, knot, derajat, steps);
    }

    /**
     * Menghitung nilai fungsi basis ke-j di titik t
     * @param j indeks basis (0 sampai derajat untuk polinomial, sisanya untuk knot)
     * @param t nilai x
     * @param knot posisi knot
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

    private static double pangkat(double a, int p) {
        double hasil = 1;
        for (int i = 0; i < p; i++) {
            hasil = hasil * a;
        }
        return hasil;
    }
}