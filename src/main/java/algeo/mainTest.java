package algeo;

import java.util.ArrayList;
import java.util.List;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.interpolasipolinomial.interpolasi;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;

public class mainTest {
    public static void main(String[] args) {
        try {
            // tes dgn matriks 3x3
            double[][] data = {
                {2, 1, 1},
                {1, 3, 2},
                {1, 0, 0}
            };
            
            Matrix m = new Matrix(data);

            System.out.println("--- UJI COBA DETERMINAN ---");
            System.out.println("Matriks Input:\n" + m.toString());
            
            // 1. Pengujian Metode OBE
            List<String> langkahOBE = new ArrayList<>();
            double hasilOBE = OBE.hitungOBE(m, langkahOBE); 
            
            System.out.println("Langkah-Langkah OBE:");
            for (String langkah : langkahOBE) {
                System.out.println("- " + langkah);
            }
            System.out.println("Hasil OBE      : " + NumberFormatter.round(hasilOBE) + "\n");

            // 2. Pengujian Metode Ekspansi Kofaktor
            List<String> langkahKofaktor = new ArrayList<>();
            double hasilKofaktor = Kofaktor.hitungKofaktor(m, langkahKofaktor);
            
            System.out.println("Langkah-Langkah Kofaktor:");
            for (String langkah : langkahKofaktor) {
                System.out.println("- " + langkah);
            }
            System.out.println("Hasil Kofaktor : " + NumberFormatter.round(hasilKofaktor) + "\n");

            System.out.println("--- UJI COBA INVERS ---");

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

            // 5. Pengujian Interpolasi Polinomial
            System.out.println("--- UJI COBA INTERPOLASI POLINOMIAL ---");
            
            double[] xData = {0, 1, 2};
            double[] yData = {1, 3, 7};
            
            System.out.println("Titik Input:");
            for(int i = 0; i < xData.length; i++) {
                System.out.println("- (" + xData[i] + ", " + yData[i] + ")");
            }

            InterpolasiResult outputInterpolasi = interpolasi.hitungInterpolasi(xData, yData);

            if (outputInterpolasi.getType() == InterpolasiResult.ResultType.SUCCESS) {
                double[][] koef = outputInterpolasi.getKoefisien();
                
                System.out.println("\nKoefisien yang didapat:");
                for (int i = 0; i < koef.length; i++) {
                    System.out.println("a_" + i + " = " + NumberFormatter.round(koef[i][0]));
                }
                
                System.out.print("\nPersamaan Polinomial: P(x) = ");
                for (int i = 0; i < koef.length; i++) {
                    System.out.print(NumberFormatter.round(koef[i][0]));
                    if (i > 0) System.out.print("x^" + i);
                    if (i < koef.length - 1) System.out.print(" + ");
                }
                System.out.println("\n");
                
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