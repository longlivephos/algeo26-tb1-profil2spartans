package algeo.modules;

import java.io.IOException;
import java.util.Scanner;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseSolverOutput;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

public class InversModule implements Module {

    @Override
    public void jalankan(Scanner sc) {
        System.out.println();
        System.out.println("*~* SUB-MENU INVERS *~*");
        System.out.println("1. Metode Augmentasi [A | I] (Gauss-Jordan)");
        System.out.println("2. Metode Adjoin (Kofaktor)");
        System.out.print("Pilihan metode: ");
        String pilihan = sc.nextLine().trim();
        String namaMetode = pilihan.equals("2") ? "Metode Adjoin" : "Metode Augmentasi";

        Matrix m;
        try {
            m = bacaMatriksPersegi(sc);
        } catch (MatrixException e) {
            System.out.println("Input tidak valid: " + e.getMessage());
            return;
        }

        InverseSolverOutput output;
        try {
            output = pilihan.equals("2")
                    ? Adjoin.hitungInversAdjoin(m)
                    : Augmentasi.hitungInversAugmentasi(m);
        } catch (MatrixException e) {
            System.out.println("Matriks tidak memiliki balikan: " + e.getMessage());
            return;
        }

        String teksLengkap = OutputWriter.teksInvers(namaMetode, m, output.getSteps(), output.getResult());
        System.out.println("\n================ KESIMPULAN ================");
        System.out.println(teksLengkap);
        System.out.println("============================================");

        tanyaSimpan(sc, teksLengkap);
    }

    private Matrix bacaMatriksPersegi(Scanner sc) throws MatrixException {
        System.out.println("Sumber input: 1) Manual  2) File .txt");
        System.out.print("Pilihan: ");
        String sumber = sc.nextLine().trim();

        if (sumber.equals("2")) {
            System.out.print("Masukkan path file (contoh: test/inverse_1.txt): ");
            return MatrixReader.bacaFile(sc.nextLine().trim());
        }

        System.out.print("Ukuran matriks persegi (n): ");
        int n;
        try {
            n = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new MatrixException("Ukuran matriks harus berupa bilangan bulat");
        }
        if (n <= 0 || n > MatrixReader.MAKS_MANUAL_BARIS) {
            throw new MatrixException("Ukuran manual dibatasi 1-" + MatrixReader.MAKS_MANUAL_BARIS);
        }

        System.out.println("Masukkan tiap baris matriks (elemen dipisah spasi, '.' atau ',' sbg desimal):");
        StringBuilder teks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            System.out.print("Baris " + (i + 1) + ": ");
            if (i > 0) teks.append("\n");
            teks.append(sc.nextLine());
        }

        Matrix hasil = MatrixReader.parseTeks(teks.toString());
        if (hasil.getCols() > MatrixReader.MAKS_MANUAL_KOLOM) {
            throw new MatrixException("Jumlah kolom manual dibatasi maksimal " + MatrixReader.MAKS_MANUAL_KOLOM);
        }
        return hasil;
    }

    private void tanyaSimpan(Scanner sc, String teks) {
        System.out.print("\nSimpan hasil keseluruhan ini ke file .txt? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;

        System.out.print("Masukkan nama/path file tujuan (contoh: test/hasil_invers.txt): ");
        String pathSimpan = sc.nextLine().trim();
        try {
            OutputWriter.simpan(pathSimpan, teks);
            System.out.println("Hasil berhasil disimpan di: " + pathSimpan);
        } catch (IOException e) {
            System.out.println("Gagal menyimpan file: " + e.getMessage());
        }
    }
}