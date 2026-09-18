package algeo.core.matrix;
import algeo.core.util.NumberFormatter;

public class Matrix {
    private final double[][] matriks;
    private final int rows, cols;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Ukuran matriks harus lebih besar dari 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.matriks = new double[rows][cols];
    }

    public Matrix(double[][] matriks) throws MatrixException {
        int expectedCols = matriks.length > 0 ? matriks[0].length : 0;
        for (double[] row : matriks) {
            if (row.length != expectedCols) {
                throw new MatrixException("Setiap baris matriks harus memiliki jumlah kolom yang sama");
            }
        }
        this.matriks = matriks;
        this.rows = matriks.length;
        this.cols = expectedCols;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public boolean isSquare() { return rows == cols; }
    public double get(int i, int j) { return matriks[i][j]; }
    public void set(int i, int j, double val) { matriks[i][j] = val; }

    public Matrix add(Matrix other) throws MatrixException {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new MatrixException("Dimensi matriks tidak sama, tidak bisa dijumlahkan");
        }
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.matriks[i][j] = this.matriks[i][j] + other.matriks[i][j];
        return result;
    }

    public Matrix subtract(Matrix other) throws MatrixException {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new MatrixException("Dimensi matriks tidak sama, tidak bisa dikurangkan");
        }
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.matriks[i][j] = this.matriks[i][j] - other.matriks[i][j];
        return result;
    }

    public Matrix multiply(Matrix other) throws MatrixException {
        if (this.cols != other.rows) {
            throw new MatrixException("Jumlah kolom matriks pertama harus sama dengan jumlah baris matriks kedua");
        }
        Matrix result = new Matrix(this.rows, other.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double sum = 0;
                for (int k = 0; k < this.cols; k++)
                    sum += this.matriks[i][k] * other.matriks[k][j];
                result.matriks[i][j] = sum;
            }
        }
        return result;
    }

    public Matrix scalarMultiply(double k) {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.matriks[i][j] = this.matriks[i][j] * k;
        return result;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.matriks[j][i] = this.matriks[i][j];
        return result;
    }

    public Matrix getSubmatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
        int newRows = rowEnd - rowStart + 1;
        int newCols = colEnd - colStart + 1;
        Matrix result = new Matrix(newRows, newCols);
        for (int i = 0; i < newRows; i++)
            for (int j = 0; j < newCols; j++)
                result.matriks[i][j] = this.matriks[rowStart + i][colStart + j];
        return result;
    }

    public Matrix getMinor(int excludeRow, int excludeCol) {
        Matrix result = new Matrix(rows - 1, cols - 1);
        int ri = 0;
        for (int i = 0; i < rows; i++) {
            if (i == excludeRow) continue;
            int rj = 0;
            for (int j = 0; j < cols; j++) {
                if (j == excludeCol) continue;
                result.matriks[ri][rj] = this.matriks[i][j];
                rj++;
            }
            ri++;
        }
        return result;
    }

    public Matrix copy() {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            System.arraycopy(this.matriks[i], 0, result.matriks[i], 0, cols);
        return result;
    }

    public static Matrix identity(int n) {
        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++)
            result.matriks[i][i] = 1.0;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append("[ ");
            for (int j = 0; j < cols; j++) {
                sb.append(String.format("%.3f", NumberFormatter.round(matriks[i][j])));
                if (j < cols - 1) sb.append("  ");
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }
}