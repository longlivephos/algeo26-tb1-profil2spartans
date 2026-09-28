package algeo.modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.regression.RegressionOutput;
import algeo.core.regression.SplineRegression;
import algeo.core.util.DecimalParser;
import algeo.core.util.NumberFormatter;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

/**
 * Modul untuk menangani antarmuka CLI Regresi Spline.
 * Modul ini menerima input sampel titik data dari keyboard atau file,
 * meminta masukan jumlah dan posisi knot dari pengguna, mengeksekusi komputasi 
 * regresi dengan basis daya terpotong (truncated power basis), memprediksi nilai y^(xt),
 * serta mengekspor hasilnya ke file .txt.
 */
public class ModuleRegresiSplineKubik implements Module {

    /**
     * Menjalankan menu interaktif untuk fitur Regresi Spline.
     * 
     * @param sc Objek Scanner bersama dari program utama.
     */
    @Override
    public void jalankan(Scanner sc) {
        System.out.println("\n=== SUB-MENU REGRESI SPLINE ===");
        System.out.println("Pilih Derajat Spline:");
        System.out.println("1. Regresi Spline Linier (Derajat 1)");
        System.out.println("2. Regresi Spline Kuadratik (Derajat 2)");
        System.out.println("3. Regresi Spline Kubik (Derajat 3)");
        System.out.print("Pilih derajat (1-3) [default 3]: ");
        
        String inputDerajat = sc.nextLine().trim();
        int derajat = 3;
        if (inputDerajat.equals("1")) derajat = 1;
        else if (inputDerajat.equals("2")) derajat = 2;
        else if (inputDerajat.equals("3") || inputDerajat.isEmpty()) derajat = 3;
        else {
            System.out.println("Pilihan derajat tidak valid.");
            return;
        }

        String namaMetode = "Regresi Spline (Derajat " + derajat + ")";
        
        System.out.println("\n--- SUMBER INPUT DATA ---");
        System.out.println("1. Input titik sampel dari keyboard");
        System.out.println("2. Input titik sampel dari file (.txt)");
        System.out.print("Pilih metode (1/2): ");
        String pilihanInput = sc.nextLine().trim();

        Matrix inputData = null;

        try {
            // baca sampel titik data
            if (pilihanInput.equals("1")) {
                System.out.println("\nMasukkan sampel titik koordinat x dan y (pisahkan dengan spasi).");
                System.out.println("Format: x y (contoh: 2.5 4)");
                System.out.println("Tekan ENTER 2 kali jika sudah selesai memasukkan semua titik.");
                
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = sc.nextLine();
                    if (line.trim().isEmpty()) {
                        if (sb.length() > 0) break;
                        else continue;
                    }
                    sb.append(line).append("\n");
                }
                inputData = MatrixReader.parseTeks(sb.toString());

            } else if (pilihanInput.equals("2")) {
                System.out.print("Masukkan path file (contoh: test/regresi_spline.txt): ");
                inputData = MatrixReader.bacaFile(sc.nextLine().trim());
            } else {
                System.out.println("Pilihan tidak valid.");
                return;
            }

            if (inputData.getCols() != 2) {
                System.out.println("Gagal: Data sampel harus persis 2 kolom (Kolom 1 untuk X, Kolom 2 untuk Y).");
                return;
            }

            // baca input knot
            System.out.print("\nMasukkan jumlah knot (K): ");
            int k;
            try {
                k = Integer.parseInt(sc.nextLine().trim());
                if (k < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Jumlah knot harus berupa angka bulat non-negatif.");
                return;
            }

            double[] knots = new double[k];
            if (k > 0) {
                System.out.println("Masukkan " + k + " posisi knot (pisahkan dengan spasi, harus terurut naik):");
                while (true) {
                    System.out.print("Posisi knot: ");
                    String[] parts = sc.nextLine().trim().split("\\s+");
                    if (parts.length == k) {
                        try {
                            for (int j = 0; j < k; j++) {
                                knots[j] = DecimalParser.parse(parts[j]);
                            }
                            break;
                        } catch (NumberFormatException ignored) {}
                    }
                    System.out.println("Format salah. Harap masukkan persis " + k + " angka yang dipisahkan spasi.");
                }
            }

            // eksekusi regresi spline
            SplineRegression solver = new SplineRegression();
            RegressionOutput regOutput = solver.solve(inputData, knots, derajat); 

            String posisiKnotStr = (k == 0) ? "Tanpa Knot" : Arrays.toString(knots);
            String persamaanStr = regOutput.getPersamaan();

            System.out.println("\n>> Regresi Spline berhasil dihitung!");
            System.out.println(">> Posisi Knot: " + posisiKnotStr);
            System.out.println(">> Persamaan: " + persamaanStr);

            // perhitungan prediksi nilai
            StringBuilder hasilPrediksi = new StringBuilder();
            while (true) {
                System.out.print("\nIngin memprediksi nilai y^(xt)? (y/n): ");
                if (sc.nextLine().trim().equalsIgnoreCase("n")) break;

                System.out.print("Masukkan nilai xt: ");
                try {
                    double xTarget = DecimalParser.parse(sc.nextLine().trim());
                    double yPrediksi = regOutput.prediksi(xTarget);

                    String resText = "y^(" + NumberFormatter.teks(xTarget) + ") = " + NumberFormatter.teks(yPrediksi);
                    System.out.println("--> Hasil Prediksi: " + resText);
                    
                    hasilPrediksi.append("- ").append(resText).append("\n");
                } catch (NumberFormatException e) {
                    System.out.println("Input tidak valid. Harap masukkan angka.");
                }
            }

            // menyusun dan menyimpan teks ke dalam file. txt
            String teksHasil = OutputWriter.teksRegresiSpline(namaMetode, inputData, regOutput.getSteps(), posisiKnotStr, persamaanStr, hasilPrediksi.toString());

            System.out.println("\n================ KESIMPULAN ================");
            System.out.println(teksHasil);
            System.out.println("============================================");

            System.out.print("\nSimpan hasil keseluruhan ini ke file .txt? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.print("Masukkan path file tujuan (contoh: hasil_regresi.txt): ");
                String pathSimpan = sc.nextLine().trim();
                try {
                    OutputWriter.simpan(pathSimpan, teksHasil);
                    System.out.println("Hasil berhasil disimpan di: " + pathSimpan);
                } catch (IOException e) {
                    System.out.println("Gagal menyimpan file: " + e.getMessage());
                }
            }

        } catch (MatrixException e) {
            System.out.println("\nGagal menghitung regresi: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nTerjadi kesalahan sistem: " + e.getMessage());
        }
    }
}