package algeo.core.spl;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.core.util.Toleransi;

public class InverseMethod {
    /**
     * Menyelesaikan SPL dengan metode matriks balikan (x = A^-1 b)
     * @param augmented matriks augmented [A|b], A harus persegi
     * @return hasil SPL beserta langkah-langkahnya
     * @throws MatrixException jika A tidak persegi atau tidak punya balikan
     */
    public SolverOutput solve(Matrix augmented) throws MatrixException {
        int n = augmented.getRows();
        if (augmented.getCols() != n + 1) {
            throw new MatrixException("Metode matriks balikan tidak bisa dipakai karena jumlah persamaan tidak sama dengan jumlah variabel");
        }

        List<String> steps = new ArrayList<>();
        boolean catat = n <= 11;

        Matrix a = augmented.getSubmatrix(0, n - 1, 0, n - 1);
        Matrix b = augmented.getSubmatrix(0, n - 1, n, n);
        if (catat) {
            steps.add("Matriks A:\n" + a);
            steps.add("Vektor b:\n" + b);
        } else {
            steps.add("Matriks besar, langkah tidak ditampilkan.");
        }

        Matrix inv;
        try {
            inv = cariInvers(a, steps, catat);
        } catch (MatrixException e) {
            throw new MatrixException("Metode matriks balikan tidak bisa dipakai karena det(A) = 0 (A tidak punya balikan)");
        }
        if (catat) {
            steps.add("Matriks balikan A^-1:\n" + inv);
        }

        Matrix hasil = inv.multiply(b);
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = hasil.get(i, 0);
        }
        steps.add("x = A^-1 * b, solusi tunggal ditemukan.");
        return new SolverOutput(SPLResult.unique(x), steps);
    }

    /**
     * Mencari invers matriks persegi dengan Gauss-Jordan (partial pivoting) pada [A|I]
     * @param a matriks persegi
     * @param steps tempat menyimpan langkah (boleh kosong)
     * @param catat true jika langkah OBE mau dicatat
     * @return matriks balikan dari a
     * @throws MatrixException jika a tidak persegi atau tidak punya balikan
     */
    public Matrix cariInvers(Matrix a, List<String> steps, boolean catat) throws MatrixException {
        if (!a.isSquare()) {
            throw new MatrixException("Matriks tidak persegi, tidak memiliki balikan");
        }
        int n = a.getRows();
        double tol = Toleransi.hitung(a);

        Matrix m = new Matrix(n, 2 * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m.set(i, j, a.get(i, j));
            }
            m.set(i, n + i, 1);
        }
        if (catat) {
            steps.add("Matriks [A | I]:\n" + m);
        }

        for (int kol = 0; kol < n; kol++) {
            int maks = kol;
            for (int i = kol + 1; i < n; i++) {
                if (Math.abs(m.get(i, kol)) > Math.abs(m.get(maks, kol))) {
                    maks = i;
                }
            }
            if (Toleransi.nol(m.get(maks, kol), tol)) {
                throw new MatrixException("Matriks tidak memiliki balikan");
            }

            if (maks != kol) {
                for (int j = 0; j < 2 * n; j++) {
                    double temp = m.get(kol, j);
                    m.set(kol, j, m.get(maks, j));
                    m.set(maks, j, temp);
                }
                if (catat) {
                    steps.add("Tukar baris " + (kol + 1) + " dengan baris " + (maks + 1) + ":\n" + m);
                }
            }

            double pivot = m.get(kol, kol);
            for (int j = 0; j < 2 * n; j++) {
                m.set(kol, j, m.get(kol, j) / pivot);
            }
            m.set(kol, kol, 1);
            if (catat) {
                steps.add("Baris " + (kol + 1) + " = Baris " + (kol + 1) + " / " + NumberFormatter.teks(pivot) + ":\n" + m);
            }

            for (int i = 0; i < n; i++) {
                if (i == kol) continue;
                double faktor = m.get(i, kol);
                if (faktor == 0) continue;
                for (int j = 0; j < 2 * n; j++) {
                    m.set(i, j, m.get(i, j) - faktor * m.get(kol, j));
                }
                m.set(i, kol, 0);
                if (catat) {
                    steps.add("Baris " + (i + 1) + " = Baris " + (i + 1) + " - (" + NumberFormatter.teks(faktor) + ") x Baris " + (kol + 1) + ":\n" + m);
                }
            }
        }

        return m.getSubmatrix(0, n - 1, n, 2 * n - 1);
    }
}