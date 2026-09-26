package algeo.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.JComponent;

import algeo.gui.theme.Theme;

/**
 * Indikator kemajuan langkah perhitungan dengan tiga penanda di 1/3, 2/3, dan akhir.
 */
public class StepProgressBar extends JComponent {

    private int sekarang = 0;
    private int total = 0;

    public StepProgressBar() {
        setPreferredSize(new Dimension(220, 34));
    }

    public void setProgres(int sekarang, int total) {
        this.sekarang = sekarang;
        this.total = total;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);
        int w = getWidth();
        int yBar = 20;
        double rasio = (total == 0) ? 0 : (double) sekarang / total;

        // bar
        g2.setColor(Theme.PINK_GARIS);
        g2.fillRoundRect(0, yBar, w, 12, 12, 12);
        g2.setColor(Theme.PINK_MUDA);
        g2.fillRoundRect(2, yBar + 2, w - 4, 8, 8, 8);
        if (rasio > 0) {
            g2.setColor(Theme.KUNING);
            g2.fillRoundRect(2, yBar + 2, (int) ((w - 4) * rasio), 8, 8, 8);
        }

        // tiga bintang di posisi 1/3, 2/3, dan ujung
        for (int i = 1; i <= 3; i++) {
            double batas = i / 3.0;
            int cx = (int) ((w - 12) * batas);
            Shape s = Theme.bintang(cx, 9, 8, 3.6);
            g2.setColor(rasio >= batas - 1e-9 ? Theme.KUNING : new Color(0xF7B5CD));
            g2.fill(s);
            g2.setColor(rasio >= batas - 1e-9 ? Theme.KUNING_TUA : Theme.PINK_TUA);
            g2.draw(s);
        }
        g2.dispose();
    }
}
