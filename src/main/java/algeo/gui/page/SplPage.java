package algeo.gui.page;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.Cramer;
import algeo.core.spl.GaussEliminationSolver;
import algeo.core.spl.GaussJordan;
import algeo.core.spl.InverseMethod;
import algeo.core.spl.SolverOutput;
import algeo.gui.component.MatrixInput;
import algeo.io.OutputWriter;

public class SplPage extends FeaturePage {

    private JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Eliminasi Gauss",
        "Eliminasi Gauss-Jordan",
        "Metode Matriks Balikan",
        "Kaidah Cramer"
    });

    public SplPage() {
        super(new MatrixInput("Persamaan:", 3, 11, "Variabel:", 3, 11, true));
        tambahOpsi(new JLabel("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix augmented = input.bacaMatriks();

        SolverOutput output;
        int pilihan = pilihanMetode.getSelectedIndex();
        if (pilihan == 0) {
            output = new GaussEliminationSolver().solve(augmented);
        } else if (pilihan == 1) {
            output = new GaussJordan().solve(augmented);
        } else if (pilihan == 2) {
            output = new InverseMethod().solve(augmented);
        } else {
            output = new Cramer().solve(augmented);
        }

        String metode = (String) pilihanMetode.getSelectedItem();
        String teks = OutputWriter.teksSPL(metode, augmented, output.getSteps(), output.getResult());
        mulaiTampilkan(output.getSteps(), teks);
    }
}
