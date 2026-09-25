package algeo.core.spl;
import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.core.util.Toleransi;

public class GaussEliminationSolver {
    /**
     * Menyelesaikan SPL dengan eliminasi Gauss (partial pivoting) lalu substitusi mundur
     * @param augmented matriks augmented [A|b]
     * @return hasil SPL beserta langkah-langkahnya
     * @throws MatrixException jika matriks augmented kurang dari 2 kolom
     */
    public SolverOutput solve(Matrix augmented) throws MatrixException {
        if (augmented.getCols() < 2) {
            throw new MatrixException("Matriks augmented minimal punya 2 kolom");
        }
        List<String> steps = new ArrayList<>();
        Matrix m = augmented.copy();
        int rows = m.getRows();
        int n = m.getCols() - 1;
        boolean catat = rows <= 11 && n <= 11;
        if (catat) {
            steps.add("Matriks augmented awal:\n" + m);
        } else {
            steps.add("Matriks besar, langkah tidak ditampilkan.");
        }
        double tol = Toleransi.hitung(m, n);
        double tolB = Toleransi.hitung(m, n + 1) * 10;

        int[] kolomPivot = new int[rows];
        boolean[] adaPivot = new boolean[n];
        int baris = 0;

        for (int kol = 0; kol < n && baris < rows; kol++) {
            int maks = baris;
            for (int i = baris + 1; i < rows; i++) {
                if (Math.abs(m.get(i, kol)) > Math.abs(m.get(maks, kol))) {
                    maks = i;
                }
            }
            if (Toleransi.nol(m.get(maks, kol), tol)) {
                continue;
            }

            if (maks != baris) {
                for (int j = 0; j <= n; j++) {
                    double temp = m.get(baris, j);
                    m.set(baris, j, m.get(maks, j));
                    m.set(maks, j, temp);
                }
                if (catat) {
                    steps.add("Tukar baris " + (baris + 1) + " dengan baris " + (maks + 1) + ":\n" + m);
                }
            }

            double pivot = m.get(baris, kol);
            for (int j = kol; j <= n; j++) {
                m.set(baris, j, m.get(baris, j) / pivot);
            }
            m.set(baris, kol, 1);
            if (catat) {
                steps.add("Baris " + (baris + 1) + " = Baris " + (baris + 1) + " / " + NumberFormatter.teks(pivot) + ":\n" + m);
            }

            for (int i = baris + 1; i < rows; i++) {
                double faktor = m.get(i, kol);
                if (faktor == 0) continue;
                for (int j = kol; j <= n; j++) {
                    m.set(i, j, m.get(i, j) - faktor * m.get(baris, j));
                }
                m.set(i, kol, 0);
                if (catat) {
                    steps.add("Baris " + (i + 1) + " = Baris " + (i + 1) + " - (" + NumberFormatter.teks(faktor) + ") x Baris " + (baris + 1) + ":\n" + m);
                }
            }

            kolomPivot[baris] = kol;
            adaPivot[kol] = true;
            baris++;
        }
        int rank = baris;
        if (catat) {
            steps.add("Matriks eselon baris:\n" + m);
        }

        for (int i = rank; i < rows; i++) {
            if (!Toleransi.nol(m.get(i, n), tolB)) {
                steps.add("Baris " + (i + 1) + " berbentuk 0 = c (c tidak nol), SPL tidak punya solusi.");
                return new SolverOutput(SPLResult.noSolution(), steps);
            }
        }

        if (rank == n) {
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double sisa = m.get(i, n);
                for (int j = i + 1; j < n; j++) {
                    sisa = sisa - m.get(i, j) * x[j];
                }
                x[i] = sisa;
                if (catat) {
                    steps.add("x" + (i + 1) + " = " + NumberFormatter.teks(x[i]));
                }
            }
            steps.add("Substitusi mundur selesai, solusi tunggal ditemukan.");
            return new SolverOutput(SPLResult.unique(x), steps);
        }

        steps.add("Rank (" + rank + ") < jumlah variabel (" + n + "), SPL punya tak hingga solusi.");
        return new SolverOutput(SPLResult.infinite(bentukParametrik(m, n, rank, kolomPivot, adaPivot)), steps);
    }

    private String bentukParametrik(Matrix m, int n, int rank, int[] kolomPivot, boolean[] adaPivot) {
        int bebas = n - rank;
        double[][] x = new double[n][bebas + 1];

        int k = 0;
        for (int j = 0; j < n; j++) {
            if (!adaPivot[j]) {
                x[j][k] = 1;
                k++;
            }
        }

        for (int i = rank - 1; i >= 0; i--) {
            int p = kolomPivot[i];
            for (int c = 0; c <= bebas; c++) {
                double nilai = 0;
                if (c == bebas) nilai = m.get(i, n);
                for (int j = p + 1; j < n; j++) {
                    nilai = nilai - m.get(i, j) * x[j][c];
                }
                x[p][c] = nilai;
            }
        }

        return SPLFormatter.parametrik(x, adaPivot);
    }
}