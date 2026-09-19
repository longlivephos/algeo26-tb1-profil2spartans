package algeo;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;

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
            InverseSolverOutput outputAdjoin = Adjoin.hitungInversAdjoin(m);
            System.out.println("Langkah-Langkah Adjoin:");
            for (String step : outputAdjoin.getSteps()) {
                System.out.println("- " + step);
            }
            if (outputAdjoin.getResult().getType() == InverseResult.ResultType.HAS_INVERSE) {
                System.out.println("Hasil Invers Adjoin:\n" + outputAdjoin.getResult().getInverse().toString());
            } else {
                System.out.println(outputAdjoin.getResult().getErrorMessage());
            }

            // 4. Pengujian Invers Metode Augmentasi 
            InverseSolverOutput outputAugmentasi = Augmentasi.hitungInversAugmentasi(m);
            System.out.println("\nLangkah-Langkah Augmentasi:");
            for (String step : outputAugmentasi.getSteps()) {
                System.out.println("- " + step);
            }
            if (outputAugmentasi.getResult().getType() == InverseResult.ResultType.HAS_INVERSE) {
                System.out.println("Hasil Invers Augmentasi:\n" + outputAugmentasi.getResult().getInverse().toString());
            } else {
                System.out.println(outputAugmentasi.getResult().getErrorMessage());
            }

        } catch (MatrixException e) { // Menangkap error dari new Matrix(), OBE.hitungOBE(), Kofaktor.hitungKofaktor()
            System.out.println("Gagal memproses matriks: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ukuran tidak valid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi error sistem: " + e.getMessage());
        }
    }
}