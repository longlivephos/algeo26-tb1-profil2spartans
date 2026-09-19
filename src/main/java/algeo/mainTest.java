package algeo;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;

public class mainTest {
    public static void main(String[] args) {
        try {//tes dgn matriks 3x3
            double[][] data = {
                {2, 1, 1},
                {1, 3, 2},
                {1, 0, 0}
            };
            
            Matrix m = new Matrix(data);

            System.out.println("--- UJI COBA DETERMINAN ---");
            System.out.println("Matriks Input:\n" + m.toString());
            
            // 1. Pengujian Metode OBE
            double hasilOBE = OBE.hitungOBE(m); 
            System.out.println("Hasil OBE      : " + NumberFormatter.round(hasilOBE));

            // 2. Pengujian Metode Ekspansi Kofaktor
            double hasilKofaktor = Kofaktor.hitungKofaktor(m);
            System.out.println("Hasil Kofaktor : " + NumberFormatter.round(hasilKofaktor));

            System.out.println("\n--- UJI COBA INVERS ---");

            // 3. Pengujian Invers Metode Adjoin
            Matrix inversAdjoin = Adjoin.hitungInversAdjoin(m);
            System.out.println("Hasil Invers Adjoin:\n" + inversAdjoin.toString());

            // 4. Pengujian Invers Metode Augmentasi 
            Matrix inversAugmentasi = Augmentasi.hitungInversAugmentasi(m);
            System.out.println("Hasil Invers Augmentasi:\n" + inversAugmentasi.toString());

        } catch (MatrixException e) { // Menangkap error dari new Matrix(), OBE.hitungOBE(), Kofaktor.hitungKofaktor()
            System.out.println("Gagal memproses matriks: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ukuran tidak valid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi error sistem: " + e.getMessage());
        }
    }
}