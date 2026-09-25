package algeo.interpolasipolinomial;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;

public class interpolasi {
    /**
     * Menghitung koefisien polinomial dari sekumpulan titik (x, y).
     *
     * @param xData Array berisi nilai x dari titik koordinat.
     * @param yData Array berisi nilai y dari titik koordinat.
     * @return Objek InterpolasiResult berisi status keberhasilan dan koefisien.
     */
    public static InterpolasiResult hitungInterpolasi(double[] xData, double[] yData) {
        // 1. Validasi Input
        if (xData == null || yData == null) {
            return InterpolasiResult.failed("Data x atau y tidak boleh kosong (null).");
        }
        if (xData.length != yData.length) {
            return InterpolasiResult.failed("Jumlah titik x (" + xData.length + ") dan y (" + yData.length + ") tidak sama.");
        }
        
        int n = xData.length;
        if (n == 0) {
            return InterpolasiResult.failed("Data titik tidak boleh kosong.");
        }

        try {
            // 2. Membentuk Matriks Vandermonde (A)
            // Persamaan: a_0(x^0) + a_1(x^1) + ... + a_n(x^n) = y
            double[][] vandermondeData = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    vandermondeData[i][j] = Math.pow(xData[i], j);
                }
            }
            Matrix matriksA = new Matrix(vandermondeData);

            // 3. Memecahkan SPL dengan Mencari Invers Matriks Vandermonde (A^-1)
            // Menggunakan metode Augmentasi (OBE) buatan rekan tim agar lebih stabil
            InverseSolverOutput outputInvers = Augmentasi.hitungInversAugmentasi(matriksA);
            
            if (outputInvers.getResult().getType() == InverseResult.ResultType.NO_INVERSE) {
                return InterpolasiResult.failed("Gagal menghitung: Matriks titik-titik tidak memiliki invers (mungkin terdapat nilai x yang duplikat).");
            }

            Matrix invA = outputInvers.getResult().getInverse();

            // 4. Mengalikan Invers(A) dengan Matriks Y untuk mendapatkan Koefisien (C = A^-1 * Y)
            // Hasilnya disimpan dalam matriks kolom [n][1]
            double[][] koefisien = new double[n][1];
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    // Perkalian baris ke-i dari invA dengan nilai yData[j]
                    sum += invA.get(i, j) * yData[j]; 
                }
                koefisien[i][0] = sum;
            }

            // 5. Mengembalikan hasil sukses
            return InterpolasiResult.success(koefisien, xData, yData);

        } catch (MatrixException e) {
            return InterpolasiResult.failed("Terjadi kesalahan pada struktur matriks: " + e.getMessage());
        } catch (Exception e) {
            return InterpolasiResult.failed("Terjadi error sistem saat komputasi interpolasi: " + e.getMessage());
        }
    }
}
    
