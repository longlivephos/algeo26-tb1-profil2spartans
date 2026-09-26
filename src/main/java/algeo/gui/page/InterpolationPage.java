package algeo.gui.page;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import algeo.core.interpolasi.InterpolasiResult;
import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.NumberFormatter;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PixelButton;
import algeo.gui.component.PlotPanel;
import algeo.gui.theme.Theme;
import algeo.interpolasipolinomial.Interpolasi;
import algeo.io.EquationFormatter;
import algeo.io.OutputWriter;

/**
 * Halaman interpolasi polinomial beserta evaluasi P(xt) dan grafik.
 */
public class InterpolationPage extends FeaturePage {

    private static final String NAMA_METODE = "Interpolasi Polinomial";

    private final JTextField fieldXt = new JTextField(7);
    private final PlotPanel grafik = new PlotPanel();

    // hasil HITUNG terakhir, dipakai lagi saat evaluasi xt
    private Matrix titik;
    private List<String> langkah;
    private InterpolasiResult hasil;
    private double xMin, xMax;
    private String persamaan;
    private StringBuilder hasilEvaluasi = new StringBuilder();

    public InterpolationPage() {
        super("INTERPOLASI POLINOMIAL", "Isi titik sampel (x, y), maksimal 10 titik. Setelah HITUNG, " + "masukkan nilai xt lalu tekan EVALUASI untuk menaksir P(xt).", new MatrixInput(MatrixInput.Gaya.TITIK, 4, 2, 10, 2));
        Theme.gayakan(fieldXt);
        PixelButton tombolEvaluasi = new PixelButton("EVALUASI", Theme.BIRU, Theme.BIRU_TUA);
        tombolEvaluasi.addActionListener(e -> amanJalankan(this::evaluasi));
        fieldXt.addActionListener(e -> amanJalankan(this::evaluasi)); // tekan Enter
        tambahBarisOpsi(Theme.label("xt ="), fieldXt, tombolEvaluasi);
        tambahGrafik(grafik);
    }

    @Override
    protected void hitung() throws MatrixException {
        Matrix dataTitik = grid.bacaMatriks();
        double[] x = kolom(dataTitik, 0);
        double[] y = kolom(dataTitik, 1);

        List<String> langkahBaru = new ArrayList<>();
        InterpolasiResult hasilBaru = Interpolasi.hitungInterpolasi(x, y, langkahBaru);
        if (hasilBaru.getType() != InterpolasiResult.ResultType.SUCCESS) {
            throw new MatrixException(hasilBaru.getErrorMessage());
        }

        titik = dataTitik;
        langkah = langkahBaru;
        hasil = hasilBaru;
        xMin = min(x);
        xMax = max(x);
        persamaan = EquationFormatter.polinom(hasil.getKoefisien());
        hasilEvaluasi = new StringBuilder();

        double[][] koef = hasil.getKoefisien();
        grafik.setData(x, y, t -> Interpolasi.taksirNilaiPolinomial(koef, t), null);
        tampilkanProses(langkah, susunTeks(), "POLINOM DITEMUKAN", grafik::mulaiAnimasi);
    }

    private void evaluasi() throws MatrixException {
        if (hasil == null) {
            throw new MatrixException("Tekan HITUNG dulu sebelum mengevaluasi P(xt).");
        }
        double xt = bacaAngka(fieldXt, "xt");
        double yt = Interpolasi.taksirNilaiPolinomial(hasil.getKoefisien(), xt);

        String baris = "P(" + NumberFormatter.teks(xt) + ") = " + NumberFormatter.teks(yt);
        if (xt < xMin || xt > xMax) {
            baris += "   (di luar domain, hasil ekstrapolasi)";
        }
        hasilEvaluasi.append("- ").append(baris).append("\n");
        pemutar.tambahCatatan("EVALUASI", baris);
        grafik.tambahTitikEvaluasi(xt, yt);
        perbaruiTeksLengkap(susunTeks());
        fieldXt.setText("");
    }

    private String susunTeks() {
        String domain = "[" + NumberFormatter.teks(xMin) + ", " + NumberFormatter.teks(xMax) + "]";
        return OutputWriter.teksInterpolasi(NAMA_METODE, titik, langkah, hasil, domain, persamaan, hasilEvaluasi.toString());
    }

    @Override
    protected void reset() {
        super.reset();
        hasil = null;
        grafik.kosongkan();
    }
}
