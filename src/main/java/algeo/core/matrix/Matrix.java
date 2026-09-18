package algeo.core.matrix;

public class Matrix {
    private final double[][] data;
    private final int rows, cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Matrix(double[][] data) {
        // TODO: validasi setiap baris punya panjang kolom yang sama,
        // lempar MatrixException kalau tidak konsisten
        this.data = data;
        this.rows = data.length;
        this.cols = data.length > 0 ? data[0].length : 0;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public boolean isSquare() { return rows == cols; }

    public double get(int i, int j) { return data[i][j]; }
    public void set(int i, int j, double val) { data[i][j] = val; }

    public Matrix add(Matrix other) throws MatrixException {
        // TODO: cek dimensi sama, lempar MatrixException kalau beda
        return null;
    }

    public Matrix subtract(Matrix other) throws MatrixException {
        // TODO
        return null;
    }

    public Matrix multiply(Matrix other) throws MatrixException {
        // TODO: cek this.cols == other.rows
        return null;
    }

    public Matrix scalarMultiply(double k) {
        // TODO
        return null;
    }

    public Matrix transpose() {
        // TODO
        return null;
    }

    public Matrix getSubmatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
        // TODO: dipakai buat pisah augmented matrix [A|b] -> A dan b
        return null;
    }

    public Matrix getMinor(int excludeRow, int excludeCol) {
        // TODO: dipakai kofaktor determinan
        return null;
    }

    public Matrix copy() {
        // TODO
        return null;
    }

    public static Matrix identity(int n) {
        // TODO: dipakai invers augmentasi [A|I]
        return null;
    }

    @Override
    public String toString() {
        // TODO: format tampil matriks buat langkah-langkah perhitungan
        return null;
    }
}