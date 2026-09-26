package algeo.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import javax.swing.JPanel;
import javax.swing.Timer;

import algeo.core.util.NumberFormatter;
import algeo.gui.theme.Theme;

/**
 * Grafik titik data, kurva hasil, posisi knot, dan titik evaluasi, dilengkapi pembacaan nilai saat kursor diarahkan.
 */
public class PlotPanel extends JPanel {

    private static final int MARGIN_KIRI = 64;
    private static final int MARGIN = 30;
    private static final int JUMLAH_SAMPEL = 300;
    private static final int JUMLAH_GARIS = 5;

    private double[] xData;
    private double[] yData;
    private DoubleUnaryOperator fungsi;
    private double[] knot = new double[0];
    private final List<double[]> titikEvaluasi = new ArrayList<>();

    private double progres = 0;   // 0 = kurva belum tampil, 1 = kurva penuh
    private final Timer animasi;
    private int mouseX = -1;      // posisi mouse, -1 kalau mouse di luar grafik

    // batas sumbu, dihitung ulang setiap kali digambar
    private double xKiri, xKanan, yBawah, yAtas;

    public PlotPanel() {
        setPreferredSize(new Dimension(450, 300));
        setOpaque(false);
        animasi = new Timer(25, e -> {
            progres = Math.min(1, progres + 0.03);
            if (progres >= 1) ((Timer) e.getSource()).stop();
            repaint();
        });

        MouseAdapter pendengarMouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                repaint();
            }
        };
        addMouseListener(pendengarMouse);
        addMouseMotionListener(pendengarMouse);
    }

    /**
     * @param fungsi kurva hasil, misalnya t -> Interpolasi.taksirNilaiPolinomial(koef, t)
     * @param knot posisi knot (boleh null)
     */
    public void setData(double[] x, double[] y, DoubleUnaryOperator fungsi, double[] knot) {
        this.xData = x;
        this.yData = y;
        this.fungsi = fungsi;
        this.knot = (knot == null) ? new double[0] : knot;
        titikEvaluasi.clear();
        progres = 0;
        animasi.stop();
        repaint();
    }

    /** Dipanggil setelah langkah perhitungan selesai diputar. */
    public void mulaiAnimasi() {
        progres = 0;
        animasi.restart();
    }

    public void tambahTitikEvaluasi(double x, double y) {
        titikEvaluasi.add(new double[] {x, y});
        repaint();
    }

    public void kosongkan() {
        xData = null;
        titikEvaluasi.clear();
        animasi.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);

        // latar kartu putih dengan garis pink
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
        g2.setColor(Theme.PINK_GARIS);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 24, 24);

        if (xData == null) {
            g2.setFont(Theme.pixel(9f));
            g2.setColor(Theme.PINK_TUA);
            String teks = "GRAFIK MUNCUL SETELAH HITUNG";
            g2.drawString(teks, (getWidth() - g2.getFontMetrics().stringWidth(teks)) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }

        hitungBatasSumbu();
        double[] kx = new double[JUMLAH_SAMPEL + 1];
        double[] ky = new double[JUMLAH_SAMPEL + 1];
        for (int i = 0; i <= JUMLAH_SAMPEL; i++) {
            kx[i] = xKiri + (xKanan - xKiri) * i / JUMLAH_SAMPEL;
            ky[i] = fungsi.applyAsDouble(kx[i]);
            if (!Double.isNaN(ky[i]) && !Double.isInfinite(ky[i])) {
                yBawah = Math.min(yBawah, ky[i]);
                yAtas = Math.max(yAtas, ky[i]);
            }
        }
        double tinggi = yAtas - yBawah;
        if (tinggi == 0) tinggi = 1;
        yBawah -= tinggi * 0.1;
        yAtas += tinggi * 0.1;

        int kiri = MARGIN_KIRI;
        int atas = MARGIN;
        int kanan = getWidth() - MARGIN;
        int bawah = getHeight() - MARGIN - 10;

        // garis bantu dan angka di sumbu
        g2.setFont(Theme.ANGKA.deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= JUMLAH_GARIS; i++) {
            double nx = xKiri + (xKanan - xKiri) * i / JUMLAH_GARIS;
            double ny = yBawah + (yAtas - yBawah) * i / JUMLAH_GARIS;
            int px = layarX(nx);
            int py = layarY(ny);
            g2.setColor(new Color(0xFCE4EE));
            g2.drawLine(px, atas, px, bawah);
            g2.drawLine(kiri, py, kanan, py);
            g2.setColor(Theme.TEKS);
            String tx = NumberFormatter.teks(nx);
            String ty = NumberFormatter.teks(ny);
            g2.drawString(tx, px - fm.stringWidth(tx) / 2, bawah + 16);
            g2.drawString(ty, kiri - 6 - fm.stringWidth(ty), py + 4);
        }
        g2.setColor(Theme.PINK_MUDA);
        g2.drawRect(kiri, atas, kanan - kiri, bawah - atas);

        // sumbu x = 0 dan y = 0 kalau terlihat
        g2.setColor(new Color(0xE0B0C4));
        if (yBawah < 0 && yAtas > 0) g2.drawLine(kiri, layarY(0), kanan, layarY(0));
        if (xKiri < 0 && xKanan > 0) g2.drawLine(layarX(0), atas, layarX(0), bawah);

        // garis knot
        g2.setColor(Theme.BIRU_TUA);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {6f, 4f}, 0f));
        for (double k : knot) {
            g2.drawLine(layarX(k), atas, layarX(k), bawah);
        }

        // kurva, hanya sampai bagian "progres"
        g2.setColor(Theme.PINK_TUA);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int batas = (int) (progres * JUMLAH_SAMPEL);
        for (int i = 0; i < batas; i++) {
            if (Double.isNaN(ky[i]) || Double.isNaN(ky[i + 1])) continue;
            g2.drawLine(layarX(kx[i]), layarY(ky[i]), layarX(kx[i + 1]), layarY(ky[i + 1]));
        }

        // titik data berbentuk bintang
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < xData.length; i++) {
            Shape s = Theme.bintang(layarX(xData[i]), layarY(yData[i]), 9, 4);
            g2.setColor(Theme.KUNING);
            g2.fill(s);
            g2.setColor(Theme.KUNING_TUA);
            g2.draw(s);
        }

        // titik evaluasi xt
        for (double[] t : titikEvaluasi) {
            g2.setColor(Theme.BIRU);
            g2.fillOval(layarX(t[0]) - 6, layarY(t[1]) - 6, 12, 12);
            g2.setColor(Theme.BIRU_TUA);
            g2.drawOval(layarX(t[0]) - 6, layarY(t[1]) - 6, 12, 12);
        }

        // nilai kurva di posisi mouse (hanya setelah kurva selesai digambar)
        if (progres >= 1 && mouseX >= kiri && mouseX <= kanan) {
            double x = xKiri + (double) (mouseX - kiri) / (kanan - kiri) * (xKanan - xKiri);
            double y = fungsi.applyAsDouble(x);
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                int py = layarY(y);
                g2.setColor(new Color(184, 41, 95, 90));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(mouseX, atas, mouseX, bawah);
                g2.setColor(Theme.PINK_GARIS);
                g2.fillOval(mouseX - 5, py - 5, 10, 10);

                String label = "(" + NumberFormatter.teks(x) + ", " + NumberFormatter.teks(y) + ")";
                int lebarLabel = fm.stringWidth(label) + 12;
                int lx = Math.min(mouseX + 10, kanan - lebarLabel);
                int ly = Math.max(atas + 4, py - 28);
                g2.setColor(Theme.PINK_GARIS);
                g2.fillRoundRect(lx, ly, lebarLabel, 20, 10, 10);
                g2.setColor(Color.WHITE);
                g2.drawString(label, lx + 6, ly + 14);
            }
        }
        g2.dispose();
    }

    // rentang x mengikuti data dan titik evaluasi, sedikit dilebarkan
    private void hitungBatasSumbu() {
        xKiri = min(xData);
        xKanan = max(xData);
        yBawah = min(yData);
        yAtas = max(yData);
        for (double[] t : titikEvaluasi) {
            xKiri = Math.min(xKiri, t[0]);
            xKanan = Math.max(xKanan, t[0]);
            yBawah = Math.min(yBawah, t[1]);
            yAtas = Math.max(yAtas, t[1]);
        }
        double lebar = xKanan - xKiri;
        if (lebar == 0) lebar = 1;
        xKiri -= lebar * 0.05;
        xKanan += lebar * 0.05;
    }

    private int layarX(double x) {
        int kiri = MARGIN_KIRI;
        int kanan = getWidth() - MARGIN;
        return kiri + (int) Math.round((x - xKiri) / (xKanan - xKiri) * (kanan - kiri));
    }

    private int layarY(double y) {
        int atas = MARGIN;
        int bawah = getHeight() - MARGIN - 10;
        return bawah - (int) Math.round((y - yBawah) / (yAtas - yBawah) * (bawah - atas));
    }

    private static double min(double[] a) {
        double hasil = a[0];
        for (double v : a) hasil = Math.min(hasil, v);
        return hasil;
    }

    private static double max(double[] a) {
        double hasil = a[0];
        for (double v : a) hasil = Math.max(hasil, v);
        return hasil;
    }
}
