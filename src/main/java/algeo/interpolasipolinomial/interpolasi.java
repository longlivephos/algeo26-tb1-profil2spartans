package algeo.interpolasipolinomial;

import java.util.List;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;

public class Interpolasi { 

    // Menghitung koefisien polinomial dengan pencatatan langkah penyelesaian.
    public static InterpolasiResult hitungInterpolasi(double[] xData, double[] yData, List<String> steps) {
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
        // Batas maksimal 10 titik
        if (n > 10) {
            return InterpolasiResult.failed("Batas maksimal titik adalah 10 untuk menjaga stabilitas Matriks Vandermonde.");
        }

        try {
            if (steps != null) {
                steps.add("Membentuk Matriks Vandermonde (A) dari " + n + " titik koordinat...");
            }
            
            double[][] vandermondeData = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    vandermondeData[i][j] = Math.pow(xData[i], j);
                }
            }
            Matrix matriksA = new Matrix(vandermondeData);
            
            if (steps != null) {
                steps.add("Matriks Vandermonde (A) yang terbentuk:\n" + matriksA.toString());
                steps.add("Mencari Invers Matriks A (A^-1) menggunakan metode Augmentasi (OBE)...");
            }

            InverseSolverOutput outputInvers = Augmentasi.hitungInversAugmentasi(matriksA);
            
            if (outputInvers.getResult().getType() == InverseResult.ResultType.NO_INVERSE) {
                return InterpolasiResult.failed("Gagal menghitung: Matriks titik-titik tidak memiliki invers.");
            }

            Matrix invA = outputInvers.getResult().getInverse();
            
            if (steps != null) {
                steps.add("Matriks Invers (A^-1) berhasil didapatkan:\n" + invA.toString());
                steps.add("Mengalikan A^-1 dengan Matriks Y untuk mendapatkan Koefisien (C = A^-1 * Y)...");
            }

            double[][] koefisien = new double[n][1];
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    sum += invA.get(i, j) * yData[j]; 
                }
                koefisien[i][0] = sum;
            }

            if (steps != null) {
                steps.add("Perkalian selesai. Deret koefisien polinomial berhasil ditemukan.");
            }

            return InterpolasiResult.success(koefisien, xData, yData);

        } catch (MatrixException e) {
            return InterpolasiResult.failed("Terjadi kesalahan pada struktur matriks: " + e.getMessage());
        } catch (Exception e) {
            return InterpolasiResult.failed("Terjadi error sistem: " + e.getMessage());
        }
    }

    // Mengevaluasi fungsi P(x) untuk suatu nilai x yang baru.
    public static double taksirNilaiPolinomial(double[][] koefisien, double xTarget) {
        double hasil = 0;
        for (int i = 0; i < koefisien.length; i++) {
            hasil += koefisien[i][0] * Math.pow(xTarget, i);
        }
        return hasil;
    }
}