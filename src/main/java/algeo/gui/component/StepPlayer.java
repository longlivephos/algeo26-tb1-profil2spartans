package algeo.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import algeo.gui.theme.Theme;
import algeo.gui.theme.ThemedUI;

/**
 * Menampilkan langkah perhitungan secara bertahap beserta kontrol pemutaran dan indikator kemajuan.
 */
public class StepPlayer extends JPanel {

    private static final int BATAS_TEKS_HASIL = 20000;
    private static final Pattern POLA_ACUAN = Pattern.compile("x Baris (\\d+)");
    private static final Pattern POLA_OPERASI = Pattern.compile("Baris \\d+ = Baris");

    private final JPanel daftar = new JPanel();
    private final JScrollPane scroll;
    private final Timer timer;
    private final JSlider sliderKecepatan = new JSlider(1, 10, 6);
    private final PixelButton tombolMundur = tombolKecil("<");
    private final PixelButton tombolJeda = tombolKecil("JEDA");
    private final PixelButton tombolMaju = tombolKecil(">");
    private final PixelButton tombolLewati = tombolKecil("LEWATI");
    private final PixelButton tombolUlangi = tombolKecil("ULANGI");
    private final StepProgressBar bar = new StepProgressBar();
    private final Chip chipLangkah = new Chip();
    private final Chip chipTukar = new Chip();
    private final Chip chipOperasi = new Chip();

    private List<String> langkah = new ArrayList<>();
    private String hasil = "";
    private String status = "";
    private Runnable saatSelesai;
    private int indeks;
    private boolean selesai = true;
    private boolean adaHasil = false;
    private double[][] matriksSebelum;

    // untuk tombol mundur: komponen yang ditambahkan tiap langkah dan matriks sebelumnya
    private final List<Component[]> kartuLangkah = new ArrayList<>();
    private final List<double[][]> riwayatMatriks = new ArrayList<>();

    public StepPlayer() {
        super(new BorderLayout(0, 8));
        setOpaque(false);

        daftar.setLayout(new BoxLayout(daftar, BoxLayout.Y_AXIS));
        daftar.setBackground(Color.WHITE);
        daftar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll = new JScrollPane(daftar);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.PINK_GARIS, 3));
        ThemedUI.gayakan(scroll);

        timer = new Timer(jeda(), e -> langkahBerikut());
        ThemedUI.gayakan(sliderKecepatan);
        sliderKecepatan.setToolTipText("Kecepatan animasi langkah");
        sliderKecepatan.addChangeListener(e -> timer.setDelay(jeda()));

        tombolMundur.setToolTipText("Mundur satu langkah");
        tombolMaju.setToolTipText("Maju satu langkah");
        tombolMundur.addActionListener(e -> mundur());
        tombolJeda.addActionListener(e -> jedaAtauLanjut());
        tombolMaju.addActionListener(e -> maju());
        tombolLewati.addActionListener(e -> lewati());
        tombolUlangi.addActionListener(e -> putar(langkah, hasil, status, saatSelesai));

        JPanel barisKontrol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barisKontrol.setOpaque(false);
        barisKontrol.add(tombolMundur);
        barisKontrol.add(tombolJeda);
        barisKontrol.add(tombolMaju);
        barisKontrol.add(tombolLewati);
        barisKontrol.add(tombolUlangi);
        barisKontrol.add(Box.createHorizontalStrut(10));
        barisKontrol.add(Theme.label("Lambat"));
        barisKontrol.add(sliderKecepatan);
        barisKontrol.add(Theme.label("Cepat"));

        JPanel barisIndikator = new JPanel(new BorderLayout(10, 0));
        barisIndikator.setOpaque(false);
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        chip.setOpaque(false);
        chip.add(chipLangkah);
        chip.add(chipTukar);
        chip.add(chipOperasi);
        barisIndikator.add(bar, BorderLayout.CENTER);
        barisIndikator.add(chip, BorderLayout.EAST);

        JPanel atas = new JPanel(new BorderLayout(0, 6));
        atas.setOpaque(false);
        atas.add(barisKontrol, BorderLayout.NORTH);
        atas.add(barisIndikator, BorderLayout.SOUTH);

        add(atas, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        kosongkan();
    }

    /**
     * Mulai menampilkan langkah dari awal.
     * @param langkah langkah dari library
     * @param hasil teks hasil akhir
     * @param status ringkasan singkat untuk badge hasil, misal "SOLUSI TUNGGAL" (boleh kosong)
     * @param saatSelesai dijalankan setelah kartu hasil muncul (boleh null)
     */
    public void putar(List<String> langkah, String hasil, String status, Runnable saatSelesai) {
        timer.stop();
        this.langkah = (langkah == null) ? new ArrayList<>() : langkah;
        this.hasil = hasil;
        this.status = (status == null) ? "" : status;
        this.saatSelesai = saatSelesai;
        indeks = 0;
        selesai = false;
        adaHasil = false;
        matriksSebelum = null;
        kartuLangkah.clear();
        riwayatMatriks.clear();
        daftar.removeAll();
        daftar.revalidate();
        daftar.repaint();
        tombolJeda.setText("JEDA");
        perbaruiIndikator();
        timer.start();
    }

    /** Menambah kartu kecil langsung tanpa jeda, misalnya hasil evaluasi xt. */
    public void tambahCatatan(String judul, String teks) {
        tambahKomponen(new Kartu(judul, Theme.BIRU, "", teks, null));
    }

    public void kosongkan() {
        timer.stop();
        selesai = true;
        adaHasil = false;
        langkah = new ArrayList<>();
        indeks = 0;
        kartuLangkah.clear();
        riwayatMatriks.clear();
        daftar.removeAll();
        JLabel kosong = Theme.label("Isi input lalu tekan HITUNG. Langkah perhitungan akan muncul di sini.");
        kosong.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        daftar.add(kosong);
        daftar.revalidate();
        daftar.repaint();
        perbaruiIndikator();
    }

    // ===================== kontrol =====================

    private int jeda() {
        return 1100 - sliderKecepatan.getValue() * 100; // 1000 ms (lambat) sampai 100 ms (cepat)
    }

    private void langkahBerikut() {
        if (indeks < langkah.size()) {
            tampilkanLangkah(indeks);
            indeks++;
        } else {
            timer.stop();
            tampilkanHasil();
        }
        perbaruiIndikator();
    }

    private void jedaAtauLanjut() {
        if (timer.isRunning()) {
            timer.stop();
        } else if (!selesai) {
            timer.start();
        }
        perbaruiIndikator();
    }

    private void maju() {
        timer.stop();
        langkahBerikut();
    }

    /** Menghapus kartu langkah terakhir dan mengembalikan matriks pembanding sebelumnya. */
    private void mundur() {
        timer.stop();
        if (indeks > 0 && !selesai) {
            indeks--;
            for (Component c : kartuLangkah.remove(kartuLangkah.size() - 1)) {
                daftar.remove(c);
            }
            matriksSebelum = riwayatMatriks.remove(riwayatMatriks.size() - 1);
            daftar.revalidate();
            daftar.repaint();
        }
        perbaruiIndikator();
    }

    private void lewati() {
        timer.stop();
        while (indeks < langkah.size()) {
            tampilkanLangkah(indeks);
            indeks++;
        }
        tampilkanHasil();
        perbaruiIndikator();
    }

    private void tampilkanHasil() {
        if (selesai) return;
        selesai = true;
        adaHasil = true;
        String teks = hasil.stripTrailing();
        if (teks.length() > BATAS_TEKS_HASIL) {
            teks = teks.substring(0, BATAS_TEKS_HASIL)
                    + "\n... (hasil terlalu panjang, simpan ke file untuk melihat semuanya)";
        }
        String judul = status.isEmpty() ? "HASIL" : "HASIL - " + status;
        tambahKomponen(buatKartu(judul, Theme.KUNING, teks, false));
        if (saatSelesai != null) saatSelesai.run();
    }

    /** Memperbarui progress bar, chip, dan tombol mana yang bisa ditekan. */
    private void perbaruiIndikator() {
        int total = langkah.size();
        int tukar = 0;
        int operasi = 0;
        boolean adaTukar = false;
        boolean adaOperasi = false;
        for (int i = 0; i < total; i++) {
            String s = langkah.get(i);
            boolean isTukar = s.toLowerCase().contains("tukar baris");
            boolean isOperasi = POLA_OPERASI.matcher(s).find();
            adaTukar = adaTukar || isTukar;
            adaOperasi = adaOperasi || isOperasi;
            if (i < indeks && isTukar) tukar++;
            if (i < indeks && isOperasi) operasi++;
        }
        if (adaHasil) {
            bar.setProgres(1, 1);
        } else {
            bar.setProgres(indeks, Math.max(total, 1));
        }
        chipLangkah.setText("LANGKAH " + indeks + "/" + total);
        chipTukar.setText("TUKAR BARIS " + tukar);
        chipOperasi.setText("OPERASI BARIS " + operasi);
        // chip hanya tampil kalau metode ini memang punya langkah tukar / operasi baris
        chipTukar.setVisible(adaTukar);
        chipOperasi.setVisible(adaOperasi);

        tombolMundur.setEnabled(!selesai && indeks > 0);
        tombolJeda.setEnabled(!selesai);
        tombolJeda.setText(timer.isRunning() ? "JEDA" : "LANJUT");
        tombolMaju.setEnabled(!selesai);
        tombolLewati.setEnabled(!selesai);
        tombolUlangi.setEnabled(selesai && total > 0);
    }

    // ===================== kartu langkah =====================

    /** Memisahkan teks biasa dan baris matriks dari satu langkah, lalu membuat kartunya. */
    private void tampilkanLangkah(int i) {
        riwayatMatriks.add(matriksSebelum);
        Component[] ditambah = tambahKomponen(buatKartu("LANGKAH " + (i + 1), Theme.PINK, langkah.get(i), true));
        kartuLangkah.add(ditambah);
    }

    /**
     * Memisahkan teks biasa dan baris matriks dari satu langkah (atau hasil), lalu membuat kartunya.
     * Semua halaman memakai cara yang sama, jadi tampilan langkahnya seragam.
     * @param sorot true untuk langkah: sel yang berubah dari matriks sebelumnya disorot
     */
    private Kartu buatKartu(String judul, Color warna, String isi, boolean sorot) {
        List<String> teks = new ArrayList<>();
        List<double[]> barisMatriks = new ArrayList<>();
        int pemisah = -1;
        for (String baris : isi.split("\n")) {
            double[] angka = bacaBarisMatriks(baris);
            if (angka != null) {
                barisMatriks.add(angka);
                if (pemisah < 0) pemisah = posisiPemisah(baris);
            } else if (!baris.trim().isEmpty()) {
                teks.add(baris);
            }
        }

        MatrixView view = null;
        if (!barisMatriks.isEmpty() && semuaSamaPanjang(barisMatriks)) {
            double[][] m = barisMatriks.toArray(new double[0][]);
            if (sorot) {
                view = new MatrixView(m, bandingkan(matriksSebelum, m), cariBarisAcuan(teks), pemisah);
                matriksSebelum = m;
            } else {
                view = new MatrixView(m, null, -1, pemisah);
            }
        } else {
            // bentuknya tidak rapi, tampilkan saja sebagai teks
            for (double[] b : barisMatriks) teks.add(Arrays.toString(b));
        }
        String kategori = sorot ? StepFormatter.kategori(isi) : "";
        return new Kartu(judul, warna, kategori, String.join("\n", teks), view);
    }

    private Component[] tambahKomponen(Kartu kartu) {
        if (daftar.getComponentCount() == 1 && daftar.getComponent(0) instanceof JLabel) {
            daftar.removeAll(); // hapus tulisan petunjuk awal
        }
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        Component jarak = Box.createVerticalStrut(8);
        daftar.add(kartu);
        daftar.add(jarak);
        daftar.revalidate();
        // gulir ke bawah setelah layout selesai dihitung
        SwingUtilities.invokeLater(() -> {
            JScrollBar b = scroll.getVerticalScrollBar();
            b.setValue(b.getMaximum());
        });
        return new Component[] {kartu, jarak};
    }

    /** "[ 1.000  -2.500 ]" menjadi {1.0, -2.5}. Mengembalikan null kalau bukan baris matriks. */
    static double[] bacaBarisMatriks(String baris) {
        String s = baris.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return null;
        String isi = s.substring(1, s.length() - 1).replace("|", " ").trim();
        if (isi.isEmpty()) return null;
        String[] token = isi.split("\\s+");
        double[] hasil = new double[token.length];
        try {
            for (int j = 0; j < token.length; j++) {
                hasil[j] = Double.parseDouble(token[j]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return hasil;
    }

    /** "[ 1 2 | 3 4 ]" -> 2 (banyak angka sebelum tanda |), -1 kalau tidak ada tanda |. */
    static int posisiPemisah(String baris) {
        int posisi = baris.indexOf('|');
        if (posisi < 0) return -1;
        String kiri = baris.substring(baris.indexOf('[') + 1, posisi).trim();
        return kiri.isEmpty() ? 0 : kiri.split("\\s+").length;
    }

    private static boolean semuaSamaPanjang(List<double[]> baris) {
        for (double[] b : baris) {
            if (b.length != baris.get(0).length) return false;
        }
        return true;
    }

    // sel yang nilainya berbeda dari matriks langkah sebelumnya (ukuran harus sama)
    private static boolean[][] bandingkan(double[][] lama, double[][] baru) {
        if (lama == null || lama.length != baru.length || lama[0].length != baru[0].length) {
            return null;
        }
        boolean[][] hasil = new boolean[baru.length][baru[0].length];
        for (int i = 0; i < baru.length; i++) {
            for (int j = 0; j < baru[i].length; j++) {
                hasil[i][j] = Math.abs(lama[i][j] - baru[i][j]) > 1e-9;
            }
        }
        return hasil;
    }

    // "Baris 3 = Baris 3 - (2) x Baris 1" -> baris acuan = indeks 0
    private static int cariBarisAcuan(List<String> teks) {
        for (String t : teks) {
            Matcher m = POLA_ACUAN.matcher(t);
            if (m.find()) return Integer.parseInt(m.group(1)) - 1;
        }
        return -1;
    }

    private static PixelButton tombolKecil(String teks) {
        PixelButton t = new PixelButton(teks, Theme.BIRU, Theme.BIRU_TUA);
        t.setFont(Theme.pixel(8f));
        return t;
    }

    /** Label kecil berlatar pink bulat untuk indikator. */
    private static class Chip extends JLabel {

        Chip() {
            setFont(Theme.pixel(7f));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            g2.setColor(Theme.PINK_TUA);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Satu kartu langkah: badge judul, teks penjelasan, dan (opsional) gambar matriks. */
    private static class Kartu extends JPanel {

        private final Color warnaAksen;

        Kartu(String judul, Color warnaBadge, String kategori, String teks, JComponent matriks) {
            super(new BorderLayout(0, 8));
            this.warnaAksen = warnaBadge;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 18, 12, 12));

            JLabel badge = new JLabel(judul);
            badge.setFont(Theme.pixel(8f));
            badge.setForeground(warnaBadge == Theme.KUNING ? Theme.TEKS : Color.WHITE);
            badge.setOpaque(true);
            badge.setBackground(warnaBadge);
            badge.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            atas.setOpaque(false);
            atas.add(badge);
            if (!kategori.isEmpty()) {
                // jenis langkah, misalnya OPERASI BARIS atau KOFAKTOR
                JLabel label = new JLabel(kategori);
                label.setFont(Theme.pixel(8f));
                label.setForeground(Theme.PINK_TUA);
                atas.add(label);
            }
            add(atas, BorderLayout.NORTH);

            // setiap baris teks digambar sebagai rumus atau keterangan
            if (!teks.isEmpty()) {
                JPanel isiTeks = new JPanel();
                isiTeks.setLayout(new BoxLayout(isiTeks, BoxLayout.Y_AXIS));
                isiTeks.setOpaque(false);
                for (String baris : teks.split("\n")) {
                    if (baris.trim().isEmpty()) continue;
                    for (StepFormatter.Baris b : StepFormatter.format(baris)) {
                        isiTeks.add(new MathText(b.markup, b.gaya));
                    }
                }
                add(isiTeks, BorderLayout.CENTER);
            }
            if (matriks != null) {
                JPanel wadah = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                wadah.setOpaque(false);
                wadah.add(matriks);
                add(wadah, BorderLayout.SOUTH);
            }
        }

        @Override
        public Dimension getMaximumSize() {
            // supaya BoxLayout tidak meregangkan tinggi kartu
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            g2.setColor(new Color(0xFFF3F8));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            // garis warna di sisi kiri kartu
            g2.setColor(warnaAksen);
            g2.fillRoundRect(0, 0, 6, getHeight() - 1, 6, 6);
            g2.setColor(Theme.PINK_MUDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
        }
    }
}
