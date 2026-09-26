package algeo.gui.page;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.DecimalParser;
import algeo.gui.component.CardPanel;
import algeo.gui.component.MatrixInput;
import algeo.gui.component.MessageDialog;
import algeo.gui.component.PixelButton;
import algeo.gui.component.PlotPanel;
import algeo.gui.component.StepPlayer;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.io.MatrixReader;
import algeo.io.OutputWriter;

/**
 * Kerangka halaman fitur: panel input di kiri, langkah dan hasil di kanan. Subclass cukup mengimplementasikan hitung().
 */
public abstract class FeaturePage extends JPanel {

    /** Aksi tombol yang boleh melempar exception, supaya error ditangani di satu tempat. */
    protected interface Aksi {
        void jalankan() throws Exception;
    }

    private static final int LEBAR_KIRI = 500;

    protected final MatrixInput grid;
    protected final StepPlayer pemutar = new StepPlayer();

    private final JPanel panelOpsi = new JPanel();
    private final JPanel kanan = new JPanel(new BorderLayout());
    private final PixelButton tombolKembali = new PixelButton("< MENU", Theme.KUNING, Theme.KUNING_TUA);

    // teks lengkap (metode, input, langkah, hasil) untuk disimpan ke .txt
    private String teksLengkap = "";

    public FeaturePage(String judul, String petunjuk, MatrixInput grid) {
        super(new BorderLayout(14, 14));
        this.grid = grid;
        setBackground(Theme.LATAR);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        tombolKembali.setForeground(Theme.TEKS);
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        atas.setOpaque(false);
        atas.add(tombolKembali);
        atas.add(Theme.judul(judul, 20f));

        JLabel labelPetunjuk = new JLabel("<html><body style='width:" + (LEBAR_KIRI - 170) + "px'>" + petunjuk + "</body></html>");
        labelPetunjuk.setFont(Theme.BIASA);
        labelPetunjuk.setForeground(Theme.TEKS);

        panelOpsi.setLayout(new BoxLayout(panelOpsi, BoxLayout.Y_AXIS));
        panelOpsi.setOpaque(false);

        // petunjuk, input, dan opsi ikut digulir; tombol tetap di bawah
        JPanel isiKiri = new JPanel();
        isiKiri.setLayout(new BoxLayout(isiKiri, BoxLayout.Y_AXIS));
        isiKiri.setOpaque(false);
        tambahRataKiri(isiKiri, labelPetunjuk);
        isiKiri.add(Box.createVerticalStrut(10));
        tambahRataKiri(isiKiri, grid);
        isiKiri.add(Box.createVerticalStrut(10));
        tambahRataKiri(isiKiri, panelOpsi);

        JPanel wadahIsi = new JPanel(new BorderLayout());
        wadahIsi.setOpaque(false);
        wadahIsi.add(isiKiri, BorderLayout.NORTH);
        JScrollPane scrollKiri = new JScrollPane(wadahIsi);
        scrollKiri.setOpaque(false);
        scrollKiri.getViewport().setOpaque(false);
        scrollKiri.setBorder(null);
        ThemedUI.gayakan(scrollKiri);

        PixelButton tombolBuka = new PixelButton("BUKA FILE");
        PixelButton tombolHitung = new PixelButton("HITUNG", Theme.KUNING, Theme.KUNING_TUA);
        PixelButton tombolSimpan = new PixelButton("SIMPAN");
        PixelButton tombolReset = new PixelButton("RESET");
        tombolHitung.setForeground(Theme.TEKS);
        tombolBuka.setToolTipText("Baca input dari file .txt");
        tombolSimpan.setToolTipText("Simpan metode, input, langkah, dan hasil ke file .txt");
        tombolBuka.addActionListener(e -> amanJalankan(this::bukaFile));
        tombolHitung.addActionListener(e -> amanJalankan(this::hitung));
        tombolSimpan.addActionListener(e -> amanJalankan(this::simpanHasil));
        tombolReset.addActionListener(e -> reset());

        JPanel barisTombol = new JPanel(new GridLayout(2, 2, 8, 8));
        barisTombol.setOpaque(false);
        barisTombol.add(tombolBuka);
        barisTombol.add(tombolHitung);
        barisTombol.add(tombolSimpan);
        barisTombol.add(tombolReset);

        CardPanel kartuInput = new CardPanel("INPUT", new BorderLayout(0, 12));
        kartuInput.add(scrollKiri, BorderLayout.CENTER);
        kartuInput.add(barisTombol, BorderLayout.SOUTH);
        kartuInput.setPreferredSize(new Dimension(LEBAR_KIRI, 0));

        kanan.setOpaque(false);
        kanan.add(pemutar, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(kartuInput, BorderLayout.WEST);
        add(kanan, BorderLayout.CENTER);
    }

    /** Diisi setiap halaman: baca input, panggil library, lalu tampilkanProses(...). */
    protected abstract void hitung() throws MatrixException;

    public void setAksiKembali(Runnable aksi) {
        tombolKembali.addActionListener(e -> aksi.run());
    }

    /** Menambah satu baris komponen di bawah input (misal pilihan metode). */
    protected void tambahBarisOpsi(JComponent... isi) {
        JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        baris.setOpaque(false);
        for (JComponent c : isi) {
            baris.add(c);
        }
        tambahRataKiri(panelOpsi, baris);
    }

    /** Area kanan dibagi dua: grafik di atas, langkah di bawah. */
    protected void tambahGrafik(PlotPanel grafik) {
        kanan.remove(pemutar);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grafik, pemutar);
        split.setUI(new BasicSplitPaneUI()); // pembatas polos tanpa titik-titik bawaan
        ((BasicSplitPaneUI) split.getUI()).getDivider().setBorder(null);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(12);
        kanan.add(split, BorderLayout.CENTER);
    }

    /**
     * Menjalankan animasi langkah lalu menampilkan hasil.
     * @param langkah langkah dari library
     * @param teksLengkap teks dari OutputWriter (juga yang disimpan ke file)
     * @param status ringkasan singkat untuk badge hasil, boleh kosong
     * @param saatSelesai dijalankan setelah hasil muncul, boleh null
     */
    protected void tampilkanProses(List<String> langkah, String teksLengkap, String status, Runnable saatSelesai) {
        this.teksLengkap = teksLengkap;
        pemutar.putar(langkah, ambilBagianHasil(teksLengkap), status, saatSelesai);
    }

    /** Dipakai setelah evaluasi xt supaya file yang disimpan ikut memuat hasil evaluasi. */
    protected void perbaruiTeksLengkap(String teksLengkap) {
        this.teksLengkap = teksLengkap;
    }

    /** Membaca satu angka dari text field (titik atau koma sebagai desimal). */
    protected static double bacaAngka(JTextField field, String nama) throws MatrixException {
        String isi = field.getText().trim();
        if (isi.isEmpty()) {
            throw new MatrixException("Isi nilai " + nama + " dulu.");
        }
        try {
            return DecimalParser.parse(isi);
        } catch (NumberFormatException e) {
            throw new MatrixException(nama + ": " + e.getMessage());
        }
    }

    protected void reset() {
        grid.kosongkan();
        pemutar.kosongkan();
        teksLengkap = "";
    }

    /** Menjalankan aksi tombol; semua exception ditampilkan sebagai dialog, program tidak crash. */
    protected void amanJalankan(Aksi aksi) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            aksi.jalankan();
        } catch (MatrixException | NumberFormatException e) {
            MessageDialog.tampilkan(this, "KESALAHAN", e.getMessage());
        } catch (IOException e) {
            MessageDialog.tampilkan(this, "KESALAHAN", "Gagal membaca atau menyimpan file: " + e.getMessage());
        } catch (Exception e) {
            MessageDialog.tampilkan(this, "KESALAHAN", "Terjadi kesalahan tak terduga: " + e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected static double[] kolom(Matrix m, int j) {
        double[] hasil = new double[m.getRows()];
        for (int i = 0; i < m.getRows(); i++) {
            hasil[i] = m.get(i, j);
        }
        return hasil;
    }

    protected static double min(double[] a) {
        double hasil = a[0];
        for (double v : a) hasil = Math.min(hasil, v);
        return hasil;
    }

    protected static double max(double[] a) {
        double hasil = a[0];
        for (double v : a) hasil = Math.max(hasil, v);
        return hasil;
    }
    
    private static void tambahRataKiri(JPanel wadah, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        wadah.add(c);
    }

    private static String ambilBagianHasil(String teks) {
        int posisi = teks.lastIndexOf("\nHasil:\n");
        return (posisi >= 0) ? teks.substring(posisi + "\nHasil:\n".length()) : teks;
    }

    private void bukaFile() throws MatrixException {
        File folderAwal = new File("test").isDirectory() ? new File("test") : new File(".");
        JFileChooser chooser = new JFileChooser(folderAwal);
        chooser.setFileFilter(new FileNameExtensionFilter("File teks (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        Matrix m = MatrixReader.bacaFile(file.getPath());
        grid.muatMatriks(m, file.getName());
    }

    private void simpanHasil() throws IOException {
        if (teksLengkap.isEmpty()) {
            MessageDialog.tampilkan(this, "BELUM ADA HASIL", "Tekan HITUNG dulu sebelum menyimpan.");
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("File teks (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = chooser.getSelectedFile().getPath();
        if (!path.toLowerCase().endsWith(".txt")) {
            path = path + ".txt";
        }
        OutputWriter.simpan(path, teksLengkap);
        MessageDialog.tampilkan(this, "TERSIMPAN", "Hasil disimpan ke:\n" + path);
    }
}
