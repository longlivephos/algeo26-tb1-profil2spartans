package algeo.inverse;

import java.util.List;

public class InverseSolverOutput {
    private final InverseResult result;
    private final List<String> steps;

    /**
     * @param result hasil perhitungan
     * @param steps langkah-langkah perhitungan
     */
    public InverseSolverOutput(InverseResult result, List<String> steps) {
        this.result = result;
        this.steps = steps;
    }

    /** @return hasil perhitungan */
    public InverseResult getResult() { return result; }
    
    /** @return daftar langkah */
    public List<String> getSteps() { return steps; }
}