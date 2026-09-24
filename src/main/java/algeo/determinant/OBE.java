package algeo.determinant;

import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;

public class OBE {
    private static final double EPS = 1e-12;

    /**
     * Menghitung determinan dengan reduksi baris (tanpa mencatat langkah).
     *
     * @param matriks matriks persegi
     * @return nilai determinan (belum dibulatkan)
     * @throws MatrixException jika matriks tidak persegi
     */
    public static double hitungOBE(Matrix matriks) throws MatrixException {
        return hitungOBE(matriks, null);
    }

    /**
     * Menghitung determinan dengan reduksi baris dan mencatat langkahnya.
     *
     * @param matriks matriks persegi
     * @param steps tempat menyimpan langkah, isi null kalau tidak perlu
     * @return nilai determinan (belum dibulatkan)
     * @throws MatrixException jika matriks tidak persegi
     */
    public static double hitungOBE(Matrix matriks, List<String> steps) throws MatrixException {
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung determinan");
        }

        int n = matriks.getRows();
        boolean catat = steps != null && n <= 11;
        double[][] tempMatriks = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tempMatriks[i][j] = matriks.get(i, j);
            }
        }
        if (catat) {
            steps.add("Matriks awal:\n" + new Matrix(tempMatriks));
        }

        double det = 1.0;

        for (int i = 0; i < n; i++) {
            int pivotRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(tempMatriks[j][i]) > Math.abs(tempMatriks[pivotRow][i])) {
                    pivotRow = j;
                }
            }

            if (pivotRow != i) {
                double[] temp = tempMatriks[i];
                tempMatriks[i] = tempMatriks[pivotRow];
                tempMatriks[pivotRow] = temp;
                det *= -1.0;
                if (catat) {
                    steps.add("Tukar baris " + (i + 1) + " dengan baris " + (pivotRow + 1)
                        + " (determinan dikali -1):\n" + new Matrix(tempMatriks));
                }
            }

            if (Math.abs(tempMatriks[i][i]) < EPS) {
                if (catat) {
                    steps.add("Pivot kolom " + (i + 1) + " bernilai 0, determinan = 0.");
                }
                return 0.0;
            }

            for (int j = i + 1; j < n; j++) {
                double factor = tempMatriks[j][i] / tempMatriks[i][i];
                if (factor == 0) continue;
                for (int k = i; k < n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                }
                tempMatriks[j][i] = 0.0;
                if (catat) {
                    steps.add("Baris " + (j + 1) + " = Baris " + (j + 1) + " - (" + NumberFormatter.round(factor)
                        + ") x Baris " + (i + 1) + ":\n" + new Matrix(tempMatriks));
                }
            }
        }

        for (int i = 0; i < n; i++) {
            det *= tempMatriks[i][i];
        }
        if (catat) {
            steps.add("Determinan = hasil kali diagonal (dengan tanda dari tukar baris) = " + NumberFormatter.round(det));
        }

        return det;
    }
}