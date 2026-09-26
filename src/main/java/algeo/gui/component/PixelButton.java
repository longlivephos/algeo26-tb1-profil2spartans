package algeo.gui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.JButton;

import algeo.gui.theme.Theme;

/**
 * Tombol bergaya pixel: sudut bertangga, garis tepi tebal, dan efek turun saat ditekan.
 */
public class PixelButton extends JButton {

    private static final int P = 3; // ukuran satu "pixel"

    private Color warna;
    private Color warnaGelap;

    public PixelButton(String teks) {
        this(teks, Theme.PINK, Theme.PINK_TUA);
    }

    public PixelButton(String teks, Color warna, Color warnaGelap) {
        super(teks);
        this.warna = warna;
        this.warnaGelap = warnaGelap;
        setFont(Theme.pixel(10f));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Mengganti warna tombol, dipakai untuk menandai tab yang sedang aktif. */
    public void setWarna(Color warna, Color warnaGelap) {
        this.warna = warna;
        this.warnaGelap = warnaGelap;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 10 * P, fm.getHeight() + 8 * P);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight() - P; // sisakan satu pixel untuk bayangan
        boolean ditekan = getModel().isArmed() && getModel().isPressed();
        int turun = ditekan ? P : 0;

        Color isi = warna;
        if (!isEnabled()) isi = new Color(0xCCCCCC);
        else if (getModel().isRollover()) isi = warna.brighter();

        if (!ditekan) {
            g2.setColor(warnaGelap.darker());
            kotakTangga(g2, 0, P, w, h);
        }
        g2.setColor(warnaGelap.darker());
        kotakTangga(g2, 0, turun, w, h);
        g2.setColor(isi);
        kotakTangga(g2, P, turun + P, w - 2 * P, h - 2 * P);

        // kilap tipis di bagian atas
        g2.setColor(new Color(255, 255, 255, 110));
        g2.fillRect(3 * P, turun + 2 * P, w - 6 * P, P);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = turun + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.setFont(getFont());
        g2.setColor(new Color(0, 0, 0, 70));
        g2.drawString(getText(), x + 2, y + 2);
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    // persegi panjang yang keempat sudutnya dipotong satu pixel, ciri khas pixel art
    private static void kotakTangga(Graphics2D g2, int x, int y, int w, int h) {
        g2.fillRect(x + P, y, w - 2 * P, h);
        g2.fillRect(x, y + P, w, h - 2 * P);
    }
}
