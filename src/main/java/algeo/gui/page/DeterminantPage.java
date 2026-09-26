package algeo.gui.page;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.determinant.Kofaktor;
import algeo.determinant.OBE;
import algeo.gui.component.MatrixInput;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.io.OutputWriter;

/**
 * Halaman determinan (reduksi baris dan ekspansi kofaktor).
 */
public class DeterminantPage extends FeaturePage {

    private final JComboBox<String> pilihanMetode = new JComboBox<>(new String[] {
        "Reduksi Baris (OBE)",
        "Ekspansi Kofaktor"
    });

    public DeterminantPage() {
        super("DETERMINAN",
              "Isi matriks persegi satu angka per kotak. Input manual maksimal 11 x 11, "
              + "file .txt sampai 1001 x 1001.",
              new MatrixInput(MatrixInput.Gaya.MATRIKS, 3, 3, 11, 11));
        ThemedUI.gayakan(pilihanMetode);
        tambahBarisOpsi(Theme.label("Metode:"), pilihanMetode);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix m = grid.bacaMatriks();
        String metode = (String) pilihanMetode.getSelectedItem();
        List<String> langkah = new ArrayList<>();

        if (!m.isSquare()) {
            String teks = "Metode: " + metode + "\n\nInput matriks:\n" + m
                    + "\nHasil:\nmatriks tidak memiliki determinan (matriks tidak persegi, "
                    + m.getRows() + " x " + m.getCols() + ")";
            tampilkanProses(langkah, teks, "TIDAK ADA DETERMINAN", null);
            return;
        }

        double det;
        if (pilihanMetode.getSelectedIndex() == 0) {
            det = OBE.hitungOBE(m, langkah);
        } else {
            det = Kofaktor.hitungKofaktor(m, langkah);
            // library kofaktor tidak mencetak matriksnya, jadi ditampilkan di langkah pertama
            // supaya tampilannya sama dengan metode lain
            langkah.add(0, "Matriks input A:\n" + m);
        }
        tampilkanProses(langkah, OutputWriter.teksDeterminan(metode, m, langkah, det),
                "det = " + NumberFormatter.teks(det), null);
    }
}
