package algeo.core.interpolasi;

import java.util.ArrayList;
import java.util.List;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.GaussJordan;
import algeo.core.spl.SolverOutput;

public class NaturalCubicSpline {

    /**
     * @param x array dari x
     * @param y array dari y
     * @return array 2D ukuran [n][4] -> koef [a, b, c, d] per segman
     * @throws MatrixException jika terjadi kesalahan operasi matriks
     * 
     */

    public static InterpolasiSolverOutput hitungSpline(double[] x, double[] y) {
        List<String> steps = new ArrayList<>();

        // validate input
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            steps.add("Jumlah titik minimal 2 dan ukuran x & y harus sama.");
            return new InterpolasiSolverOutput(
                InterpolasiResult.failed("Data input tidak valid"), 
                steps
            );
        }

        // n = # dari interpolating spline (always 1 less than the number of data points)
        int n = x.length-1;
        steps.add("Jumlah titik data = " + (n+1) + ", sehingga terbentuk " + n + " segmen polinomial kubik");

        // # dari unknown coefficients akan selalu berjumlah 4n, maka kita harus mencari 4n linear equations to solve it

        try {
            // cari interval (h) antara titik-titik x
            double[] h = new double[n];
            for(int i = 0; i < n; i++){
                h[i] = x[i+1] - x[i];
                if (h[i] <= 0){
                    steps.add("Nilai x harus terurut naik");
                    return new InterpolasiSolverOutput(InterpolasiResult.failed("Data x tidak terurut naik"), steps);
                }
                steps.add(String.format("h[%d] = %.4f", i, h[i]));
            }

            //  menyederhanakan 4n variabel jadi 1 sistem persamaan tridiagonal
            steps.add("Menyusun matriks augmented SPL tridiagonal");
            Matrix aug = new Matrix(n+1, n+2);

            // khusus natural spline: diasumsikan turunan kedua ujung2 sama dgn 0
            aug.set(0, 0, 1.0);
            aug.set(0, n+1, 0.0);
            steps.add("Syarat batas ujung kiri: c_0 = 0");
            steps.add("Baris 1: [1, 0, ..., 0 | 0");
            aug.set(n, n, 1.0);
            aug.set(n, n+1, 0.0);
            steps.add("Syarat batas ujung kanan: c_" + n + " = 0");
            steps.add("Baris " + (n+1) + ": [0, 0, ..., 1 | 0");

            // isi tengah matriks
            for (int i = 1; i < n; i++){
                // kolom sebelah kiri diagonal
                aug.set(i, i-1, h[i-1]);
                // kolom diagonal utama
                aug.set(i, i, 2.0 * (h[i-1] + h[i]));
                // kolom sebelah kanan diagonal
                aug.set(i, i+1, h[i]);
                // kolom konstanta b
                double rhs = 3.0 * ((y[i+1] - y[i])/h[i] - (y[i] - y[i-1])/h[i-1]);
                aug.set(i, n+1, rhs);

                steps.add(String.format("Persamaan baris-%d: %.4f*c_%d + %.4f*c_%d + %.4f*c_%d = %.4f",
                            (i + 1), h[i - 1], (i - 1), 2.0 * (h[i - 1] + h[i]), i, h[i], (i + 1), rhs));
            }

            // gunakan gauss jordan
            steps.add("Selesaikan SPL dengan Gauss-Jordan");
            GaussJordan solver = new GaussJordan();
            SolverOutput solverOutput = solver.solve(aug);

            steps.addAll(solverOutput.getSteps());
            double[] c = solverOutput.getResult().getSolution();
            if (c == null) {
                steps.add("Tidak ditemukan solusi tunggal untuk nilai c");
                return new InterpolasiSolverOutput(
                    InterpolasiResult.failed("SPL koefisien c tidak memiliki solusi tunggal."), 
                    steps
                );
            }   

            // nilai turunan kedua pada tiap knot
            steps.add("Nilai turunan kedua pada setiap knot: ");
            for (int i = 0; i <= n; i++) {
                double turunanKedua = 2.0 * c[i];
                steps.add(String.format("  Knot x_%d (%.2f) -> S''(x) = 2 * c_%d = %.6f", i, x[i], i, turunanKedua));
            }

            // hitung koefisien a, b, d tiap segmen
            steps.add("Hitung koefisien a, b, d per segmen");
            double[][] koefisien = new double[n][4]; // biat matriks n x 4 utk simpen koef

            for (int i = 0; i < n; i++) {
                double a = y[i]; // nilai a[i] sama dengan y[i] karena x=x
                double b = (y[i + 1] - y[i]) / h[i] - h[i] * (2.0 * c[i] + c[i + 1]) / 3.0; // kemiringan, syarat kontinuitas turunan kedua
                double d = (c[i + 1] - c[i]) / (3.0 * h[i]); // seberapa cepat kelengkungan berubah, syarat kurva segmen harus menyentuh titik ujung kanan

                koefisien[i][0] = a;
                koefisien[i][1] = b;
                koefisien[i][2] = c[i];
                koefisien[i][3] = d;

                steps.add(String.format("Segmen %d [%.2f <= x <= %.2f]:", (i + 1), x[i], x[i + 1]));
                steps.add(String.format("  a_%d = %.6f", i, a));
                steps.add(String.format("  b_%d = %.6f", i, b));
                steps.add(String.format("  c_%d = %.6f", i, c[i]));
                steps.add(String.format("  d_%d = %.6f", i, d));
                steps.add(String.format("  S_%d(x) = %.4f + %.4f*(x - %.2f) + %.4f*(x - %.2f)^2 + %.4f*(x - %.2f)^3",
                        i, a, b, x[i], c[i], x[i], d, x[i]));
            }

            return new InterpolasiSolverOutput(
                InterpolasiResult.success(koefisien, x, y), 
                steps
            );

        } catch (MatrixException e) {
            steps.add("Terjadi kesalahan operasi matriks: " + e.getMessage());
            return new InterpolasiSolverOutput(
                InterpolasiResult.failed(e.getMessage()), 
                steps
            );
        }
    }

    // evaluasi nilai y terinterpolasi utk nilai xVal tertentu
    public static double evaluasi(double[] x, double[][] koefisien, double xVal) {
        int n = x.length - 1;

        int i = 0;
        if (xVal <= x[0]) {
            i = 0;
        } else if (xVal >= x[n]) {
            i = n - 1;
        } else {
            for (int k = 0; k < n; k++) {
                if (xVal >= x[k] && xVal <= x[k + 1]) {
                    i = k;
                    break;
                }
            }
        }

        double dx = xVal - x[i];
        double a = koefisien[i][0];
        double b = koefisien[i][1];
        double c = koefisien[i][2];
        double d = koefisien[i][3];

        return a + (b * dx) + (c * dx * dx) + (d * dx * dx * dx);
    }
}