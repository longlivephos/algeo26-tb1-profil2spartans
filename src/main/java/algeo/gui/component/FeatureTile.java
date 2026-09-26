package algeo.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.JButton;

import algeo.gui.theme.Theme;

/**
 * Tile fitur pada halaman beranda: nama fitur, deskripsi singkat, dan label keterangan.
 */
public class FeatureTile extends JButton {

    private final String judul;
    private final String penjelasan;
    private final String keterangan;

    public FeatureTile(String judul, String penjelasan, String keterangan) {
        this.judul = judul;
        this.penjelasan = penjelasan;
        this.keterangan = keterangan;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(240, 150));
        setToolTipText(penjelasan);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);
        boolean disorot = getModel().isRollover();
        boolean ditekan = getModel().isArmed() && getModel().isPressed();
        int naik = ditekan ? 0 : (disorot ? -4 : 0);
        int w = getWidth();
        int h = getHeight() - 8;

        // bayangan
        g2.setColor(new Color(184, 41, 95, 70));
        g2.fillRoundRect(3, 8, w - 6, h, 26, 26);
        // garis luar, kuning kalau disorot
        g2.setColor(disorot ? Theme.KUNING_TUA : Theme.PINK_GARIS);
        g2.fillRoundRect(0, 4 + naik, w - 4, h, 26, 26);
        // isi
        g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, h, disorot ? new Color(0xFFF1B8) : Theme.PINK_MUDA));
        g2.fillRoundRect(4, 8 + naik, w - 12, h - 8, 20, 20);

        // bintang di pojok kiri atas
        Shape s = Theme.bintang(30, 34 + naik, 14, 6);
        g2.setColor(Theme.KUNING);
        g2.fill(s);
        g2.setColor(Theme.KUNING_TUA);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(s);

        // judul
        g2.setFont(Theme.pixel(Math.min(15f, w / 14f)));
        g2.setColor(Theme.PINK_GARIS);
        g2.drawString(judul, 52, 42 + naik);

        // penjelasan, dipotong per kata supaya muat
        g2.setFont(Theme.BIASA.deriveFont(15f));
        g2.setColor(Theme.TEKS);
        FontMetrics fm = g2.getFontMetrics();
        int y = 74 + naik;
        StringBuilder baris = new StringBuilder();
        for (String kata : penjelasan.split(" ")) {
            String coba = baris.length() == 0 ? kata : baris + " " + kata;
            if (fm.stringWidth(coba) > w - 44 && baris.length() > 0) {
                g2.drawString(baris.toString(), 22, y);
                y += fm.getHeight();
                baris = new StringBuilder(kata);
            } else {
                baris = new StringBuilder(coba);
            }
        }
        g2.drawString(baris.toString(), 22, y);

        // keterangan kecil di pojok kiri bawah, misalnya "4 METODE"
        g2.setFont(Theme.pixel(8f));
        fm = g2.getFontMetrics();
        int lebarChip = fm.stringWidth(keterangan) + 16;
        g2.setColor(Theme.PINK_TUA);
        g2.fillRoundRect(20, h - 32 + naik, lebarChip, 22, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawString(keterangan, 28, h - 17 + naik);

        // tanda panah di pojok kanan bawah
        g2.setFont(Theme.pixel(12f));
        g2.setColor(disorot ? Theme.KUNING_TUA : Theme.PINK_TUA);
        g2.drawString(">", w - 34, h - 15 + naik);
        g2.dispose();
    }
}
