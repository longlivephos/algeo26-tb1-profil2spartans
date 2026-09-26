package algeo.modules;

import java.io.IOException;
import java.util.Scanner;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.Cramer;
import algeo.core.spl.GaussEliminationSolver;
import algeo.core.spl.GaussJordan;
import algeo.core.spl.InverseMethod;
import algeo.core.spl.SolverOutput;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

public class SPLModule implements Module {

    @Override
    public void jalankan(Scanner sc) {
        System.out.println();
        System.out.println("*~* SUB-MENU SPL *~*");
        System.out.println("1. Metode Eliminasi Gauss");
        System.out.println("2. Metode Eliminasi Gauss-Jordan");
        System.out.println("3. Metode Matriks Balikan");
        System.out.println("4. Kaidah Cramer");
        System.out.print("Pilihan metode: ");
        String metodePilihan = sc.nextLine().trim();

        String namaMetode;
        switch (metodePilihan) {
            case "1": namaMetode = "Eliminasi Gauss"; break;
            case "2": namaMetode = "Eliminasi Gauss-Jordan"; break;
            case "3": namaMetode = "Metode Matriks Balikan"; break;
            case "4": namaMetode = "Kaidah Cramer"; break;
            default:
                System.out.println("Pilihan metode tidak dikenal.");
                return;
        }

        Matrix augmented;
        try {
            augmented = bacaAugmented(sc);
        } catch (MatrixException e) {
            System.out.println("Input tidak valid: " + e.getMessage());
            return;
        }

        SolverOutput output;
        try {
            switch (metodePilihan) {
                case "1": output = new GaussEliminationSolver().solve(augmented); break;
                case "2": output = new GaussJordan().solve(augmented); break;
                case "3": output = new InverseMethod().solve(augmented); break;
                case "4": output = new Cramer().solve(augmented); break;
                default: return;
            }
        } catch (MatrixException e) {
            System.out.println("Tidak bisa menyelesaikan SPL: " + e.getMessage());
            return;
        }

        String teksLengkap = OutputWriter.teksSPL(namaMetode, augmented, output.getSteps(), output.getResult());
        System.out.println();
        System.out.println(teksLengkap);

        tanyaSimpan(sc, teksLengkap);
    }

    private Matrix bacaAugmented(Scanner sc) throws MatrixException {
        System.out.println("Sumber input: 1) Manual  2) File .txt");
        System.out.print("Pilihan: ");
        String sumber = sc.nextLine().trim();

        if (sumber.equals("2")) {
            System.out.print("Path file (spl_xxx.txt): ");
            return MatrixReader.bacaFile(sc.nextLine().trim());
        }

        System.out.print("Jumlah persamaan (baris): ");
        int baris;
        try {
            baris = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new MatrixException("Jumlah baris harus berupa bilangan bulat");
        }
        if (baris <= 0 || baris > MatrixReader.MAKS_MANUAL_BARIS) {
            throw new MatrixException("Jumlah baris manual dibatasi 1-" + MatrixReader.MAKS_MANUAL_BARIS);
        }

        System.out.println("Masukkan tiap baris augmented [A|b] (elemen dipisah spasi, '.' atau ',' sbg desimal):");
        StringBuilder teks = new StringBuilder();
        for (int i = 0; i < baris; i++) {
            System.out.print("Baris " + (i + 1) + ": ");
            if (i > 0) teks.append("\n");
            teks.append(sc.nextLine());
        }

        Matrix hasil = MatrixReader.parseTeks(teks.toString());
        if (hasil.getCols() > MatrixReader.MAKS_MANUAL_KOLOM_AUGMENTED) {
            throw new MatrixException("Jumlah kolom manual (augmented) dibatasi maksimal "
                    + MatrixReader.MAKS_MANUAL_KOLOM_AUGMENTED);
        }
        return hasil;
    }

    private void tanyaSimpan(Scanner sc, String teks) {
        System.out.print("Simpan hasil ke file .txt? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;

        System.out.print("Nama file output (tanpa .txt): ");
        String path = sc.nextLine().trim() + ".txt";
        try {
            OutputWriter.simpan(path, teks);
            System.out.println("Berhasil disimpan ke " + path);
        } catch (IOException e) {
            System.out.println("Gagal menyimpan file: " + e.getMessage());
        }
    }
}