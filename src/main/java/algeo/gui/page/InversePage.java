package algeo.gui.page;

import java.util.ArrayList;

import javax.swing.JComboBox;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.gui.component.MatrixInput;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;
import algeo.io.OutputWriter;

/**
 * Halaman matriks balikan (augmentasi [A | I] dan adjoin).
 */
public class InversePage extends FeaturePage {

    private final JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Augmentasi [A | I]",
        "Adjoin (Kofaktor)"
    });

    public InversePage() {
        super("MATRIKS BALIKAN", "Isi matriks persegi satu angka per kotak. Input manual maksimal 11 x 11, " + "file .txt sampai 1001 x 1001.", new MatrixInput(MatrixInput.Gaya.MATRIKS, 3, 3, 11, 11));
        ThemedUI.gayakan(pilihanMetode);
        tambahBarisOpsi(Theme.label("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix m = grid.bacaMatriks();
        String metode = (String) pilihanMetode.getSelectedItem();

        if (!m.isSquare()) {
            InverseResult gagal = InverseResult.noInverse("matriks tidak persegi, " + m.getRows() + " x " + m.getCols());
            tampilkanProses(new ArrayList<>(), OutputWriter.teksInvers(metode, m, new ArrayList<>(), gagal), "TIDAK ADA BALIKAN", null);
            return;
        }

        InverseSolverOutput output;
        if (pilihanMetode.getSelectedIndex() == 0) {
            output = Augmentasi.hitungInversAugmentasi(m);
        } else {
            output = Adjoin.hitungInversAdjoin(m);
        }
        String teks = OutputWriter.teksInvers(metode, m, output.getSteps(), output.getResult());
        boolean ada = output.getResult().getType() == InverseResult.ResultType.HAS_INVERSE;
        tampilkanProses(output.getSteps(), teks, ada ? "PUNYA BALIKAN" : "TIDAK ADA BALIKAN", null);
    }
}
