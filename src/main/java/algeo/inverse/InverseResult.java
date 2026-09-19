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

    public static InverseResult exists(Matrix inverse) {
        InverseResult r = new InverseResult(ResultType.HAS_INVERSE);
        r.inverse = inverse;
        return r;
    }

    public static InverseResult noInverse(String errorMessage) {
        InverseResult r = new InverseResult(ResultType.NO_INVERSE);
        r.errorMessage = errorMessage;
        return r;
    }

    public ResultType getType() { return type; }
    public Matrix getInverse() { return inverse; }
    public String getErrorMessage() { return errorMessage; }
}