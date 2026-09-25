package algeo.core.spl;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.core.util.Toleransi;

public class Cramer {
    private static final int BATAS_N = 300;

    /**
     * Menyelesaikan SPL dengan kaidah Cramer
     * @param augmented matriks augmented [A|b], A harus persegi
     * @return hasil SPL beserta langkah-langkahnya
     * @throws MatrixException jika A tidak persegi, terlalu besar, atau det(A) = 0
     */
    public SolverOutput solve(Matrix augmented) throws MatrixException {
        int n = augmented.getRows();
        if (augmented.getCols() != n + 1) {
            throw new MatrixException("Kaidah Cramer tidak bisa dipakai karena jumlah persamaan tidak sama dengan jumlah variabel");
        }
        if (n > BATAS_N) {
            throw new MatrixException("Kaidah Cramer dibatasi maksimal " + BATAS_N + " variabel karena terlalu lama, gunakan eliminasi Gauss/Gauss-Jordan");
        }

        List<String> steps = new ArrayList<>();
        boolean catat = n <= 11;

        Matrix a = augmented.getSubmatrix(0, n - 1, 0, n - 1);
        double tol = Toleransi.hitung(a);
        double[] detA = determinan(a, tol);
        if (catat) {
            steps.add("Matriks A:\n" + a);
        }
        steps.add("det(A) = " + teksDet(detA));

        if (detA[0] == 0) {
            throw new MatrixException("Kaidah Cramer tidak bisa dipakai karena det(A) = 0");
        }

        double[] x = new double[n];
        for (int k = 0; k < n; k++) {
            Matrix ak = a.copy();
            for (int i = 0; i < n; i++) {
                ak.set(i, k, augmented.get(i, n));
            }
            double[] detAk = determinan(ak, 0);
            x[k] = detAk[0] / detA[0] * pangkat10((int) (detAk[1] - detA[1])); // x = (m1*10^e1) / (m2*10^e2) = (m1/m2) * 10^(e1-e2)

            if (catat) {
                steps.add("Matriks A" + (k + 1) + " (kolom " + (k + 1) + " diganti b):\n" + ak);
            }
            steps.add("det(A" + (k + 1) + ") = " + teksDet(detAk) + ", x" + (k + 1) + " = det(A" + (k + 1) + ") / det(A) = " + NumberFormatter.teks(x[k]));
        }

        return new SolverOutput(SPLResult.unique(x), steps);
    }

    /**
     * Menghitung determinan dengan reduksi baris, tapi hasilnya disimpan sebagai mantisa x 10^eksponen
     * @param matriks matriks persegi
     * @param tol pivot dengan |pivot| <= tol dianggap 0
     * @return {mantisa, eksponen}, mantisa = 0 jika matriks singular
     */
    private double[] determinan(Matrix matriks, double tol) {
        int n = matriks.getRows();
        double[][] t = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                t[i][j] = matriks.get(i, j);
            }
        }

        double mantisa = 1;
        int eksponen = 0;
        for (int i = 0; i < n; i++) {
            int pivotRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(t[j][i]) > Math.abs(t[pivotRow][i])) {
                    pivotRow = j;
                }
            }
            if (Toleransi.nol(t[pivotRow][i], tol)) {
                return new double[] {0, 0};
            }
            if (pivotRow != i) {
                double[] temp = t[i];
                t[i] = t[pivotRow];
                t[pivotRow] = temp;
                mantisa = -mantisa;
            }
            for (int j = i + 1; j < n; j++) {
                double faktor = t[j][i] / t[i][i];
                if (faktor == 0) continue;
                for (int k = i; k < n; k++) {
                    t[j][k] -= faktor * t[i][k];
                }
            }

            mantisa *= t[i][i];
            if (mantisa == 0) {
                return new double[] {0, 0};
            }
            while (Math.abs(mantisa) >= 10) {
                mantisa /= 10;
                eksponen++;
            }
            while (Math.abs(mantisa) < 1) {
                mantisa *= 10;
                eksponen--;
            }
        }
        return new double[] {mantisa, eksponen};
    }

    private double pangkat10(int e) {
        double hasil = 1;
        if (e >= 0) {
            for (int i = 0; i < e; i++) hasil *= 10;
        } else {
            for (int i = 0; i < -e; i++) hasil /= 10;
        }
        return hasil;
    }

    private String teksDet(double[] det) {
        if (det[0] == 0) {
            return "0";
        }
        if (det[1] >= -3 && det[1] <= 14) {
            return NumberFormatter.teks(det[0] * pangkat10((int) det[1]));
        }
        return NumberFormatter.teks(det[0]) + " x 10^" + (int) det[1];
    }
}