package algeo;

import java.util.ArrayList;
import java.util.List;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.interpolasipolinomial.Interpolasi;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput; 

public class mainTest {
    public static void main(String[] args) {
        try {
            double[][] data = {
                {2, 1, 1},
                {1, 3, 2},
                {1, 0, 0}
            };
            
            Matrix m = new Matrix(data);

            System.out.println("--- UJI COBA DETERMINAN ---");
            System.out.println("Matriks Input:\n" + m.toString());
            
            List<String> langkahOBE = new ArrayList<>();
            double hasilOBE = OBE.hitungOBE(m, langkahOBE); 
            System.out.println("Hasil OBE      : " + NumberFormatter.round(hasilOBE));

            List<String> langkahKofaktor = new ArrayList<>();
            double hasilKofaktor = Kofaktor.hitungKofaktor(m, langkahKofaktor);
            System.out.println("Hasil Kofaktor : " + NumberFormatter.round(hasilKofaktor) + "\n");

            System.out.println("--- UJI COBA INVERS ---");

            InverseSolverOutput outputAdjoin = Adjoin.hitungInversAdjoin(m);
            if (outputAdjoin.getResult().getType() == InverseResult.ResultType.HAS_INVERSE) {
                System.out.println("Hasil Invers Adjoin:\n" + outputAdjoin.getResult().getInverse().toString());
            }

            InverseSolverOutput outputAugmentasi = Augmentasi.hitungInversAugmentasi(m);
            if (outputAugmentasi.getResult().getType() == InverseResult.ResultType.HAS_INVERSE) {
                System.out.println("Hasil Invers Augmentasi:\n" + outputAugmentasi.getResult().getInverse().toString());
            }

            System.out.println("--- UJI COBA INTERPOLASI POLINOMIAL ---");
            
            double[] xData = {0, 1, 2};
            double[] yData = {1, 3, 7};
            
            System.out.println("Titik Input:");
            for(int i = 0; i < xData.length; i++) {
                System.out.println("- (" + xData[i] + ", " + yData[i] + ")");
            }

            // Memasukkan list langkah sebagai parameter ketiga
            List<String> langkahInterpolasi = new ArrayList<>();
            InterpolasiResult outputInterpolasi = Interpolasi.hitungInterpolasi(xData, yData, langkahInterpolasi);

            if (outputInterpolasi.getType() == InterpolasiResult.ResultType.SUCCESS) {
                System.out.println("\nLangkah-langkah Interpolasi:");
                for (String langkah : langkahInterpolasi) {
                    System.out.println("- " + langkah);
                }

                double[][] koef = outputInterpolasi.getKoefisien();
                
                System.out.print("\nPersamaan Polinomial: P(x) = ");
                for (int i = 0; i < koef.length; i++) {
                    System.out.print(NumberFormatter.round(koef[i][0]));
                    if (i > 0) System.out.print("x^" + i);
                    if (i < koef.length - 1) System.out.print(" + ");
                }
                System.out.println();
                
                // Menguji evaluasi P(x)
                double xUji = 1.5;
                double taksiran = Interpolasi.taksirNilaiPolinomial(koef, xUji);
                System.out.println("Taksiran nilai y saat x = " + xUji + " adalah: " + NumberFormatter.round(taksiran) + "\n");
                
            } else {
                System.out.println("\nGagal: " + outputInterpolasi.getErrorMessage());
            }

        } catch (MatrixException e) { 
            System.out.println("Gagal memproses matriks: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Ukuran tidak valid: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi error sistem: " + e.getMessage());
        }
    }
}