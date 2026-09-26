package algeo.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import algeo.core.util.NumberFormatter;
import algeo.gui.theme.Theme;

/**
 * Menggambar matriks pada satu langkah perhitungan, dengan sorotan sel yang berubah dan baris acuan.
 */
public class MatrixView extends JComponent {

    private static final int TINGGI_SEL = 22;
    private static final int PADDING = 10;

    private final double[][] data;
    private final boolean[][] berubah;
    private final int barisAcuan;     // -1 kalau tidak ada
    private final int kolomPemisah;   // garis tegak sebelum kolom ini, -1 kalau tidak ada
    private final int[] lebarKolom;

    public MatrixView(double[][] data, boolean[][] berubah, int barisAcuan) {
        this(data, berubah, barisAcuan, -1);
    }

    /**
     * @param kolomPemisah posisi garis pemisah dari tanda "|" di teks langkah;
     *                     -1 berarti ditebak dari ukuran ([A|b] atau [A|I])
     */
    public MatrixView(double[][] data, boolean[][] berubah, int barisAcuan, int kolomPemisah) {
        this.data = data;
        this.berubah = berubah;
        this.barisAcuan = barisAcuan;

        int baris = data.length;
        int kolom = data[0].length;
        // matriks augmented [A|b] atau [A|I] diberi garis pemisah
        if (kolomPemisah >= 0) this.kolomPemisah = kolomPemisah;
        else if (kolom == baris + 1) this.kolomPemisah = kolom - 1;
        else if (kolom == 2 * baris) this.kolomPemisah = baris;
        else this.kolomPemisah = -1;

        FontMetrics fm = getFontMetrics(Theme.ANGKA);
        lebarKolom = new int[kolom];
        for (int j = 0; j < kolom; j++) {
            int maks = fm.stringWidth("0");
            for (double[] b : data) {
                maks = Math.max(maks, fm.stringWidth(NumberFormatter.teks(b[j])));
            }
            lebarKolom[j] = maks + 14;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int w = 2 * PADDING;
        for (int l : lebarKolom) w += l;
        return new Dimension(w, data.length * TINGGI_SEL + 8);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);
        g2.setFont(Theme.ANGKA);
        FontMetrics fm = g2.getFontMetrics();
        int tinggi = data.length * TINGGI_SEL;
        int lebar = getPreferredSize().width;

        for (int i = 0; i < data.length; i++) {
            int x = PADDING;
            int y = 4 + i * TINGGI_SEL;
            for (int j = 0; j < data[i].length; j++) {
                if (berubah != null && berubah[i][j]) {
                    g2.setColor(Theme.SOROT_UBAH);
                    g2.fillRoundRect(x + 1, y + 1, lebarKolom[j] - 2, TINGGI_SEL - 2, 8, 8);
                } else if (i == barisAcuan) {
                    g2.setColor(Theme.SOROT_SUMBER);
                    g2.fillRoundRect(x + 1, y + 1, lebarKolom[j] - 2, TINGGI_SEL - 2, 8, 8);
                }
                String teks = NumberFormatter.teks(data[i][j]);
                g2.setColor(Theme.TEKS);
                g2.drawString(teks, x + lebarKolom[j] - 7 - fm.stringWidth(teks), y + TINGGI_SEL - 7);
                x += lebarKolom[j];
            }
        }

        // kurung siku kiri dan kanan
        g2.setColor(Theme.PINK_GARIS);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(4, 3, 4, tinggi + 5);
        g2.drawLine(4, 3, 10, 3);
        g2.drawLine(4, tinggi + 5, 10, tinggi + 5);
        g2.drawLine(lebar - 4, 3, lebar - 4, tinggi + 5);
        g2.drawLine(lebar - 10, 3, lebar - 4, 3);
        g2.drawLine(lebar - 10, tinggi + 5, lebar - 4, tinggi + 5);

        if (kolomPemisah > 0) {
            int x = PADDING;
            for (int j = 0; j < kolomPemisah; j++) x += lebarKolom[j];
            g2.setColor(new Color(184, 41, 95, 140));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 3f}, 0f));
            g2.drawLine(x, 4, x, tinggi + 4);
        }
        g2.dispose();
    }
}
