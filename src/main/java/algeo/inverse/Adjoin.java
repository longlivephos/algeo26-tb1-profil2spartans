package algeo.inverse;

import java.util.ArrayList;
import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.OBE;

public class Adjoin {

    /**
     * Menghitung invers dari suatu matriks persegi menggunakan metode Adjoin (Transpose Kofaktor).
     *
     * @param matriks Matriks persegi yang merupakan input.
     * @return InverseSolverOutput objek hasil perhitungan beserta langkah-langkahnya.
     * @throws MatrixException Jika matriks tidak berbentuk persegi.
     */
    public static InverseSolverOutput hitungInversAdjoin(Matrix matriks) throws MatrixException {
        List<String> steps = new ArrayList<>();

        // validate
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung invers");
        }

        try {
            steps.add("Matriks input A:\n" + matriks);

            // determinan. jika det = 0, tidak ada invers
            double det = OBE.hitungOBE(matriks);
            steps.add("Hitung determinan matriks A: det(A) = " + NumberFormatter.round(det));

            if (Math.abs(det) < 1e-9) {
                steps.add("Determinan matriks bernilai 0, invers tidak ada.");
                return new InverseSolverOutput(
                    InverseResult.noInverse("Matriks singular (determinan = 0), invers tidak ada"), 
                    steps
                );
            }

            int n = matriks.getRows();
            Matrix invers = new Matrix(n, n);

            // basis matrix 1x1
            if (n == 1) {
                invers.set(0, 0, 1.0 / matriks.get(0, 0));
                steps.add("Matriks berukuran 1x1, invers bernilai 1.");
                return new InverseSolverOutput(InverseResult.exists(invers), steps);
            }

            steps.add("Menghitung matriks kofaktor dan adjoin (transpose kofaktor)...");

            Matrix kofaktorMat = new Matrix(n, n);
            Matrix adjoinMat = new Matrix(n, n);

            // matrix adjoin
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // submatrix (minor)
                    Matrix subMatriks = matriks.getMinor(i, j);
                    
                    // determinan dari minor
                    double detMinor = OBE.hitungOBE(subMatriks);
                    
                    // sign
                    int tanda = ((i + j) % 2 == 0) ? 1 : -1;
                    
                    // nilai kofaktor
                    double kofaktorVal = tanda * detMinor;
                    kofaktorMat.set(i, j, kofaktorVal);
                    
                    // transpose untuk dapat adjoin
                    adjoinMat.set(j, i, kofaktorVal);

                    steps.add("C[" + (i + 1) + "][" + (j + 1) + "] = (" + (tanda > 0 ? "+" : "-") 
                        + ") * det(Minor) = " + kofaktorVal);
                }
            }

            steps.add("Matriks Kofaktor C:\n" + kofaktorMat);
            steps.add("Matriks Adjoin Adj(A) = Transpose(C):\n" + adjoinMat);

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double nilaiInvers = adjoinMat.get(i, j) / det;
                    invers.set(i, j, nilaiInvers);
                }
            }

            steps.add("Bagi matriks Adjoin dengan determinan (" + NumberFormatter.round(det) + "):\n" + invers);

            return new InverseSolverOutput(InverseResult.exists(invers), steps);

        } catch (MatrixException e) {
            steps.add("Terjadi kesalahan perhitungan matriks: " + e.getMessage());
            return new InverseSolverOutput(
                InverseResult.noInverse(e.getMessage()), 
                steps
            );
        }
    }
}