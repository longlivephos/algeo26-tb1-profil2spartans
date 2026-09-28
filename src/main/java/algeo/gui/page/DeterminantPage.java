package algeo.gui.page;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.gui.component.MatrixInput;
import algeo.io.OutputWriter;

public class DeterminantPage extends FeaturePage {

    private JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Reduksi Baris (OBE)",
        "Ekspansi Kofaktor"
    });

    public DeterminantPage() {
        super(new MatrixInput("Baris:", 3, 11, "Kolom:", 3, 11, false));
        tambahOpsi(new JLabel("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix m = input.bacaMatriks();
        String metode = (String) pilihanMetode.getSelectedItem();
        List<String> langkah = new ArrayList<>();

        if (!m.isSquare()) {
            String teks = "Metode: " + metode + "\n\nInput matriks:\n" + m + "\nHasil:\nmatriks tidak memiliki determinan (matriks tidak persegi)";
            mulaiTampilkan(langkah, teks);
            return;
        }

        double det;
        if (pilihanMetode.getSelectedIndex() == 0) {
            det = OBE.hitungOBE(m, langkah);
        } else {
            langkah.add("Matriks input A:\n" + m);
            det = Kofaktor.hitungKofaktor(m, langkah);
        }
        mulaiTampilkan(langkah, OutputWriter.teksDeterminan(metode, m, langkah, det));
    }
}
