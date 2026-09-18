package algeo;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.determinant.OBE;

public class mainTest {
    public static void main(String[] args) {
        try {
            // Pembuatan data array biasa
            double[][] data = {
                {1, 2},
                {3, 4}
            };
            
            // Konstruktor Matrix ini sekarang dijamin aman karena terbungkus try-catch
            Matrix m = new Matrix(data);

            System.out.println("--- UJI COBA DETERMINAN OBE ---");
            
            // Perhitungan determinan
            double hasil = OBE.hitungOBE(m); 
            System.out.println("Hasil Determinan: " + hasil);

        } catch (MatrixException e) {
            // Menangkap error dari new Matrix() maupun dari OBE.hitungOBE()
            System.out.println("Gagal memproses matriks: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Menangkap error dari konstruktor Matrix jika ukuran 0
            System.out.println("Ukuran tidak valid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi error sistem: " + e.getMessage());
        }
    }
}