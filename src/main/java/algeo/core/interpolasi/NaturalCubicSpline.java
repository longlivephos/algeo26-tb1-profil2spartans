package algeo.core.interpolasi;

import java.util.ArrayList;
import java.util.List;
import algeo.core.matrix.Matrix;

public class NaturalCubicSpline {

    /**
     * @param x array dari x
     * @param y array dari y
     */

    public static double[][] hitungSpline(double[] x, double[] y) {
        List<String> steps = new ArrayList<>();

        // n = # dari interpolating spline (selalu 1 less than the number of data points)
        int n = x.length-1;

        steps.add("Jumlah titik data = " + (n+1) + ", sehingga terbentuk " + n + " segmen polinomial kubik.");

        // # dari unknown coefficients akan selalu berjumlah 4n, maka kita harus mencari 4n linear equations to solve it

        // cari interval (h) antara titik-titik x
        double[] h = new double[n];
        for(int i = 0; i < n; i++){
            h[i] = x[i+1] - x[i];
            steps.add(String.format("h[%d] = %.4f", i, h[i]));
        }

        //  menyederhanakan 4n variabel jadi 1 sistem persamaan tridiagonal
        Matrix aug = new Matrix(n+1, n+2);

        // khusus natural spline: diasumsikan turunan kedua ujung2 sama dgn 0
        aug.set(0, 0, 1.0);
        aug.set(0, n+1, 0.0);
        aug.set(0, n, 1.0);
        aug.set(n, n+1, 0.0);

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

            // to dos: selesain SPL
        }
            return null;
    }
}
