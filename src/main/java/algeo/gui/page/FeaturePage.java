package algeo.gui.page;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.DecimalParser;
import algeo.gui.component.LangkahPanel;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.PlotPanel;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

public abstract class FeaturePage extends JPanel {

    private static final int JEDA = 500; // jeda antar langkah komputasinya (ms)

    protected MatrixInput input;

    private JPanel panelOpsi = new JPanel();
    private JPanel panelKanan = new JPanel(new BorderLayout(0, 5));
    private JPanel panelLangkah = new JPanel();
    private JScrollPane scrollLangkah;
    private JLabel labelIndikator = new JLabel(" ");
    private JButton tombolHitung = new JButton("Hitung");

    private Timer timer;
    private List<String> langkah = new ArrayList<>();
    private int indeks = 0;
    private int jumlahTukar = 0;
    private int jumlahOperasi = 0;
    private String teksHasil = "";
    private double[][] matriksSebelum = null;
    protected boolean sedangJalan = false;

    protected String teksLengkap = "";

    public FeaturePage(MatrixInput input) {
        super(new BorderLayout(10, 10));
        this.input = input;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton tombolBuka = new JButton("Buka File");
        JButton tombolSimpan = new JButton("Simpan");
        JButton tombolReset = new JButton("Reset");

        tombolBuka.addActionListener(e -> {
            try {
                bukaFile();
            } catch (MatrixException ex) {
                tampilkanError(ex.getMessage());
            }
        });
        tombolHitung.addActionListener(e -> {
            try {
                hitung();
            } catch (MatrixException ex) {
                tampilkanError(ex.getMessage());
            } catch (Exception ex) {
                tampilkanError("Terjadi kesalahan: " + ex.getMessage());
            }
        });
        tombolSimpan.addActionListener(e -> {
            try {
                simpanHasil();
            } catch (IOException ex) {
                tampilkanError("Gagal menyimpan file: " + ex.getMessage());
            }
        });
        tombolReset.addActionListener(e -> reset());

        JPanel panelTombol = new JPanel(new GridLayout(1, 4, 5, 5));
        panelTombol.add(tombolBuka);
        panelTombol.add(tombolHitung);
        panelTombol.add(tombolSimpan);
        panelTombol.add(tombolReset);

        panelOpsi.setLayout(new BoxLayout(panelOpsi, BoxLayout.Y_AXIS));

        JPanel isiKiri = new JPanel(new BorderLayout(0, 10));
        isiKiri.add(input, BorderLayout.NORTH);
        isiKiri.add(panelOpsi, BorderLayout.CENTER);

        JPanel panelKiri = new JPanel(new BorderLayout(0, 10));
        panelKiri.setBorder(BorderFactory.createTitledBorder("Input"));
        panelKiri.add(new JScrollPane(isiKiri), BorderLayout.CENTER);
        panelKiri.add(panelTombol, BorderLayout.SOUTH);

        panelLangkah.setLayout(new BoxLayout(panelLangkah, BoxLayout.Y_AXIS));
        scrollLangkah = new JScrollPane(panelLangkah);
        scrollLangkah.getVerticalScrollBar().setUnitIncrement(16);
        kosongkanLangkah();

        JPanel panelWarna = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panelWarna.add(kotakWarna("Baris diubah", LangkahPanel.KUNING));
        panelWarna.add(kotakWarna("Nilai yang berubah", LangkahPanel.ORANYE));
        panelWarna.add(kotakWarna("Baris acuan", LangkahPanel.BIRU));
        panelWarna.add(kotakWarna("Baris ditukar", LangkahPanel.UNGU));

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.add(labelIndikator, BorderLayout.NORTH);
        panelAtas.add(panelWarna, BorderLayout.SOUTH);

        panelKanan.setBorder(BorderFactory.createTitledBorder("Langkah dan Hasil"));
        panelKanan.add(panelAtas, BorderLayout.NORTH);
        panelKanan.add(scrollLangkah, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelKiri, panelKanan);
        split.setDividerLocation(450);
        add(split, BorderLayout.CENTER);

        timer = new Timer(JEDA, e -> tampilkanLangkahBerikut());
    }

    protected abstract void hitung() throws MatrixException;
    protected void tambahOpsi(JComponent... komponen) {
        JPanel baris = new JPanel();
        for (JComponent k : komponen) {
            baris.add(k);
        }
        panelOpsi.add(baris);
    }

    // nampilin grafik untuk interpolasi, spline, dan regresi
    protected void tambahGrafik(PlotPanel grafik) {
        panelKanan.remove(scrollLangkah);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grafik, scrollLangkah);
        split.setResizeWeight(0.5);
        panelKanan.add(split, BorderLayout.CENTER);
    }

    protected void mulaiTampilkan(List<String> daftarLangkah, String teksLengkap) {
        timer.stop();
        this.langkah = daftarLangkah;
        this.teksLengkap = teksLengkap;
        this.teksHasil = ambilBagianHasil(teksLengkap);
        indeks = 0;
        jumlahTukar = 0;
        jumlahOperasi = 0;
        sedangJalan = true;
        tombolHitung.setEnabled(false);

        matriksSebelum = null;
        panelLangkah.removeAll();
        perbaruiIndikator();
        timer.start();
    }

    private void tampilkanLangkahBerikut() {
        if (indeks < langkah.size()) {
            String s = langkah.get(indeks);
            String judul = "Langkah " + (indeks + 1);
            if (s.startsWith("Tukar baris")) {
                jumlahTukar++;
                judul = judul + " Tukar Baris";
            } else if (s.startsWith("Baris ") && s.contains(" = Baris ")) {
                jumlahOperasi++;
                judul = judul + " Operasi Baris";
            }
            LangkahPanel panel = new LangkahPanel(judul, s, matriksSebelum, new Color(255, 245, 250));
            if (panel.getMatriks() != null) {
                matriksSebelum = panel.getMatriks();
            }
            tambahPanel(panel);
            indeks++;
            perbaruiIndikator();
        } else {
            timer.stop();
            sedangJalan = false;
            tombolHitung.setEnabled(true);
            String hasil = teksHasil;
            if (hasil.length() > 20000) {
                hasil = hasil.substring(0, 20000) + "\n... (hasil terlalu panjang, cobalah simpan ke file untuk melihat semuanya)";
            }
            tambahPanel(new LangkahPanel("HASIL", hasil, null, new Color(255, 250, 215)));
        }
    }

    private void tambahPanel(JPanel panel) {
        panelLangkah.add(panel);
        panelLangkah.add(Box.createVerticalStrut(6));
        panelLangkah.revalidate();
        panelLangkah.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollLangkah.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private static JLabel kotakWarna(String teks, Color warna) {
        JLabel label = new JLabel(teks);
        label.setOpaque(true);
        label.setBackground(warna);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        return label;
    }
    private void kosongkanLangkah() {
        panelLangkah.removeAll();
        // panelLangkah.add(new JLabel("Isi input lalu tekan Hitung"));
        panelLangkah.revalidate();
        panelLangkah.repaint();
    }

    private void perbaruiIndikator() {
        labelIndikator.setText("<html>Langkah " + indeks + "/" + langkah.size() + "<br>Tukar baris: " + jumlahTukar + "<br>Operasi baris: " + jumlahOperasi + "</html>");
    }

    protected void tambahKeOutput(String baris) {
        tambahPanel(new LangkahPanel("Evaluasi", baris, null, new Color(215, 240, 215)));
    }

    protected void reset() {
        timer.stop();
        sedangJalan = false;
        tombolHitung.setEnabled(true);
        input.kosongkan();
        kosongkanLangkah();
        matriksSebelum = null;
        langkah = new ArrayList<>();
        indeks = 0;
        jumlahTukar = 0;
        jumlahOperasi = 0;
        teksLengkap = "";
        labelIndikator.setText(" ");
    }

    protected void tampilkanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    // membaca satu angka dari text field
    protected static double bacaAngka(JTextField field, String nama) throws MatrixException {
        String isi = field.getText().trim();
        if (isi.isEmpty()) {
            throw new MatrixException("Nilai " + nama + " belum diisi");
        }
        try {
            return DecimalParser.parse(isi);
        } catch (NumberFormatException e) {
            throw new MatrixException(nama + ": " + e.getMessage());
        }
    }

    protected static double[] ambilKolom(Matrix m, int j) {
        double[] hasil = new double[m.getRows()];
        for (int i = 0; i < m.getRows(); i++) {
            hasil[i] = m.get(i, j);
        }
        return hasil;
    }

    protected static double cariMin(double[] a) {
        double hasil = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] < hasil) hasil = a[i];
        }
        return hasil;
    }

    protected static double cariMax(double[] a) {
        double hasil = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > hasil) hasil = a[i];
        }
        return hasil;
    }

    protected static double[] buatSampelX(double a, double b) {
        int n = 200;
        double[] x = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            x[i] = a + (b - a) * i / n;
        }
        return x;
    }

    private static String ambilBagianHasil(String teks) {
        int posisi = teks.lastIndexOf("\nHasil:\n");
        if (posisi >= 0) {
            return teks.substring(posisi + "\nHasil:\n".length());
        }
        return teks;
    }

    private void bukaFile() throws MatrixException {
        JFileChooser chooser = new JFileChooser(new File("test"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Matrix m = MatrixReader.bacaFile(chooser.getSelectedFile().getPath());
        input.muatMatriks(m);
    }

    private void simpanHasil() throws IOException {
        if (teksLengkap.isEmpty()) {
            tampilkanError("Tekan Hitung dulu sebelum menyimpan");
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = chooser.getSelectedFile().getPath();
        if (!path.toLowerCase().endsWith(".txt")) {
            path = path + ".txt";
        }
        OutputWriter.simpan(path, teksLengkap);
        JOptionPane.showMessageDialog(this, "Hasil disimpan ke " + path);
    }
}
