package algeo.determinant;

import java.util.List;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;

public class Kofaktor {
    
    // Batas aman ukuran matriks untuk Kofaktor agar tidak hang / Out of Memory
    private static final int BATAS_N = 11;
    // Batas toleransi presisi desimal untuk mendeteksi angka 0
    private static final double EPS = 1e-12;

    /**
     * Fungsi utama perhitungan kofaktor tanpa mencatat langkah (silent mode).
     * Digunakan untuk perhitungan lanjutan seperti Invers atau Cramer.
     */
    public static double hitungKofaktor(Matrix matriks) throws MatrixException {
        return hitungKofaktor(matriks, null);
    }

    /**
     * Fungsi perhitungan kofaktor dengan pencatatan langkah (top-level only).
     */
    public static double hitungKofaktor(Matrix matriks, List<String> steps) throws MatrixException {
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung determinan");
        }

        int n = matriks.getRows();
        
        // Proteksi O(N!) - Tolak eksekusi jika melebihi batas
        if (n > BATAS_N) {
            throw new MatrixException("Ukuran matriks (" + n + "x" + n + ") terlalu besar untuk metode Kofaktor (Batas maksimal 11x11). Silakan gunakan metode OBE.");
        }

        boolean catat = (steps != null);
        return hitungRekursif(matriks, catat, steps);
    }

    /**
     * Fungsi internal rekursif dengan pencarian baris/kolom nol terbanyak.
     */
    private static double hitungRekursif(Matrix m, boolean isTopLevel, List<String> steps) throws MatrixException {
        int n = m.getRows();

        if (n == 1) return m.get(0, 0);
        if (n == 2) return (m.get(0, 0) * m.get(1, 1)) - (m.get(0, 1) * m.get(1, 0));

        // 1. Cari baris atau kolom dengan elemen 0 terbanyak untuk efisiensi
        int maxZeros = -1;
        int bestIdx = 0;
        boolean isRow = true;

        for (int i = 0; i < n; i++) {
            int zeroCountRow = 0;
            int zeroCountCol = 0;
            for (int j = 0; j < n; j++) {
                if (Math.abs(m.get(i, j)) < EPS) zeroCountRow++;
                if (Math.abs(m.get(j, i)) < EPS) zeroCountCol++;
            }
            
            if (zeroCountRow > maxZeros) {
                maxZeros = zeroCountRow;
                bestIdx = i;
                isRow = true;
            }
            if (zeroCountCol > maxZeros) {
                maxZeros = zeroCountCol;
                bestIdx = i;
                isRow = false;
            }
        }

        Matrix targetMat = m;
        
        // 2. Jika kolom yang memiliki nol terbanyak, lakukan Transpose: det(A) = det(A^T)
        if (!isRow) {
            double[][] tData = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    tData[i][j] = m.get(j, i); // Membalik baris dan kolom
                }
            }
            
            targetMat = new Matrix(tData);
            
            if (isTopLevel && steps != null) {
                steps.add("Kolom " + (bestIdx + 1) + " dipilih karena memiliki nol terbanyak (" + maxZeros + " nol).");
                steps.add("Karena det(A) = det(A^T), matriks di-transpose. Ekspansi berlanjut di baris " + (bestIdx + 1) + " matriks baru:\n" + targetMat.toString());
            }
        } else {
            if (isTopLevel && steps != null) {
                steps.add("Baris " + (bestIdx + 1) + " dipilih sebagai acuan karena memiliki nol terbanyak (" + maxZeros + " nol).");
            }
        }

        double det = 0;
        StringBuilder rumus = new StringBuilder("det =");

        // 3. Lakukan ekspansi sepanjang baris terpilih (bestIdx)
        for (int j = 0; j < n; j++) {
            double elemen = targetMat.get(bestIdx, j);
            
            // Lewati perhitungan rekursif jika elemen = 0 (optimasi efisiensi terbesar)
            if (Math.abs(elemen) < EPS) {
                if (isTopLevel && steps != null) {
                    rumus.append(" + 0");
                    steps.add("Elemen a[" + (bestIdx + 1) + "][" + (j + 1) + "] = 0, kofaktor dilewati.");
                }
                continue; 
            }

            Matrix minor = targetMat.getMinor(bestIdx, j);
            int tanda = ((bestIdx + j) % 2 == 0) ? 1 : -1;
            
            // Panggilan rekursif murni tanpa mencetak langkah bawaan (isTopLevel = false)
            double minorDet = hitungRekursif(minor, false, null);
            double elemenKofaktor = tanda * minorDet;
            
            det += elemen * elemenKofaktor;

            if (isTopLevel && steps != null) {
                steps.add("a[" + (bestIdx + 1) + "][" + (j + 1) + "] = " + NumberFormatter.round(elemen) 
                        + "  |  det(Minor) = " + NumberFormatter.round(minorDet) 
                        + "  |  C[" + (bestIdx + 1) + "][" + (j + 1) + "] = " + NumberFormatter.round(elemenKofaktor));
                
                rumus.append(" + (").append(NumberFormatter.round(elemen))
                     .append(" * ").append(NumberFormatter.round(elemenKofaktor)).append(")");
            }
        }

        if (isTopLevel && steps != null) {
            String hasilRumus = rumus.toString().replace("det = +", "det =");
            steps.add("Rumus Akhir: " + hasilRumus);
        }

        return det;
    }
}