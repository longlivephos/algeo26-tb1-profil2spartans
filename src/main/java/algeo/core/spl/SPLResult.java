package algeo.core.spl;

public class SPLResult {
    public enum ResultType { UNIQUE, NO_SOLUTION, INFINITE }

    private final ResultType type;
    private double[] solution;
    private String parametricDescription;

    private SPLResult(ResultType type) {
        this.type = type;
    }

    public static SPLResult unique(double[] solution) {
        SPLResult r = new SPLResult(ResultType.UNIQUE);
        r.solution = solution;
        return r;
    }

    public static SPLResult noSolution() {
        return new SPLResult(ResultType.NO_SOLUTION);
    }

    public static SPLResult infinite(String parametricDescription) {
        SPLResult r = new SPLResult(ResultType.INFINITE);
        r.parametricDescription = parametricDescription;
        return r;
    }

    public ResultType getType() { return type; }
    public double[] getSolution() { return solution; }
    public String getParametricDescription() { return parametricDescription; }
}