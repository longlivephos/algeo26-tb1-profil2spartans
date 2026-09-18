package algeo.core.matrix;
import java.util.Locale;

import algeo.core.util.NumberFormatter;

public class Matrix {
    private double[][] matriks;
    private int rows;
    private int cols;

    /**
     * Membuat matriks kosong (semua elemen 0)
     * @param rows jumlah baris
     * @param cols jumlah kolom
     * @throws IllegalArgumentException jika rows atau cols kurang dari 1
     */
    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Ukuran matriks harus lebih besar dari 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.matriks = new double[rows][cols];
    }

    /**
     * Membuat matriks dari array 2 dimensi (isinya disalin)
     * @param data array 2 dimensi
     * @throws MatrixException jika array kosong atau jumlah kolom tiap baris berbeda
     */
    public Matrix(double[][] data) throws MatrixException {
        if (data.length == 0 || data[0].length == 0) {
            throw new MatrixException("Matriks tidak boleh kosong");
        }
        for (double[] baris : data) {
            if (baris.length != data[0].length) {
                throw new MatrixException("Setiap baris matriks harus memiliki jumlah kolom yang sama");
            }
        }
        this.rows = data.length;
        this.cols = data[0].length;
        this.matriks = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, matriks[i], 0, cols);
        }
    }

    /** @return jumlah baris */
    public int getRows() {
        return rows;
    }

    /** @return jumlah kolom */
    public int getCols() {
        return cols;
    }

    /** @return true jika matriks persegi (baris sama dengan kolom) */
    public boolean isSquare() {
        return rows == cols;
    }

    /**
     * Mengambil satu elemen (indeks mulai dari 0)
     * @param i baris
     * @param j kolom
     * @return nilai elemen
     */
    public double get(int i, int j) {
        return matriks[i][j];
    }

    /**
     * Mengubah satu elemen (indeks mulai dari 0)
     * @param i baris
     * @param j kolom
     * @param val nilai baru
     */
    public void set(int i, int j, double val) {
        matriks[i][j] = val;
    }

    /**
     * Menjumlahkan matriks ini dengan matriks lain
     * @param other matriks yang dijumlahkan
     * @return matriks hasil penjumlahan
     * @throws MatrixException jika ukuran kedua matriks berbeda
     */
    public Matrix add(Matrix other) throws MatrixException {
        if (rows != other.rows || cols != other.cols) {
            throw new MatrixException("Dimensi matriks tidak sama, tidak bisa dijumlahkan");
        }
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.matriks[i][j] = matriks[i][j] + other.matriks[i][j];
            }
        }
        return result;
    }

    /**
     * Mengurangkan matriks ini dengan matriks lain
     * @param other matriks pengurang
     * @return matriks hasil pengurangan
     * @throws MatrixException jika ukuran kedua matriks berbeda
     */
    public Matrix subtract(Matrix other) throws MatrixException {
        if (rows != other.rows || cols != other.cols) {
            throw new MatrixException("Dimensi matriks tidak sama, tidak bisa dikurangkan");
        }
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.matriks[i][j] = matriks[i][j] - other.matriks[i][j];
            }
        }
        return result;
    }

    /**
     * Mengalikan matriks ini dengan matriks lain
     * @param other matriks pengali
     * @return matriks hasil perkalian
     * @throws MatrixException jika kolom matriks ini tidak sama dengan baris other
     */
    public Matrix multiply(Matrix other) throws MatrixException {
        if (cols != other.rows) {
            throw new MatrixException("Jumlah kolom matriks pertama harus sama dengan jumlah baris matriks kedua");
        }
        Matrix result = new Matrix(rows, other.cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double sum = 0;
                for (int k = 0; k < cols; k++) {
                    sum += matriks[i][k] * other.matriks[k][j];
                }
                result.matriks[i][j] = sum;
            }
        }
        return result;
    }

    /**
     * Membuat transpose matriks
     * @return matriks transpose (ukuran cols x rows)
     */
    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.matriks[j][i] = matriks[i][j];
            }
        }
        return result;
    }

    /**
     * Mengambil sebagian matriks dimana batas awal dan akhir ikut diambil
     * @param rowStart baris awal
     * @param rowEnd baris akhir
     * @param colStart kolom awal
     * @param colEnd kolom akhir
     * @return submatriks
     */
    public Matrix getSubmatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
        Matrix result = new Matrix(rowEnd - rowStart + 1, colEnd - colStart + 1);
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                result.matriks[i - rowStart][j - colStart] = matriks[i][j];
            }
        }
        return result;
    }

    /**
     * Membuat minor, yaitu matriks ini tanpa satu baris dan satu kolom gitulah
     * Dipakai untuk determinan kofaktor dan invers adjoin
     * @param excludeRow baris yang dibuang
     * @param excludeCol kolom yang dibuang
     * @return matriks berukuran (rows-1) x (cols-1)
     */
    public Matrix getMinor(int excludeRow, int excludeCol) {
        Matrix result = new Matrix(rows - 1, cols - 1);
        int ri = 0;
        for (int i = 0; i < rows; i++) {
            if (i == excludeRow) continue;
            int rj = 0;
            for (int j = 0; j < cols; j++) {
                if (j == excludeCol) continue;
                result.matriks[ri][rj] = matriks[i][j];
                rj++;
            }
            ri++;
        }
        return result;
    }

    /**
     * Membuat salinan matriks
     * @return matriks baru dengan isi yang sama
     */
    public Matrix copy() {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matriks[i], 0, result.matriks[i], 0, cols);
        }
        return result;
    }

    /**
     * Mengubah matriks jadi teks dengan 3 angka di belakang koma
     * @return teks matriks
     */
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < rows; i++) {
            s += "[ ";
            for (int j = 0; j < cols; j++) {
                s += String.format(Locale.US, "%.3f", NumberFormatter.round(matriks[i][j]));
                if (j < cols - 1) s += "  ";
            }
            s += " ]\n";
        }
        return s;
    }
}