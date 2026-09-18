package algeo.determinant;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;

public class OBE { 

    public static double hitungOBE(Matrix matriks) throws MatrixException {
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung determinan");
        }

        int n = matriks.getRows();
        double[][] tempMatriks = new double[n][n];

        // Menyalin elemen menggunakan metode get() karena variabel matriks di-protect oleh Matrix.java
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tempMatriks[i][j] = matriks.get(i, j);
            }
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
            }

            if (tempMatriks[i][i] == 0) {
                return 0.0;
            }

            for (int j = i + 1; j < n; j++) {
                double factor = tempMatriks[j][i] / tempMatriks[i][i]; 
                for (int k = i; k < n; k++) {
                    tempMatriks[j][k] -= factor * tempMatriks[i][k];
                } 
            }
        }

        for (int i = 0; i < n; i++) {
            det *= tempMatriks[i][i];
        }

        return NumberFormatter.round(det);
    }   
}