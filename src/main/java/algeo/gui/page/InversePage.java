package algeo.gui.page;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.gui.component.MatrixInput;
import algeo.inverse.Adjoin;
import algeo.inverse.Augmentasi;
import algeo.inverse.InverseResult;
import algeo.inverse.InverseSolverOutput;
import algeo.io.OutputWriter;

public class InversePage extends FeaturePage {

    private JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Augmentasi [A | I]",
        "Adjoin (Kofaktor)"
    });

    public InversePage() {
        super(new MatrixInput("Baris:", 3, 11, "Kolom:", 3, 11, false));
        tambahOpsi(new JLabel("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix m = input.bacaMatriks();
        String metode = (String) pilihanMetode.getSelectedItem();

        if (!m.isSquare()) {
            InverseResult gagal = InverseResult.noInverse("matriks tidak persegi");
            mulaiTampilkan(new ArrayList<>(), OutputWriter.teksInvers(metode, m, new ArrayList<>(), gagal));
            return;
        }

        InverseSolverOutput output;
        if (pilihanMetode.getSelectedIndex() == 0) {
            output = Augmentasi.hitungInversAugmentasi(m);
        } else {
            output = Adjoin.hitungInversAdjoin(m);
        }
        mulaiTampilkan(output.getSteps(), OutputWriter.teksInvers(metode, m, output.getSteps(), output.getResult()));
    }
}
