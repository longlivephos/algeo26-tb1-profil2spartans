
package algeo.inverse;


import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;


public class Augmentasi {


    /**
     * Menghitung invers dari suatu matriks persegi menggunakan metode Adjoin (Transpose Kofaktor).
     *
     * @param matriks Matriks persegi yang akan dihitung inversnya.
     * @return Matrix matriks baru yang merupakan invers dari matriks input.
     * @throws MatrixException jika matriks bukan persegi atau merupakan matriks singular (determinan = 0).
     */
   
    public static Matrix hitungInversAugmentasi(Matrix matriks) throws MatrixException {
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
            }


            // jika pivot 0, tdk ada invers
            if (tempMatriks[i][i] == 0) {
                throw new MatrixException("Matriks singular (determinan = 0), sehingga tidak memiliki invers");
            }


            // elemen pivot menjadi 1 utama
            double pivot = tempMatriks[i][i];
            for (int j = i; j < 2 * n; j++) {
                tempMatriks[i][j] /= pivot;
            }


            // nol-kan elemen pivot
            for (int j = i + 1; j < n; j++) {
                double factor = tempMatriks[j][i];
                for (int k = i; k < 2 * n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                }
            }
        }


        // tahap 2 OBE
        for (int i = n - 1; i >= 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                double factor = tempMatriks[j][i];
                for (int k = i; k < 2 * n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                }
            }
        }


        // copy kanan matrix augmentasi ke matrix baru (jadi matrix inverse)
        Matrix invers = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double val = tempMatriks[i][n + j];
                invers.set(i, j, NumberFormatter.round(val));
            }
        }


        return invers;
    }
}
