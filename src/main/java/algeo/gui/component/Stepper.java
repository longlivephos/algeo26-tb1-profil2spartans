package algeo.gui.component;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import algeo.gui.theme.Theme;

/**
 * Pengatur angka bulat dengan tombol kurang dan tambah, dipakai untuk mengatur ukuran input.
 */
public class Stepper extends JPanel {

    private final int min;
    private final int maks;
    private int nilai;
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final PixelButton tombolKurang = new PixelButton("-");
    private final PixelButton tombolTambah = new PixelButton("+");
    private Runnable saatBerubah;

    public Stepper(int awal, int min, int maks) {
        super(new FlowLayout(FlowLayout.LEFT, 3, 0));
        this.min = min;
        this.maks = maks;
        setOpaque(false);

        label.setFont(Theme.pixel(10f));
        label.setForeground(Theme.TEKS);
        label.setPreferredSize(new Dimension(34, 26));
        tombolKurang.setFont(Theme.pixel(12f));
        tombolTambah.setFont(Theme.pixel(12f));
        tombolKurang.addActionListener(e -> setNilai(nilai - 1));
        tombolTambah.addActionListener(e -> setNilai(nilai + 1));

        add(tombolKurang);
        add(label);
        add(tombolTambah);
        setNilai(awal);
    }

    public int getNilai() {
        return nilai;
    }

    public int getMaks() {
        return maks;
    }

    /** Nilai di luar batas otomatis dipotong ke batas. */
    public void setNilai(int baru) {
        baru = Math.max(min, Math.min(maks, baru));
        boolean berubah = (baru != nilai);
        nilai = baru;
        label.setText(String.valueOf(nilai));
        tombolKurang.setEnabled(nilai > min);
        tombolTambah.setEnabled(nilai < maks);
        if (berubah && saatBerubah != null) saatBerubah.run();
    }

    public void setSaatBerubah(Runnable aksi) {
        this.saatBerubah = aksi;
    }
}
