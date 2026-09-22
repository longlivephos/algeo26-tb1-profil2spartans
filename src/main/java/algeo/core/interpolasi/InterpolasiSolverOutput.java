package algeo.core.interpolasi;

import java.util.List;

public class InterpolasiSolverOutput {
    private final InterpolasiResult result;
    private final List<String> steps;

    /**
     * @param result Hasil perhitungan interpolasi
     * @param steps Daftar catatan langkah-langkah pengerjaan
     */
    public InterpolasiSolverOutput(InterpolasiResult result, List<String> steps) {
        this.result = result;
        this.steps = steps;
    }

    public InterpolasiResult getResult() { return result; }
    public List<String> getSteps() { return steps; }
}