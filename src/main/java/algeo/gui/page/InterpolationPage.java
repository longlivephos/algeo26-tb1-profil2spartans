package algeo.gui.page;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PlotPanel;
import algeo.interpolasipolinomial.Interpolasi;
import algeo.io.EquationFormatter;
import algeo.io.OutputWriter;

public class InterpolationPage extends FeaturePage {

    private JTextField fieldXt = new JTextField(6);
    private PlotPanel grafik = new PlotPanel();

    private Matrix titik;
    private List<String> langkah;
    private InterpolasiResult hasil;
    private double xMin, xMax;
    private String persamaan;
    private String hasilEvaluasi = "";

    public InterpolationPage() {
        super(new MatrixInput("Jumlah titik:", 4, 10, new String[] {"x", "y"}));
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
        double[] x = ambilKolom(dataTitik, 0);
        double[] y = ambilKolom(dataTitik, 1);

        List<String> langkahBaru = new ArrayList<>();
        InterpolasiResult hasilBaru = Interpolasi.hitungInterpolasi(x, y, langkahBaru);
        if (hasilBaru.getType() != InterpolasiResult.ResultType.SUCCESS) {
            throw new MatrixException(hasilBaru.getErrorMessage());
        }

        titik = dataTitik;
        langkah = langkahBaru;
        hasil = hasilBaru;
        xMin = cariMin(x);
        xMax = cariMax(x);
        persamaan = EquationFormatter.polinom(hasil.getKoefisien());
        hasilEvaluasi = "";

        // titik-titik kurva untuk grafik
        double[] xKurva = buatSampelX(xMin, xMax);
        double[] yKurva = new double[xKurva.length];
        for (int i = 0; i < xKurva.length; i++) {
            yKurva[i] = Interpolasi.taksirNilaiPolinomial(hasil.getKoefisien(), xKurva[i]);
        }
        grafik.setData(x, y, xKurva, yKurva, null);

        mulaiTampilkan(langkah, susunTeks());
    }

    private void evaluasi() throws MatrixException {
        if (hasil == null) {
            throw new MatrixException("Tekan Hitung dulu sebelum evaluasi");
        }
        if (sedangJalan) {
            throw new MatrixException("Tunggu sampai langkah selesai ditampilkan");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = Interpolasi.taksirNilaiPolinomial(hasil.getKoefisien(), xt);

        String baris = "P(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        if (xt < xMin || xt > xMax) {
            baris = baris + "  (di luar domain, ekstrapolasi)";
        }
        hasilEvaluasi = hasilEvaluasi + "- " + baris + "\n";
        tambahKeOutput(baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        teksLengkap = susunTeks();
        fieldXt.setText("");
    }

    private String susunTeks() {
        String domain = "[" + NumberFormatter.teks(xMin) + ", " + NumberFormatter.teks(xMax) + "]";
        return OutputWriter.teksInterpolasi("Interpolasi Polinomial", titik, langkah, hasil, domain, persamaan, hasilEvaluasi);
    }

    @Override
    protected void reset() {
        super.reset();
        hasil = null;
        grafik.kosongkan();
    }
}
