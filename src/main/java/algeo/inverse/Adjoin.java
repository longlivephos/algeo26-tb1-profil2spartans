package algeo.inverse;


import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.determinant.OBE;


public class Adjoin {

        /**
     * Menghitung invers dari suatu matriks persegi menggunakan metode Adjoin (Transpose Kofaktor).
     *
     * @param matriks Matriks persegi yang akan dihitung inversnya.
     * @return Matrix matriks baru yang merupakan invers dari matriks input.
     * @throws MatrixException jika matriks bukan persegi atau merupakan matriks singular (determinan = 0).
     */

   
    public static Matrix hitungInversAdjoin(Matrix matriks) throws MatrixException {
        // validate
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung invers");
        }


        // determinan. jika det = 0, tidak ada invers
        double det = OBE.hitungOBE(matriks);
        if (Math.abs(det) < 1e-9) {
            throw new MatrixException("Matriks singular (determinan = 0), sehingga tidak memiliki invers");
        }


        int n = matriks.getRows();
        Matrix invers = new Matrix(n, n);


        // basis matrix 1x1
        if (n == 1) {
            invers.set(0, 0, 1.0 / matriks.get(0, 0));
            return invers;
        }


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
               
                // transpose untuk dapat adjoin
                double nilaiInvers = kofaktorVal / det;
                invers.set(j, i, nilaiInvers);
            }
        }


        return invers;
    }
}
