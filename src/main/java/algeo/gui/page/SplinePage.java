package algeo.gui.page;

import javax.swing.JTextField;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.interpolasi.InterpolasiSolverOutput;
import algeo.core.interpolasi.NaturalCubicSpline;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PixelButton;
import algeo.gui.component.PlotPanel;
import algeo.gui.theme.Theme;
import algeo.io.EquationFormatter;
import algeo.io.OutputWriter;

/**
 * Halaman natural cubic spline beserta evaluasi S(xt) dan grafik.
 */
public class SplinePage extends FeaturePage {

    private static final String NAMA_METODE = "Natural Cubic Spline Interpolation";

    private final JTextField fieldXt = new JTextField(7);
    private final PlotPanel grafik = new PlotPanel();

    // hasil HITUNG terakhir
    private Matrix titik;
    private double[] x;
    private InterpolasiSolverOutput output;
    private String persamaanSegmen;
    private StringBuilder hasilEvaluasi = new StringBuilder();

    public SplinePage() {
        super("NATURAL CUBIC SPLINE", "Isi titik sampel (x, y) dengan x terurut naik, minimal 2 dan maksimal 10 titik. " + "Setelah HITUNG, masukkan xt lalu tekan EVALUASI untuk menghitung S(xt).", new MatrixInput(MatrixInput.Gaya.TITIK, 5, 2, 10, 2));
        Theme.gayakan(fieldXt);
        PixelButton tombolEvaluasi = new PixelButton("EVALUASI", Theme.BIRU, Theme.BIRU_TUA);
        tombolEvaluasi.addActionListener(e -> amanJalankan(this::evaluasi));
        fieldXt.addActionListener(e -> amanJalankan(this::evaluasi));
        tambahBarisOpsi(Theme.label("xt ="), fieldXt, tombolEvaluasi);
        tambahGrafik(grafik);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix dataTitik = grid.bacaMatriks();
        double[] xBaru = kolom(dataTitik, 0);
        double[] y = kolom(dataTitik, 1);

        InterpolasiSolverOutput outputBaru = NaturalCubicSpline.hitungSpline(xBaru, y);
        if (outputBaru.getResult().getType() != InterpolasiResult.ResultType.SUCCESS) {
            throw new MatrixException(outputBaru.getResult().getErrorMessage());
        }

        titik = dataTitik;
        x = xBaru;
        output = outputBaru;
        double[][] koef = output.getResult().getKoefisien();
        persamaanSegmen = EquationFormatter.splineSegmen(x, koef);
        hasilEvaluasi = new StringBuilder();

        double[] xGrafik = x;
        grafik.setData(x, y, t -> NaturalCubicSpline.evaluasi(xGrafik, koef, t), x);
        tampilkanProses(output.getSteps(), susunTeks(), "SPLINE DITEMUKAN", grafik::mulaiAnimasi);
    }

    private void evaluasi() throws MatrixException {
        if (output == null) {
            throw new MatrixException("Tekan HITUNG dulu sebelum mengevaluasi S(xt).");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = NaturalCubicSpline.evaluasi(x, output.getResult().getKoefisien(), xt);

        String baris = "S(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        if (!NaturalCubicSpline.diDalamDomain(x, xt)) {
            baris += "   (di luar domain, memakai segmen terdekat)";
        }
        hasilEvaluasi.append("- ").append(baris).append("\n");
        pemutar.tambahCatatan("EVALUASI", baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        perbaruiTeksLengkap(susunTeks());
        fieldXt.setText("");
    }

    private String susunTeks() {
        String domain = "[" + NumberFormatter.teks(x[0]) + ", " + NumberFormatter.teks(x[x.length - 1]) + "]";
        return OutputWriter.teksSplineKubik(NAMA_METODE, titik, output.getSteps(), output.getResult(), domain, persamaanSegmen, hasilEvaluasi.toString());
    }

    @Override
    protected void reset() {
        super.reset();
        output = null;
        grafik.kosongkan();
    }
}
