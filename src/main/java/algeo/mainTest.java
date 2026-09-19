package algeo;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;

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

        } catch (MatrixException e) { // Menangkap error dari new Matrix(), OBE.hitungOBE(), Kofaktor.hitungKofaktor()
            System.out.println("Gagal memproses matriks: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ukuran tidak valid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi error sistem: " + e.getMessage());
        }
    }
}