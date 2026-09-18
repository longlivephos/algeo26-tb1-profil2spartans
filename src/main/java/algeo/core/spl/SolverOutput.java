package algeo.core.spl;
import java.util.List;

public class SolverOutput {
    private final SPLResult result;
    private final List<String> steps;

    public SolverOutput(SPLResult result, List<String> steps) {
        this.result = result;
        this.steps = steps;
    }

    public SPLResult getResult() { return result; }
    public List<String> getSteps() { return steps; }
}