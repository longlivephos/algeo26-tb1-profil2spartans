package algeo.determinant;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;

public class Kofaktor {
    /**
     * Menghitung nilai determinan dari sebuah matriks menggunakan Ekspansi Kofaktor.
     * Nilai yang dikembalikan adalah nilai mentah (tanpa pembulatan) untuk menjaga akurasi dan presisi pada perhitungan lanjutan seperti Invers atau Aturan Cramer.
     * @param matriks Objek matriks persegi yang akan dihitung determinannya.
     * @return Nilai determinan matriks murni dalam tipe double.
     * @throws MatrixException Jika ukuran matriks input tidak berbentuk persegi.
     */
    public static double hitungKofaktor(Matrix matriks) throws MatrixException {
        // Validasi matriks persegi
        if (!matriks.isSquare()) {
            throw new MatrixException("Matriks harus berbentuk persegi untuk menghitung determinan");
        }
        
        //fungsi rekursif untuk hitung determinan
        double det = hitungRekursif(matriks);
        return det;
    }

    // Fungsi internal memecah matriks secara rekursif
    // @param m Matriks atau sub-matriks yang sedang diproses.
    // @return Nilai determinan dari matriks input.
    private static double hitungRekursif(Matrix m) {
        int n = m.getRows();
        
        // Basis 1: untuk matriks sisa 1x1
        if (n == 1) {
            return m.get(0, 0);
        }
        
        // Basis 2: untuk matriks sisa 2x2 
        if (n == 2) {
            return (m.get(0, 0) * m.get(1, 1)) - (m.get(0, 1) * m.get(1, 0));
        }

        double det = 0;
        int tanda = 1;

        // Ekspansi kofaktor baris pertama 
        for (int j = 0; j < n; j++) {
            // Ambil sub-matriks dengan membuang baris 0 dan kolom j
            Matrix subMatriks = m.getMinor(0, j);
            
            // Rumus kofaktor:
            det += tanda * m.get(0, j) * hitungRekursif(subMatriks);
            
            // Ubah tanda untuk elemen berikutnya (+, -, +, -)
            tanda *= -1; 
        }
        
        return det;
    }
  
}
