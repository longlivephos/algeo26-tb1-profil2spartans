package algeo.gui.page;

import javax.swing.JComboBox;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.spl.Cramer;
import algeo.core.spl.GaussEliminationSolver;
import algeo.core.spl.GaussJordan;
import algeo.core.spl.InverseMethod;
import algeo.core.spl.SPLResult;
import algeo.core.spl.SolverOutput;
import algeo.gui.component.MatrixInput;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.io.OutputWriter;

/**
 * Halaman Sistem Persamaan Linier (Gauss, Gauss-Jordan, matriks balikan, Cramer).
 */
public class SplPage extends FeaturePage {

    private final JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Eliminasi Gauss",
        "Eliminasi Gauss-Jordan",
        "Metode Matriks Balikan",
        "Kaidah Cramer"
    });

    public SplPage() {
        super("SISTEM PERSAMAAN LINIER",
              "Atur jumlah persamaan dan variabel, lalu isi koefisien tiap persamaan. "
              + "Desimal boleh pakai titik atau koma. Input manual maksimal 11 variabel, "
              + "file .txt sampai 1001 variabel.",
              new MatrixInput(MatrixInput.Gaya.PERSAMAAN, 3, 3, 11, 11));
        ThemedUI.gayakan(pilihanMetode);
        tambahBarisOpsi(Theme.label("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix augmented = grid.bacaMatriks();

        SolverOutput output;
        switch (pilihanMetode.getSelectedIndex()) {
            case 0: output = new GaussEliminationSolver().solve(augmented); break;
            case 1: output = new GaussJordan().solve(augmented); break;
            case 2: output = new InverseMethod().solve(augmented); break;
            default: output = new Cramer().solve(augmented); break;
        }

        String metode = (String) pilihanMetode.getSelectedItem();
        String teks = OutputWriter.teksSPL(metode, augmented, output.getSteps(), output.getResult());
        String status;
        if (output.getResult().getType() == SPLResult.ResultType.UNIQUE) status = "SOLUSI TUNGGAL";
        else if (output.getResult().getType() == SPLResult.ResultType.NO_SOLUTION) status = "TIDAK ADA SOLUSI";
        else status = "BANYAK SOLUSI";
        tampilkanProses(output.getSteps(), teks, status, null);
    }
}
