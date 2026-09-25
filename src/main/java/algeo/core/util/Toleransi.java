package algeo.core.util;

import algeo.core.matrix.Matrix;

public class Toleransi {
    public static final double EPS_MESIN = 2.220446049250313e-16;
    private static final double FAKTOR = 10.0;// faktor pengaman supaya galat pembulatan kecil tidak dianggap bukan nol

    /**
     * Menghitung batas "dianggap nol" yang relatif terhadap entri terbesar matriks
     * Hanya kolom 0 sampai jumlahKolom-1 yang dilihat, jadi untuk matriks augmented [A|b] isi jumlahKolom = kolom A supaya b tidak ikut mempengaruhi
     * @param m matriks yang dicek
     * @param jumlahKolom banyak kolom (dari kiri) yang dipakai untuk mencari entri terbesar
     * @return toleransi, bernilai 0 kalau semua entri 0
     */
    public static double hitung(Matrix m, int jumlahKolom) {
        double terbesar = 0;
        for (int i = 0; i < m.getRows(); i++) {
            for (int j = 0; j < jumlahKolom; j++) {
                double v = Math.abs(m.get(i, j));
                if (v > terbesar) {
                    terbesar = v;
                }
            }
        }
        int n = Math.max(m.getRows(), jumlahKolom);
        return n * EPS_MESIN * terbesar * FAKTOR;
    }

    /**
     * Versi untuk matriks persegi / seluruh kolom dipakai
     * @param m matriks yang dicek
     * @return toleransi
     */
    public static double hitung(Matrix m) {
        return hitung(m, m.getCols());
    }

    /**
     * Versi untuk array 2 dimensi
     * @param data array matriks
     * @param jumlahKolom banyak kolom (dari kiri) yang dipakai
     * @return toleransi
     */
    public static double hitung(double[][] data, int jumlahKolom) {
        double terbesar = 0;
        for (double[] baris : data) {
            for (int j = 0; j < jumlahKolom; j++) {
                double v = Math.abs(baris[j]);
                if (v > terbesar) {
                    terbesar = v;
                }
            }
        }
        int n = Math.max(data.length, jumlahKolom);
        return n * EPS_MESIN * terbesar * FAKTOR;
    }

    /**
     * Mengecek apakah suatu nilai dianggap nol
     * Pakai <= (bukan <) supaya matriks nol semua tetap terdeteksi
     * @param nilai nilai yang dicek
     * @param tol toleransi dari method hitung
     * @return true jika |nilai| <= tol
     */
    public static boolean nol(double nilai, double tol) {
        return Math.abs(nilai) <= tol;
    }
}