package algeo.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.util.NumberFormatter;
import algeo.interpolasipolinomial.Interpolasi;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

public class ModuleInterpolasi implements Module {

    @Override
    public void jalankan() {
        // Deklarasi Scanner secara lokal 
        Scanner scanner = new Scanner(System.in);
        String namaMetode = "Interpolasi Polinomial";
        
        System.out.println("\n=== " + namaMetode.toUpperCase() + " ===");
        System.out.println("1. Input titik dari keyboard");
        System.out.println("2. Input titik dari file (.txt)");
        System.out.print("Pilih metode (1/2): ");
        String pilihanInput = scanner.nextLine();

        Matrix inputData = null;

        try {
            // Membaca input  dari MatrixReader 
            if (pilihanInput.equals("1")) {
                System.out.println("\nMasukkan titik koordinat x dan y (pisahkan dengan spasi).");
                System.out.println("Format: x y (contoh: 2.5 4)");
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
                System.out.print("Masukkan path file (contoh: test/titik.txt): ");
                String path = scanner.nextLine();
                inputData = MatrixReader.bacaFile(path);
            } else {
                System.out.println("Pilihan tidak valid.");
                return;
            }

            if (inputData.getCols() != 2) {
                System.out.println("Gagal: Data interpolasi harus persis 2 kolom (Kolom 1 untuk X, Kolom 2 untuk Y).");
                return;
            }

            // Memecah matriks menjadi array x dan y sekaligus mencari Domain
            int n = inputData.getRows();
            double[] xData = new double[n];
            double[] yData = new double[n];
            
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                xData[i] = inputData.get(i, 0);
                yData[i] = inputData.get(i, 1);
                
                if (xData[i] < minX) minX = xData[i];
                if (xData[i] > maxX) maxX = xData[i];
            }

            String domainStr = "[" + NumberFormatter.round(minX) + ", " + NumberFormatter.round(maxX) + "]";

            // -Proses Interpolasi Polinomial
            List<String> langkah = new ArrayList<>();
            InterpolasiResult result = Interpolasi.hitungInterpolasi(xData, yData, langkah);

            if (result.getType() == InterpolasiResult.ResultType.SUCCESS) {
                // Merakit string persamaan
                double[][] koef = result.getKoefisien();
                StringBuilder persamaan = new StringBuilder();
                for (int i = 0; i < koef.length; i++) {
                    persamaan.append(NumberFormatter.round(koef[i][0]));
                    if (i > 0) persamaan.append("x^").append(i);
                    if (i < koef.length - 1) persamaan.append(" + ");
                }

                // Evaluasi titik xt 
                System.out.println("\n>> Persamaan berhasil dibentuk!");
                System.out.println(">> Domain Interpolasi: " + domainStr);
                
                StringBuilder hasilEvaluasi = new StringBuilder();
                while(true) {
                    System.out.print("\nIngin menaksir nilai P(xt)? (y/n): ");
                    String jawab = scanner.nextLine();
                    if (jawab.equalsIgnoreCase("n")) {
                        break;
                    } else if (jawab.equalsIgnoreCase("y")) {
                        System.out.print("Masukkan nilai xt: ");
                        try {
                            double xTarget = Double.parseDouble(scanner.nextLine());
                            
                            // Peringatan jika nilai di luar domain 
                            if (xTarget < minX || xTarget > maxX) {
                                System.out.println("Peringatan: Nilai xt (" + xTarget + ") berada di LUAR domain " + domainStr);
                            }
                            
                            double taksiran = Interpolasi.taksirNilaiPolinomial(koef, xTarget);
                            String resText = "P(" + NumberFormatter.round(xTarget) + ") = " + NumberFormatter.round(taksiran);
                            System.out.println("--> Hasil: " + resText);
                            
                            // Menambahkan ke string agar tersimpan di .txt
                            hasilEvaluasi.append("- ").append(resText).append("\n");
                            
                        } catch (NumberFormatException e) {
                            System.out.println("Input tidak valid. Harap masukkan angka.");
                        }
                    }
                }

                // Menyusun teks hasil akhir untuk disimpan ke file .txt
                String teksHasil = OutputWriter.teksInterpolasi(namaMetode, inputData, langkah, result, domainStr, persamaan.toString(), hasilEvaluasi.toString());
                
                System.out.println("\n================ KESIMPULAN ================");
                System.out.println(teksHasil);
                System.out.println("============================================");
                
                System.out.print("\nSimpan hasil keseluruhan ini ke file .txt? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    System.out.print("Masukkan nama/path file tujuan (contoh: test/hasil_interpolasi.txt): ");
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