package algeo.inverse;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;

public class Augmentasi {

    /**
     * Menghitung invers dari suatu matriks persegi menggunakan metode Augmentasi (OBE / Gauss-Jordan).
     *
     * @param matriks Matriks persegi yang akan dihitung inversnya.
     * @return InverseSolverOutput objek hasil perhitungan beserta langkah-langkahnya.
     * @throws MatrixException Jika matriks tidak berbentuk persegi.
     */

    public static InverseSolverOutput hitungInversAugmentasi(Matrix matriks) throws MatrixException {
        List<String> steps = new ArrayList<>();

        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung invers");
        }

        int n = matriks.getRows();
        
        // matrix augmented [A | I] ukuran N x 2N
        double[][] tempMatriks = new double[n][2 * n];

        // matriks A ke kiri,  matriks I ke kanan
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tempMatriks[i][j] = matriks.get(i, j);
            }
            tempMatriks[i][n + i] = 1.0; // elemen diagonal utk matriks identitas
        }
        steps.add("Matriks augmented awal [A | I]:\n" + formatAugmented(tempMatriks, n));

        // tahap 1 OBE
        for (int i = 0; i < n; i++) {
            // cari pivot
            int pivotRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(tempMatriks[j][i]) > Math.abs(tempMatriks[pivotRow][i])) {
                    pivotRow = j;
                }
            }

            // pivotRow berpindah, tuker baris
            if (pivotRow != i) {
                double[] temp = tempMatriks[i];
                tempMatriks[i] = tempMatriks[pivotRow];
                tempMatriks[pivotRow] = temp;
                steps.add("Tukar baris " + (i + 1) + " dengan baris " + (pivotRow + 1) + ":\n" + formatAugmented(tempMatriks, n));
            }

            // jika pivot 0, tdk ada invers
            if (Math.abs(tempMatriks[i][i]) < 1e-9) {
                steps.add("Pivot pada diagonal (" + (i + 1) + "," + (i + 1) + ") bernilai 0, matriks singular.");
                return new InverseSolverOutput(
                    InverseResult.noInverse("Matriks singular (determinan = 0), invers tidak ada"), 
                    steps
                );
            }

            // elemen pivot menjadi 1 utama
            double pivot = tempMatriks[i][i];
            if (Math.abs(pivot - 1.0) > 1e-9) {
                for (int j = i; j < 2 * n; j++) {
                    tempMatriks[i][j] /= pivot;
                }
                tempMatriks[i][i] = 1.0;
                steps.add("Baris " + (i + 1) + " = Baris " + (i + 1) + " / " + NumberFormatter.round(pivot) + ":\n" + formatAugmented(tempMatriks, n));
            }

            // nol-kan elemen pivot
            for (int j = i + 1; j < n; j++) {
                double factor = tempMatriks[j][i];
                if (Math.abs(factor) < 1e-9) continue;
                for (int k = i; k < 2 * n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                }
                tempMatriks[j][i] = 0.0;
                steps.add("Baris " + (j + 1) + " = Baris " + (j + 1) + " - (" + NumberFormatter.round(factor) + ") x Baris " + (i + 1) + ":\n" + formatAugmented(tempMatriks, n));
            }
        }

        // tahap 2 OBE
        for (int i = n - 1; i >= 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                double factor = tempMatriks[j][i];
                if (Math.abs(factor) < 1e-9) continue;
                for (int k = i; k < 2 * n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                }
                tempMatriks[j][i] = 0.0;
                steps.add("Baris " + (j + 1) + " = Baris " + (j + 1) + " - (" + NumberFormatter.round(factor) + ") x Baris " + (i + 1) + ":\n" + formatAugmented(tempMatriks, n));
            }
        }
        steps.add("Eliminasi Gauss-Jordan selesai. Matriks bagian kiri menjadi Identitas.");

        // copy kanan matrix augmentasi ke matrix baru (jadi matrix inverse)
        Matrix invers = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double val = NumberFormatter.round(tempMatriks[i][n + j]);
                invers.set(i, j, val);
            }
        }

        return new InverseSolverOutput(InverseResult.exists(invers), steps);
    }

    // helper buat formatting augmented [A|I]
    private static String formatAugmented(double[][] mat, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("[ ");
            for (int j = 0; j < 2 * n; j++) {
                if (j == n) sb.append("| ");
                sb.append(NumberFormatter.round(mat[i][j])).append(" ");
            }
            sb.append("]");
            if (i < n - 1) sb.append("\n");
        }
        return sb.toString();
    }
}