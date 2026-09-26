package algeo.modules;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.interpolasi.InterpolasiSolverOutput;
import algeo.core.interpolasi.NaturalCubicSpline;
import algeo.core.matrix.Matrix;
import algeo.core.util.NumberFormatter;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

public class ModuleSplineKubik implements Module {
    @Override
    public void jalankan(Scanner scanner) {
        String namaMetode = "Natural Cubic Spline Interpolation";
        
        System.out.println("\n=== " + namaMetode.toUpperCase() + " ===");
        System.out.println("1. Input titik dari keyboard");
        System.out.println("2. Input titik dari file (.txt)");
        System.out.print("Pilih metode (1/2): ");
        String pilihanInput = scanner.nextLine();

        Matrix inputData = null;

        try {
            if (pilihanInput.equals("1")) {
                System.out.println("\nMasukkan titik koordinat x dan y (pisahkan dengan spasi).");
                System.out.println("Tekan ENTER 2 kali jika sudah selesai memasukkan semua titik.");
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = scanner.nextLine();
                    if (line.trim().isEmpty()) {
                        if (sb.length() > 0) break;
                        else continue;
                    }
                    sb.append(line).append("\n");
                }
                inputData = MatrixReader.parseTeks(sb.toString());
            } else if (pilihanInput.equals("2")) {
                System.out.print("Masukkan path file (contoh: test/spline.txt): ");
                inputData = MatrixReader.bacaFile(scanner.nextLine());
            } else {
                System.out.println("Pilihan tidak valid.");
                return;
            }

            if (inputData.getCols() != 2) {
                System.out.println("Gagal: Data harus persis 2 kolom (X dan Y).");
                return;
            }

            int n = inputData.getRows();
            double[] xData = new double[n];
            double[] yData = new double[n];
            for (int i = 0; i < n; i++) {
                xData[i] = inputData.get(i, 0);
                yData[i] = inputData.get(i, 1);
            }

            String domainStr = "[" + NumberFormatter.round(xData[0]) + ", " + NumberFormatter.round(xData[n-1]) + "]";

            InterpolasiSolverOutput solverOut = NaturalCubicSpline.hitungSpline(xData, yData);
            InterpolasiResult result = solverOut.getResult();
            List<String> langkah = solverOut.getSteps();

            if (result.getType() == InterpolasiResult.ResultType.SUCCESS) {
                double[][] koef = result.getKoefisien();
                
                StringBuilder persamaanSegmen = new StringBuilder();
                for (int i = 0; i < koef.length; i++) {
                    persamaanSegmen.append(String.format(Locale.US, "Segmen %d, Domain [%.4f, %.4f]:\n", i+1, xData[i], xData[i+1]));
                    persamaanSegmen.append(String.format(Locale.US, "S_%d(x) = %.4f", i, koef[i][0]));
                    
                    if (Math.abs(koef[i][1]) > 1e-9) 
                        persamaanSegmen.append(String.format(Locale.US, " %s %.4f*(x - %.4f)", (koef[i][1] > 0 ? "+" : "-"), Math.abs(koef[i][1]), xData[i]));
                    if (Math.abs(koef[i][2]) > 1e-9) 
                        persamaanSegmen.append(String.format(Locale.US, " %s %.4f*(x - %.4f)^2", (koef[i][2] > 0 ? "+" : "-"), Math.abs(koef[i][2]), xData[i]));
                    if (Math.abs(koef[i][3]) > 1e-9) 
                        persamaanSegmen.append(String.format(Locale.US, " %s %.4f*(x - %.4f)^3", (koef[i][3] > 0 ? "+" : "-"), Math.abs(koef[i][3]), xData[i]));
                    
                    persamaanSegmen.append("\n\n");
                }

                System.out.println("\n>> Persamaan Spline Kubik berhasil dibentuk!");
                System.out.println(">> Domain Keseluruhan: " + domainStr);
                // Menampilkan persamaan per segmen
                StringBuilder hasilEvaluasi = new StringBuilder();
                while (true) {
                    System.out.print("\nIngin mengevaluasi nilai S(xt)? (y/n): ");
                    if (scanner.nextLine().equalsIgnoreCase("n")) break;

                    System.out.print("Masukkan nilai xt: ");
                    try {
                        double xTarget = Double.parseDouble(scanner.nextLine());
                        
                        if (!NaturalCubicSpline.diDalamDomain(xData, xTarget)) {
                            System.out.println("Peringatan: xt (" + xTarget + ") berada di LUAR domain " + domainStr);
                        }
                        
                        double taksiran = NaturalCubicSpline.evaluasi(xData, koef, xTarget);
                        String resText = "S(" + NumberFormatter.round(xTarget) + ") = " + NumberFormatter.round(taksiran);
                        System.out.println("--> Hasil: " + resText);
                        hasilEvaluasi.append("- ").append(resText).append("\n");
                    } catch (NumberFormatException e) {
                        System.out.println("Input tidak valid.");
                    }
                }
                // Menyusun teks hasil akhir agar disimpan di file.txt

                String teksHasil = OutputWriter.teksSplineKubik(namaMetode, inputData, langkah, result, domainStr, persamaanSegmen.toString(), hasilEvaluasi.toString());
                
                System.out.println("\n================ KESIMPULAN ================");
                System.out.println(teksHasil);
                System.out.println("============================================");

                System.out.print("\nSimpan hasil keseluruhan ini ke file .txt? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    System.out.print("Masukkan path file (contoh: hasil_spline.txt): ");
                    String pathSimpan = scanner.nextLine();
                    try {
                        OutputWriter.simpan(pathSimpan, teksHasil);
                        System.out.println("Hasil berhasil disimpan di: " + pathSimpan);
                    } catch (IOException e) {
                        System.out.println("Gagal menyimpan file: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("\nKomputasi Gagal: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            System.out.println("\nTerjadi kesalahan: " + e.getMessage());
        }
    }
}