package algeo.core.spl;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.OBE;

public class Cramer {
    private static final double EPS = 1e-9;

    /**
     * Menyelesaikan SPL dengan kaidah Cramer
     * @param augmented matriks augmented [A|b], A harus persegi
     * @return hasil SPL beserta langkah-langkahnya
     * @throws MatrixException jika A tidak persegi atau det(A) = 0
     */
    public SolverOutput solve(Matrix augmented) throws MatrixException {
        int n = augmented.getRows();
        if (augmented.getCols() != n + 1) {
            throw new MatrixException("Kaidah Cramer tidak bisa dipakai karena jumlah persamaan tidak sama dengan jumlah variabel");
        }

        List<String> steps = new ArrayList<>();
        boolean catat = n <= 11;

        Matrix a = augmented.getSubmatrix(0, n - 1, 0, n - 1);
        double detA = OBE.hitungOBE(a);
        if (catat) {
            steps.add("Matriks A:\n" + a);
        }
        steps.add("det(A) = " + NumberFormatter.round(detA));

        if (Math.abs(detA) < EPS) {
            throw new MatrixException("Kaidah Cramer tidak bisa dipakai karena det(A) = 0");
        }

        double[] x = new double[n];
        for (int k = 0; k < n; k++) {
            Matrix ak = a.copy();
            for (int i = 0; i < n; i++) {
                ak.set(i, k, augmented.get(i, n));
            }
            double detAk = OBE.hitungOBE(ak);
            x[k] = detAk / detA;

            if (catat) {
                steps.add("Matriks A" + (k + 1) + " (kolom " + (k + 1) + " diganti b):\n" + ak);
            }
            steps.add("det(A" + (k + 1) + ") = " + NumberFormatter.round(detAk) + ", x" + (k + 1) + " = det(A" + (k + 1) + ") / det(A) = " + NumberFormatter.round(x[k]));
        }

        for (int i = 0; i < n; i++) {
            x[i] = NumberFormatter.round(x[i]);
        }
        return new SolverOutput(SPLResult.unique(x), steps);
    }
}