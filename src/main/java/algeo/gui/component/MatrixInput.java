package algeo.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.DecimalParser;
import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;
import algeo.io.MatrixReader;

/**
 * Komponen input matriks dengan dua mode: per angka (satu kotak per nilai) dan teks (format sama dengan file .txt).
 */
public class MatrixInput extends JPanel {

    public enum Gaya { PERSAMAAN, MATRIKS, TITIK, KNOT }

    private final Gaya gaya;
    private final int maksBaris;
    private final int maksKolom;
    private final Stepper hitungBaris;
    private final Stepper hitungKolom;
    private final JPanel isi = new JPanel(new BorderLayout());
    private final JPanel kiriSel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JScrollPane scrollTeks;
    private final JPanel panelFile = new JPanel(new BorderLayout(0, 8));
    private final JPanel panelSel = new JPanel(new GridBagLayout());
    private final JPanel barisUkuran = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JTextArea areaTeks = new JTextArea(8, 20);
    private final JLabel labelFile = Theme.label("");
    private final JLabel labelStatus = new JLabel();
    private final PixelButton tabAngka = new PixelButton("PER ANGKA");
    private final PixelButton tabTeks = new PixelButton("TEKS");

    private JTextField[][] sel = new JTextField[0][0];
    private boolean modeTeks = false;
    private Matrix matriksFile = null; // file yang terlalu besar untuk ditampilkan

    /**
     * @param baris jumlah baris awal
     * @param kolom jumlah kolom awal (untuk PERSAMAAN = jumlah variabel)
     * @param maksBaris batas input manual
     * @param maksKolom batas input manual
     */
    public MatrixInput(Gaya gaya, int baris, int kolom, int maksBaris, int maksKolom) {
        super(new BorderLayout(0, 8));
        this.gaya = gaya;
        this.maksBaris = maksBaris;
        this.maksKolom = maksKolom;
        setOpaque(false);

        hitungBaris = new Stepper(baris, gaya == Gaya.KNOT ? 0 : 1, maksBaris);
        hitungKolom = new Stepper(kolom, 1, maksKolom);
        hitungBaris.setSaatBerubah(this::bangunUlang);
        hitungKolom.setSaatBerubah(this::bangunUlang);
        barisUkuran.setOpaque(false);
        switch (gaya) {
            case PERSAMAAN:
                barisUkuran.add(Theme.label("Persamaan"));
                barisUkuran.add(hitungBaris);
                barisUkuran.add(Theme.label("Variabel"));
                barisUkuran.add(hitungKolom);
                break;
            case MATRIKS:
                barisUkuran.add(Theme.label("Baris"));
                barisUkuran.add(hitungBaris);
                barisUkuran.add(Theme.label("Kolom"));
                barisUkuran.add(hitungKolom);
                break;
            case TITIK:
                barisUkuran.add(Theme.label("Jumlah titik"));
                barisUkuran.add(hitungBaris);
                break;
            default:
                barisUkuran.add(Theme.label("Jumlah knot"));
                barisUkuran.add(hitungBaris);
                break;
        }

        JPanel atas = new JPanel(new BorderLayout(0, 8));
        atas.setOpaque(false);
        if (gaya != Gaya.KNOT) {
            tabAngka.setFont(Theme.pixel(8f));
            tabTeks.setFont(Theme.pixel(8f));
            tabAngka.addActionListener(e -> gantiMode(false));
            tabTeks.addActionListener(e -> gantiMode(true));
            JPanel barisTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            barisTab.setOpaque(false);
            barisTab.add(Theme.label("Mode input:"));
            barisTab.add(tabAngka);
            barisTab.add(tabTeks);
            atas.add(barisTab, BorderLayout.NORTH);
        }
        atas.add(barisUkuran, BorderLayout.SOUTH);

        panelSel.setOpaque(false);
        kiriSel.setOpaque(false);
        kiriSel.add(panelSel);

        areaTeks.setFont(Theme.ANGKA);
        areaTeks.setForeground(Theme.TEKS);
        areaTeks.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        areaTeks.getDocument().addDocumentListener(new PendengarUbah(this::perbaruiStatus));
        scrollTeks = new JScrollPane(areaTeks);
        scrollTeks.setBorder(BorderFactory.createLineBorder(Theme.PINK_GARIS, 2));
        ThemedUI.gayakan(scrollTeks);

        PixelButton tombolManual = new PixelButton("KEMBALI KE MANUAL", Theme.BIRU, Theme.BIRU_TUA);
        tombolManual.setFont(Theme.pixel(8f));
        tombolManual.addActionListener(e -> pakaiInputManual());
        JPanel wadahTombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wadahTombol.setOpaque(false);
        wadahTombol.add(tombolManual);
        panelFile.setOpaque(false);
        panelFile.add(labelFile, BorderLayout.NORTH);
        panelFile.add(wadahTombol, BorderLayout.CENTER);

        isi.setOpaque(false);

        labelStatus.setFont(Theme.BIASA);
        labelStatus.setForeground(Theme.PINK_GARIS);

        add(atas, BorderLayout.NORTH);
        add(isi, BorderLayout.CENTER);
        add(labelStatus, BorderLayout.SOUTH);
        bangunUlang();
        gantiMode(false);
    }

    /** Membaca input (per angka, teks, atau file) menjadi Matrix. */
    public Matrix bacaMatriks() throws MatrixException {
        if (matriksFile != null) {
            return matriksFile;
        }
        if (modeTeks) {
            Matrix m = MatrixReader.parseTeks(areaTeks.getText());
            periksaUkuran(m, false);
            return m;
        }
        if (sel.length == 0) {
            return null; // hanya mungkin untuk KNOT dengan jumlah knot 0
        }
        double[][] data = new double[sel.length][sel[0].length];
        for (int i = 0; i < sel.length; i++) {
            for (int j = 0; j < sel[i].length; j++) {
                JTextField f = sel[i][j];
                String teks = f.getText().trim();
                if (teks.isEmpty()) {
                    tandaiSalah(f);
                    throw new MatrixException("Kotak " + namaSel(i, j) + " masih kosong.");
                }
                try {
                    data[i][j] = DecimalParser.parse(teks);
                } catch (NumberFormatException e) {
                    tandaiSalah(f);
                    throw new MatrixException("Kotak " + namaSel(i, j) + ": " + e.getMessage());
                }
            }
        }
        return new Matrix(data);
    }

    /* Mengisi input dari matriks hasil baca file. Kalau melebihi batas input manual, matriks dipakai langsung tanpa ditampilkan. */
    public void muatMatriks(Matrix m, String namaFile) throws MatrixException {
        if (!periksaUkuran(m, true)) {
            matriksFile = m;
            labelFile.setText("<html><body style='width:280px'>File <b>" + namaFile + "</b> berukuran "
                    + m.getRows() + " x " + m.getCols()
                    + ", terlalu besar untuk ditampilkan, jadi dipakai langsung.</body></html>");
            pasangIsi(panelFile);
            barisUkuran.setVisible(false);
            perbaruiStatus();
            return;
        }

        matriksFile = null;
        if (modeTeks) {
            areaTeks.setText(keTeks(m));
        } else {
            hitungBaris.setNilai(m.getRows());
            if (gaya == Gaya.PERSAMAAN) hitungKolom.setNilai(m.getCols() - 1);
            if (gaya == Gaya.MATRIKS) hitungKolom.setNilai(m.getCols());
            bangunUlang();
            for (int i = 0; i < m.getRows(); i++) {
                for (int j = 0; j < m.getCols(); j++) {
                    sel[i][j].setText(teksAngka(m.get(i, j)));
                }
            }
        }
        gantiMode(modeTeks);
    }

    public void kosongkan() {
        matriksFile = null;
        areaTeks.setText("");
        for (JTextField[] baris : sel) {
            for (JTextField f : baris) f.setText("");
        }
        gantiMode(modeTeks);
    }

    private void gantiMode(boolean keTeks) {
        if (matriksFile == null && keTeks != modeTeks) {
            if (keTeks) {
                areaTeks.setText(gridKeTeks());
            } else if (!areaTeks.getText().trim().isEmpty()) {
                // isi teks dipindah ke kotak-kotak; kalau formatnya salah, tetap di mode teks
                try {
                    Matrix m = MatrixReader.parseTeks(areaTeks.getText());
                    if (!periksaUkuran(m, true)) {
                        throw new MatrixException("Ukuran teks melebihi batas input per angka.");
                    }
                    modeTeks = false;
                    muatMatriks(m, "");
                } catch (MatrixException e) {
                    MessageDialog.tampilkan(this, "KESALAHAN", "Teks belum bisa dipindah ke mode per angka. " + e.getMessage());
                    return;
                }
            }
        }
        modeTeks = keTeks;
        if (matriksFile == null) {
            pasangIsi(modeTeks ? scrollTeks : kiriSel);
            barisUkuran.setVisible(!modeTeks);
        }
        // tab yang aktif berwarna kuning
        tabAngka.setWarna(modeTeks ? Theme.PINK : Theme.KUNING, modeTeks ? Theme.PINK_TUA : Theme.KUNING_TUA);
        tabAngka.setForeground(modeTeks ? Color.WHITE : Theme.TEKS);
        tabTeks.setWarna(modeTeks ? Theme.KUNING : Theme.PINK, modeTeks ? Theme.KUNING_TUA : Theme.PINK_TUA);
        tabTeks.setForeground(modeTeks ? Theme.TEKS : Color.WHITE);
        perbaruiStatus();
        revalidate();
        repaint();
    }

    private void pasangIsi(JComponent c) {
        isi.removeAll();
        isi.add(c, BorderLayout.CENTER);
        isi.revalidate();
        isi.repaint();
    }

    private void pakaiInputManual() {
        matriksFile = null;
        gantiMode(modeTeks);
    }

    /**
     * Memeriksa bentuk dan ukuran matriks.
     * @param bolehBesar true untuk file: ukuran besar bukan error, cukup mengembalikan false
     * @return true kalau muat sebagai input manual
     */
    private boolean periksaUkuran(Matrix m, boolean bolehBesar) throws MatrixException {
        int kolomData = m.getCols();
        if (gaya == Gaya.PERSAMAAN) {
            if (m.getCols() < 2) {
                throw new MatrixException("SPL minimal punya 2 kolom (koefisien dan konstanta).");
            }
            kolomData = m.getCols() - 1;
        }
        if (gaya == Gaya.TITIK && m.getCols() != 2) {
            throw new MatrixException("Setiap baris titik harus berisi tepat 2 angka: x dan y.");
        }
        boolean muat = m.getRows() <= maksBaris && kolomData <= maksKolom;
        if (!muat && (!bolehBesar || gaya == Gaya.TITIK)) {
            throw new MatrixException("Input manual dibatasi " + maksBaris + " baris dan " + maksKolom + (gaya == Gaya.PERSAMAAN ? " variabel" : " kolom") + ". Untuk ukuran lebih besar gunakan BUKA FILE.");
        }
        return muat;
    }

    private int jumlahKolomSel() {
        switch (gaya) {
            case PERSAMAAN: return hitungKolom.getNilai() + 1; // + kolom konstanta b
            case MATRIKS: return hitungKolom.getNilai();
            case TITIK: return 2;
            default: return 1;
        }
    }

    /** Menyusun ulang kotak-kotak. Isi kotak lama dipertahankan kalau posisinya masih ada. */
    private void bangunUlang() {
        int baris = hitungBaris.getNilai();
        int kolom = jumlahKolomSel();
        JTextField[][] lama = sel;
        sel = new JTextField[baris][kolom];
        panelSel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        if (gaya == Gaya.MATRIKS) {
            for (int j = 0; j < kolom; j++) {
                c.gridx = j + 1;
                c.gridy = 0;
                panelSel.add(labelKecil("k" + (j + 1)), c);
            }
        }

        for (int i = 0; i < baris; i++) {
            // knot disusun 3 per baris supaya tidak memanjang ke bawah
            int x = (gaya == Gaya.KNOT) ? (i % 3) * 2 : 0;
            c.gridy = (gaya == Gaya.MATRIKS) ? i + 1 : (gaya == Gaya.KNOT ? i / 3 : i);
            for (int j = 0; j < kolom; j++) {
                JTextField f = buatKotak();
                if (i < lama.length && j < lama[i].length) {
                    f.setText(lama[i][j].getText());
                }
                sel[i][j] = f;

                String depan = labelDepan(i, j);
                if (depan != null) {
                    c.gridx = x++;
                    panelSel.add(labelKecil(depan), c);
                }
                c.gridx = x++;
                panelSel.add(f, c);
                String belakang = labelBelakang(j, kolom);
                if (belakang != null) {
                    c.gridx = x++;
                    panelSel.add(labelKecil(belakang), c);
                }
            }
        }
        panelSel.revalidate();
        panelSel.repaint();
        perbaruiStatus();
    }

    private JTextField buatKotak() {
        JTextField f = new JTextField(4);
        Theme.gayakan(f);
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        // kotak langsung merah kalau yang diketik bukan angka
        f.getDocument().addDocumentListener(new PendengarUbah(() -> periksaKotak(f)));
        // Enter pindah ke kotak berikutnya
        f.addActionListener(e -> f.transferFocus());
        return f;
    }

    private static void periksaKotak(JTextField f) {
        String teks = f.getText().trim();
        boolean valid = true;
        if (!teks.isEmpty()) {
            try {
                DecimalParser.parse(teks);
            } catch (NumberFormatException e) {
                valid = false;
            }
        }
        f.setBackground(valid ? Color.WHITE : Theme.MERAH_ERROR);
    }

    private void perbaruiStatus() {
        String teks;
        if (matriksFile != null) {
            teks = "Sumber: file (" + matriksFile.getRows() + " x " + matriksFile.getCols() + ")";
        } else if (modeTeks) {
            int baris = 0;
            for (String b : areaTeks.getText().split("\n")) {
                if (!b.trim().isEmpty()) baris++;
            }
            teks = "Sumber: teks, " + baris + " baris terisi";
        } else {
            switch (gaya) {
                case PERSAMAAN:
                    teks = "Sumber: per angka, " + hitungBaris.getNilai() + " persamaan, "
                            + hitungKolom.getNilai() + " variabel";
                    break;
                case MATRIKS:
                    teks = "Sumber: per angka, matriks " + hitungBaris.getNilai() + " x " + hitungKolom.getNilai();
                    break;
                case TITIK:
                    teks = "Sumber: per angka, " + hitungBaris.getNilai() + " titik";
                    break;
                default:
                    teks = "";
            }
        }
        labelStatus.setText(teks);
    }

    private String labelDepan(int i, int j) {
        switch (gaya) {
            case MATRIKS: return (j == 0) ? "b" + (i + 1) : null;
            case TITIK: return (j == 0 ? "x" : "y") + (i + 1) + " =";
            case KNOT: return "k" + (i + 1) + " =";
            default: return null;
        }
    }

    private String labelBelakang(int j, int kolom) {
        if (gaya != Gaya.PERSAMAAN || j == kolom - 1) return null;
        return "x" + (j + 1) + (j < kolom - 2 ? " +" : " =");
    }

    private String namaSel(int i, int j) {
        switch (gaya) {
            case PERSAMAAN:
                int n = sel[i].length - 1;
                return (j < n) ? "koefisien x" + (j + 1) + " pada persamaan " + (i + 1) : "b" + (i + 1);
            case MATRIKS: return "baris " + (i + 1) + " kolom " + (j + 1);
            case TITIK: return (j == 0 ? "x" : "y") + (i + 1);
            default: return "k" + (i + 1);
        }
    }

    private String gridKeTeks() {
        StringBuilder sb = new StringBuilder();
        for (JTextField[] baris : sel) {
            StringBuilder b = new StringBuilder();
            for (JTextField f : baris) {
                String t = f.getText().trim();
                if (!t.isEmpty()) b.append(t).append(" ");
            }
            if (b.length() > 0) sb.append(b.toString().trim()).append("\n");
        }
        return sb.toString();
    }

    private static String keTeks(Matrix m) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRows(); i++) {
            for (int j = 0; j < m.getCols(); j++) {
                if (j > 0) sb.append(" ");
                sb.append(teksAngka(m.get(i, j)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static JLabel labelKecil(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(Theme.ANGKA);
        l.setForeground(Theme.TEKS);
        return l;
    }

    private static void tandaiSalah(JTextField f) {
        f.setBackground(Theme.MERAH_ERROR);
        f.requestFocusInWindow();
    }

    // 2.0 ditulis "2", selain itu apa adanya supaya tidak ada presisi yang hilang
    private static String teksAngka(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    /** DocumentListener yang menjalankan satu aksi untuk semua jenis perubahan teks. */
    private static class PendengarUbah implements DocumentListener {
        private final Runnable aksi;

        PendengarUbah(Runnable aksi) {
            this.aksi = aksi;
        }

        @Override
        public void insertUpdate(DocumentEvent e) { aksi.run(); }

        @Override
        public void removeUpdate(DocumentEvent e) { aksi.run(); }

        @Override
        public void changedUpdate(DocumentEvent e) { aksi.run(); }
    }
}
