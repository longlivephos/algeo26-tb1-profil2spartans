package algeo.gui.page;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.regression.RegressionOutput;
import algeo.core.regression.SplineRegression;
import algeo.core.util.DecimalParser;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PlotPanel;
import algeo.io.OutputWriter;

public class RegressionPage extends FeaturePage {

    private JTextField fieldKnot = new JTextField(12);
    private JComboBox<String> pilihanDerajat = new JComboBox<>(new String[] {
        "3 (kubik)",
        "2 (kuadratik)",
        "1 (linear)"
    });
    private JTextField fieldXt = new JTextField(6);
    private PlotPanel grafik = new PlotPanel();

    private Matrix titik;
    private double[] knot;
    private RegressionOutput output;
    private String namaMetode;
    private String hasilPrediksi = "";

    public RegressionPage() {
        super(new MatrixInput("Jumlah titik:", 6, 10, new String[] {"x", "y"}));
        JButton tombolPrediksi = new JButton("Prediksi");
        tombolPrediksi.addActionListener(e -> {
            try {
                prediksi();
            } catch (MatrixException ex) {
                tampilkanError(ex.getMessage());
            }
        });
        tambahOpsi(new JLabel("Posisi knot:"));
        tambahOpsi(fieldKnot);
        tambahOpsi(new JLabel("Derajat:"), pilihanDerajat);
        tambahOpsi(new JLabel("xt ="), fieldXt, tombolPrediksi);
        tambahGrafik(grafik);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix dataTitik = input.bacaMatriks();
        double[] knotBaru = bacaKnot();
        int derajat = 3 - pilihanDerajat.getSelectedIndex();

        RegressionOutput outputBaru = new SplineRegression().solve(dataTitik, knotBaru, derajat);

        titik = dataTitik;
        knot = knotBaru;
        output = outputBaru;
        if (derajat == 3) {
            namaMetode = "Regresi Spline Kubik";
        } else {
            namaMetode = "Regresi Spline Derajat " + derajat;
        }
        hasilPrediksi = "";

        double[] x = ambilKolom(titik, 0);
        double[] y = ambilKolom(titik, 1);
        double[] xKurva = buatSampelX(cariMin(x), cariMax(x));
        double[] yKurva = new double[xKurva.length];
        for (int i = 0; i < xKurva.length; i++) {
            yKurva[i] = output.prediksi(xKurva[i]);
        }
        grafik.setData(x, y, xKurva, yKurva, knot);

        mulaiTampilkan(output.getSteps(), susunTeks());
    }

    // contohnya seperti "2 4,5" menjadi {2, 4.5}
    private double[] bacaKnot() throws MatrixException {
        String isi = fieldKnot.getText().trim();
        if (isi.isEmpty()) {
            return new double[0];
        }
        String[] token = isi.split("\\s+");
        double[] hasil = new double[token.length];
        for (int i = 0; i < token.length; i++) {
            try {
                hasil[i] = DecimalParser.parse(token[i]);
            } catch (NumberFormatException e) {
                throw new MatrixException("Knot ke-" + (i + 1) + ": " + e.getMessage());
            }
        }
        return hasil;
    }

    private void prediksi() throws MatrixException {
        if (output == null) {
            throw new MatrixException("Tekan Hitung dulu sebelum prediksi");
        }
        if (sedangJalan) {
            throw new MatrixException("Tunggu sampai langkah selesai ditampilkan");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = output.prediksi(xt);

        String baris = "y^(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        hasilPrediksi = hasilPrediksi + "- " + baris + "\n";
        tambahKeOutput(baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        teksLengkap = susunTeks();
        fieldXt.setText("");
    }

    private String susunTeks() {
        String posisiKnot = "";
        if (knot.length == 0) {
            posisiKnot = "tanpa knot";
        }
        for (int i = 0; i < knot.length; i++) {
            if (i > 0) posisiKnot = posisiKnot + ", ";
            posisiKnot = posisiKnot + NumberFormatter.teks(knot[i]);
        }

        String persamaan = output.getPersamaan() + output.getKeteranganNotasi() + "\n\nKoefisien regresi:";
        double[] koef = output.getKoefisien();
        for (int i = 0; i < koef.length; i++) {
            persamaan = persamaan + "\n  b" + i + " = " + NumberFormatter.teks(koef[i]);
        }
        return OutputWriter.teksRegresiSpline(namaMetode, titik, output.getSteps(), posisiKnot, persamaan, hasilPrediksi);
    }

    @Override
    protected void reset() {
        super.reset();
        fieldKnot.setText("");
        output = null;
        grafik.kosongkan();
    }
}
