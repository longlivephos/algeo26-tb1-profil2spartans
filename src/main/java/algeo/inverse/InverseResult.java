package algeo.inverse;

import algeo.core.matrix.Matrix;

public class InverseResult {
    public enum ResultType { HAS_INVERSE, NO_INVERSE }

    private final ResultType type;
    private Matrix inverse;
    private String errorMessage;

    private InverseResult(ResultType type) {
        this.type = type;
    }

    /**
     * Membuat hasil untuk matriks yang punya invers.
     * @param inverse matriks inversnya
     * @return objek hasil bertipe HAS_INVERSE
     */
    public static InverseResult exists(Matrix inverse) {
        InverseResult r = new InverseResult(ResultType.HAS_INVERSE);
        r.inverse = inverse;
        return r;
    }

    /**
     * Membuat hasil untuk matriks yang tidak punya invers.
     * @param errorMessage kasih alasan tidak punya invers
     * @return objek hasil bertipe NO_INVERSE
     */
    public static InverseResult noInverse(String errorMessage) {
        InverseResult r = new InverseResult(ResultType.NO_INVERSE);
        r.errorMessage = errorMessage;
        return r;
    }

    /** @return jenis hasil */
    public ResultType getType() { return type; }
    
    /** @return matriks invers, null kalau tidak ada */
    public Matrix getInverse() { return inverse; }
    
    /** @return pesan alasan, null kalau punya invers */
    public String getErrorMessage() { return errorMessage; }
}
