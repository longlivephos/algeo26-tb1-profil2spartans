package algeo.gui.page;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.regression.RegressionOutput;
import algeo.core.regression.SplineRegression;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PixelButton;
import algeo.gui.component.PlotPanel;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.io.OutputWriter;

/**
 * Halaman regresi spline (truncated power basis) beserta prediksi dan grafik.
 */
public class RegressionPage extends FeaturePage {

    private final MatrixInput gridKnot = new MatrixInput(MatrixInput.Gaya.KNOT, 1, 1, 9, 1);
    private final JComboBox<String> pilihanDerajat = new JComboBox<>(new String[] {
        "3 (kubik)",
        "2 (kuadratik)",
        "1 (linear)"
    });
    private final JTextField fieldXt = new JTextField(7);
    private final PlotPanel grafik = new PlotPanel();

    // hasil HITUNG terakhir
    private Matrix titik;
    private double[] knot;
    private RegressionOutput output;
    private String namaMetode;
    private StringBuilder hasilPrediksi = new StringBuilder();

    public RegressionPage() {
        super("REGRESI SPLINE", "Isi titik sampel (x, y) maksimal 10 titik, lalu posisi knot (boleh 0 knot). " + "Jumlah titik minimal derajat + 1 + jumlah knot.", new MatrixInput(MatrixInput.Gaya.TITIK, 6, 2, 10, 2));
        ThemedUI.gayakan(pilihanDerajat);
        Theme.gayakan(fieldXt);
        PixelButton tombolPrediksi = new PixelButton("PREDIKSI", Theme.BIRU, Theme.BIRU_TUA);
        tombolPrediksi.addActionListener(e -> amanJalankan(this::prediksi));
        fieldXt.addActionListener(e -> amanJalankan(this::prediksi));

        tambahBarisOpsi(gridKnot);
        JLabel catatan = new JLabel("<html><body style='width:300px'>Knot harus terurut naik dan berada " + "di antara x terkecil dan x terbesar.</body></html>");
        catatan.setFont(Theme.BIASA);
        catatan.setForeground(Theme.TEKS);
        tambahBarisOpsi(catatan);
        tambahBarisOpsi(Theme.label("Derajat:"), pilihanDerajat);
        tambahBarisOpsi(Theme.label("xt ="), fieldXt, tombolPrediksi);
        tambahGrafik(grafik);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix dataTitik = grid.bacaMatriks();
        Matrix matriksKnot = gridKnot.bacaMatriks();
        double[] knotBaru = (matriksKnot == null) ? new double[0] : kolom(matriksKnot, 0);
        int derajat = 3 - pilihanDerajat.getSelectedIndex(); // urutan combo: 3, 2, 1

        RegressionOutput outputBaru = new SplineRegression().solve(dataTitik, knotBaru, derajat);

        titik = dataTitik;
        knot = knotBaru;
        output = outputBaru;
        namaMetode = (derajat == 3) ? "Regresi Spline Kubik" : "Regresi Spline Derajat " + derajat;
        hasilPrediksi = new StringBuilder();

        RegressionOutput hasil = output;
        grafik.setData(kolom(titik, 0), kolom(titik, 1), t -> hasil.prediksi(t), knot);
        tampilkanProses(output.getSteps(), susunTeks(), "KOEFISIEN DITEMUKAN", grafik::mulaiAnimasi);
    }

    private void prediksi() throws MatrixException {
        if (output == null) {
            throw new MatrixException("Tekan HITUNG dulu sebelum memprediksi.");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = output.prediksi(xt);

        String baris = "y^(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        hasilPrediksi.append("- ").append(baris).append("\n");
        pemutar.tambahCatatan("PREDIKSI", baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        perbaruiTeksLengkap(susunTeks());
        fieldXt.setText("");
    }

    private String susunTeks() {
        String posisiKnot;
        if (knot.length == 0) {
            posisiKnot = "tanpa knot";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < knot.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(NumberFormatter.teks(knot[i]));
            }
            posisiKnot = sb.toString();
        }

        // spesifikasi minta koefisien regresi ikut ditampilkan
        StringBuilder persamaan = new StringBuilder(output.getPersamaan());
        persamaan.append("\n\nKoefisien regresi:");
        double[] koef = output.getKoefisien();
        for (int i = 0; i < koef.length; i++) {
            persamaan.append("\n  b").append(i).append(" = ").append(NumberFormatter.teks(koef[i]));
        }
        return OutputWriter.teksRegresiSpline(namaMetode, titik, output.getSteps(), posisiKnot, persamaan.toString(), hasilPrediksi.toString());
    }

    @Override
    protected void reset() {
        super.reset();
        gridKnot.kosongkan();
        output = null;
        grafik.kosongkan();
    }
}
