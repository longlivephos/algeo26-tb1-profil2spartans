package algeo.gui.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import algeo.gui.theme.Theme;

/**
 * Panel kartu bersudut bulat dengan judul di bagian atas, dipakai sebagai wadah bagian-bagian halaman.
 */
public class CardPanel extends JPanel {

    private static final int RADIUS = 34;
    private final String judul;

    public CardPanel(String judul, LayoutManager layout) {
        super(layout);
        this.judul = judul;
        setOpaque(false);
        int atas = (judul == null) ? 18 : 50;
        setBorder(BorderFactory.createEmptyBorder(atas, 18, 22, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);
        int w = getWidth();
        int h = getHeight();

        // bayangan
        g2.setColor(new Color(184, 41, 95, 60));
        g2.fillRoundRect(4, 7, w - 6, h - 8, RADIUS, RADIUS);
        // garis luar
        g2.setColor(Theme.PINK_GARIS);
        g2.fillRoundRect(0, 0, w - 5, h - 8, RADIUS, RADIUS);
        // isi gradasi
        g2.setPaint(new GradientPaint(0, 0, Theme.PINK_MUDA, 0, h, Theme.PINK));
        g2.fillRoundRect(5, 5, w - 15, h - 18, RADIUS - 8, RADIUS - 8);
        // kilap
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(16, 10, w - 38, 12, 12, 12);

        if (judul != null) {
            g2.setFont(Theme.pixel(15f));
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(judul)) / 2;
            g2.setColor(Theme.PINK_GARIS);
            g2.drawString(judul, x + 2, 40);
            g2.setColor(Color.WHITE);
            g2.drawString(judul, x, 38);
        }
        g2.dispose();
    }
}
