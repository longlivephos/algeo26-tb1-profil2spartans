package algeo.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.DecimalParser;


public class MatrixReader {

    public static final int MAKS_MANUAL_BARIS = 11;
    public static final int MAKS_MANUAL_KOLOM = 11;
    public static final int MAKS_MANUAL_KOLOM_AUGMENTED = 12;
    public static final int MAKS_FILE = 1001;
    public static final int MAKS_TITIK = 10;

    private MatrixReader() {}

    /**
     * Mengubah teks -> Matrix.
     * @param isi teks raw
     * @return matriks hasil parsing
     * @throws MatrixException jika format tidak valid: baris kosong di
     *         tengah teks, jumlah kolom antarbaris tidak konsisten, elemen
     *         bukan angka, atau ukuran melebihi batas 
     */
    public static Matrix parseTeks(String isi) throws MatrixException {
        if (isi == null) {
            throw new MatrixException("Teks matriks tidak boleh kosong");
        }
        String[] rawLines = isi.split("\r?\n", -1);

        int akhir = rawLines.length;
        while (akhir > 0 && rawLines[akhir - 1].trim().isEmpty()) {
            akhir--;
        }
        if (akhir == 0) {
            throw new MatrixException("Teks matriks tidak boleh kosong");
        }

        int n = akhir;
        if (n > MAKS_FILE) {
            throw new MatrixException("Jumlah baris matriks (" + n + ") melebihi batas maksimal " + MAKS_FILE);
        }

        int kolomEkspektasi = -1;
        double[][] data = new double[n][];
        for (int i = 0; i < n; i++) {
            String barisKe = rawLines[i];
            if (barisKe.trim().isEmpty()) {
                throw new MatrixException("Baris ke-" + (i + 1) + " kosong, format tidak sesuai");
            }
            String[] token = barisKe.trim().split("\\s+");
            if (kolomEkspektasi == -1) {
                kolomEkspektasi = token.length;
                if (kolomEkspektasi > MAKS_FILE) {
                    throw new MatrixException("Jumlah kolom (" + kolomEkspektasi + ") melebihi batas maksimal " + MAKS_FILE);
                }
            } else if (token.length != kolomEkspektasi) {
                throw new MatrixException("Jumlah kolom pada baris ke-" + (i + 1) + " (" + token.length
                        + ") tidak konsisten dengan baris sebelumnya (" + kolomEkspektasi + ")");
            }
            double[] row = new double[token.length];
            for (int j = 0; j < token.length; j++) {
                row[j] = parseAngka(token[j], i + 1, j + 1);
            }
            data[i] = row;
        }
        return new Matrix(data);
    }

    /**
     * Membaca isi file
     * @param path lokasi file .txt
     * @return matriks hasil parsing
     * @throws MatrixException jika file tidak ditemukan, gagal dibaca, atau
     *         isinya tidak sesuai format
     */

    public static Matrix bacaFile(String path) throws MatrixException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean pertama = true;
            while ((line = br.readLine()) != null) {
                if (!pertama) sb.append("\n");
                sb.append(line);
                pertama = false;
            }
        } catch (FileNotFoundException e) {
            throw new MatrixException("Berkas '" + path + "' tidak ditemukan");
        } catch (IOException e) {
            throw new MatrixException("Gagal membaca berkas '" + path + "': " + e.getMessage());
        }
        return parseTeks(sb.toString());
    }

    private static double parseAngka(String token, int baris, int kolom) throws MatrixException {
        try {
            return DecimalParser.parse(token);
        } catch (NumberFormatException e) {
            throw new MatrixException("Elemen pada baris " + baris + " kolom " + kolom + ": " + e.getMessage());
        }
    }
}