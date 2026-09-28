package algeo.gui.page;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.interpolasi.InterpolasiSolverOutput;
import algeo.core.interpolasi.NaturalCubicSpline;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PlotPanel;
import algeo.io.EquationFormatter;
import algeo.io.OutputWriter;

public class SplinePage extends FeaturePage {

    private JTextField fieldXt = new JTextField(6);
    private PlotPanel grafik = new PlotPanel();

    private Matrix titik;
    private double[] x;
    private InterpolasiSolverOutput output;
    private String persamaanSegmen;
    private String hasilEvaluasi = "";

    public SplinePage() {
        super(new MatrixInput("Jumlah titik:", 5, 10, new String[] {"x", "y"}));
        JButton tombolEvaluasi = new JButton("Evaluasi");
        tombolEvaluasi.addActionListener(e -> {
            try {
                evaluasi();
            } catch (MatrixException ex) {
                tampilkanError(ex.getMessage());
            }
        });
        tambahOpsi(new JLabel("xt ="), fieldXt, tombolEvaluasi);
        tambahGrafik(grafik);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix dataTitik = input.bacaMatriks();
        double[] xBaru = ambilKolom(dataTitik, 0);
        double[] y = ambilKolom(dataTitik, 1);

        InterpolasiSolverOutput outputBaru = NaturalCubicSpline.hitungSpline(xBaru, y);
        if (outputBaru.getResult().getType() != InterpolasiResult.ResultType.SUCCESS) {
            throw new MatrixException(outputBaru.getResult().getErrorMessage());
        }

        titik = dataTitik;
        x = xBaru;
        output = outputBaru;
        double[][] koef = output.getResult().getKoefisien();
        persamaanSegmen = EquationFormatter.splineSegmen(x, koef);
        hasilEvaluasi = "";

        double[] xKurva = buatSampelX(x[0], x[x.length - 1]);
        double[] yKurva = new double[xKurva.length];
        for (int i = 0; i < xKurva.length; i++) {
            yKurva[i] = NaturalCubicSpline.evaluasi(x, koef, xKurva[i]);
        }
        grafik.setData(x, y, xKurva, yKurva, x);

        mulaiTampilkan(output.getSteps(), susunTeks());
    }

    private void evaluasi() throws MatrixException {
        if (output == null) {
            throw new MatrixException("Tekan Hitung dulu sebelum evaluasi");
        }
        if (sedangJalan) {
            throw new MatrixException("Tunggu sampai langkah selesai ditampilkan");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = NaturalCubicSpline.evaluasi(x, output.getResult().getKoefisien(), xt);

        String baris = "S(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        if (!NaturalCubicSpline.diDalamDomain(x, xt)) {
            baris = baris + "  (di luar domain, memakai segmen terdekat)";
        }
        hasilEvaluasi = hasilEvaluasi + "- " + baris + "\n";
        tambahKeOutput(baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        teksLengkap = susunTeks();
        fieldXt.setText("");
    }

    private String susunTeks() {
        String domain = "[" + NumberFormatter.teks(x[0]) + ", " + NumberFormatter.teks(x[x.length - 1]) + "]";
        return OutputWriter.teksSplineKubik("Natural Cubic Spline Interpolation", titik, output.getSteps(), output.getResult(), domain, persamaanSegmen, hasilEvaluasi);
    }

    @Override
    protected void reset() {
        super.reset();
        output = null;
        grafik.kosongkan();
    }
}
