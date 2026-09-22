package algeo.core.interpolasi;

public class InterpolasiResult {
    public enum ResultType { SUCCESS, FAILED }

    private final ResultType type;
    private double[][] koefisien; // Ukuran [n][4] -> [a, b, c, d] per segmen
    private double[] xData;
    private double[] yData;
    private String errorMessage;

    private InterpolasiResult(ResultType type) {
        this.type = type;
    }

    /**
     * Membuat objek hasil jika interpolasi berhasil.
     * 
     * @param koefisien Matriks koefisien [n][4]
     * @param xData Array x asal
     * @param yData Array y asal
     * @return InterpolasiResult bertipe SUCCESS
     */
    public static InterpolasiResult success(double[][] koefisien, double[] xData, double[] yData) {
        InterpolasiResult r = new InterpolasiResult(ResultType.SUCCESS);
        r.koefisien = koefisien;
        r.xData = xData;
        r.yData = yData;
        return r;
    }

    /**
     * Membuat objek hasil jika interpolasi gagal.
     * 
     * @param errorMessage Pesan penyebab kegagalan
     * @return InterpolasiResult bertipe FAILED
     */
    public static InterpolasiResult failed(String errorMessage) {
        InterpolasiResult r = new InterpolasiResult(ResultType.FAILED);
        r.errorMessage = errorMessage;
        return r;
    }

    public ResultType getType() { return type; }
    public double[][] getKoefisien() { return koefisien; }
    public double[] getXData() { return xData; }
    public double[] getYData() { return yData; }
    public String getErrorMessage() { return errorMessage; }
}
