package algeo.inverse;

import java.util.List;

public class InverseSolverOutput {
    private final InverseResult result;
    private final List<String> steps;

    public InverseSolverOutput(InverseResult result, List<String> steps) {
        this.result = result;
        this.steps = steps;
    }

    public InverseResult getResult() { return result; }
    public List<String> getSteps() { return steps; }
}