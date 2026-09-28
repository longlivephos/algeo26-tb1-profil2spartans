package algeo.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import algeo.core.util.NumberFormatter;

public class PlotPanel extends JPanel {

    private static final int MARGIN = 45;

    private double[] xTitik;
    private double[] yTitik;
    private double[] xKurva;
    private double[] yKurva;
    private double[] knot = new double[0];
    private List<double[]> titikEvaluasi = new ArrayList<>();

    // batas sumbu
    private double xMin, xMax, yMin, yMax;

    public PlotPanel() {
        setPreferredSize(new Dimension(450, 280));
        setBackground(new Color(255, 245, 250));
    }

    public void setData(double[] xTitik, double[] yTitik, double[] xKurva, double[] yKurva, double[] knot) {
        this.xTitik = xTitik;
        this.yTitik = yTitik;
        this.xKurva = xKurva;
        this.yKurva = yKurva;
        if (knot == null) {
            this.knot = new double[0];
        } else {
            this.knot = knot;
        }
        titikEvaluasi.clear();
        repaint();
    }

    public void tambahTitikEvaluasi(double x, double y) {
        titikEvaluasi.add(new double[] {x, y});
        repaint();
    }

    public void kosongkan() {
        xTitik = null;
        titikEvaluasi.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (xTitik == null) {
            g.setColor(Color.GRAY);
            g.drawString("Grafik muncul setelah tombol Hitung ditekan", MARGIN, getHeight() / 2);
            return;
        }

        hitungBatas();
        int kiri = MARGIN;
        int kanan = getWidth() - 20;
        int atas = 20;
        int bawah = getHeight() - MARGIN;

        g.setColor(Color.BLACK);
        g.drawRect(kiri, atas, kanan - kiri, bawah - atas);
        for (int i = 0; i <= 4; i++) {
            double nx = xMin + (xMax - xMin) * i / 4;
            double ny = yMin + (yMax - yMin) * i / 4;
            g.drawString(NumberFormatter.teks(nx), keLayarX(nx) - 10, bawah + 15);
            g.drawString(NumberFormatter.teks(ny), 2, keLayarY(ny) + 4);
        }

        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < knot.length; i++) {
            g.drawLine(keLayarX(knot[i]), atas, keLayarX(knot[i]), bawah);
        }

        // kurva hasilnya
        g.setColor(Color.BLUE);
        for (int i = 0; i < xKurva.length - 1; i++) {
            g.drawLine(keLayarX(xKurva[i]), keLayarY(yKurva[i]), keLayarX(xKurva[i + 1]), keLayarY(yKurva[i + 1]));
        }

        // titik data
        g.setColor(Color.RED);
        for (int i = 0; i < xTitik.length; i++) {
            g.fillOval(keLayarX(xTitik[i]) - 4, keLayarY(yTitik[i]) - 4, 8, 8);
        }

        // titik evaluasi
        g.setColor(new Color(0, 150, 0));
        for (double[] t : titikEvaluasi) {
            g.fillOval(keLayarX(t[0]) - 4, keLayarY(t[1]) - 4, 8, 8);
        }
    }

    private void hitungBatas() {
        xMin = xTitik[0];
        xMax = xTitik[0];
        yMin = yTitik[0];
        yMax = yTitik[0];
        for (int i = 0; i < xKurva.length; i++) {
            xMin = Math.min(xMin, xKurva[i]);
            xMax = Math.max(xMax, xKurva[i]);
            yMin = Math.min(yMin, yKurva[i]);
            yMax = Math.max(yMax, yKurva[i]);
        }
        for (int i = 0; i < xTitik.length; i++) {
            yMin = Math.min(yMin, yTitik[i]);
            yMax = Math.max(yMax, yTitik[i]);
        }
        for (double[] t : titikEvaluasi) {
            xMin = Math.min(xMin, t[0]);
            xMax = Math.max(xMax, t[0]);
            yMin = Math.min(yMin, t[1]);
            yMax = Math.max(yMax, t[1]);
        }
        // agar tidak bagi nol kalo semua nilainya sama
        if (xMax == xMin) {
            xMax = xMin + 1;
        }
        if (yMax == yMin) {
            yMax = yMin + 1;
        }
    }

    private int keLayarX(double x) {
        int kiri = MARGIN;
        int kanan = getWidth() - 20;
        return kiri + (int) ((x - xMin) / (xMax - xMin) * (kanan - kiri));
    }

    private int keLayarY(double y) {
        int atas = 20;
        int bawah = getHeight() - MARGIN;
        return bawah - (int) ((y - yMin) / (yMax - yMin) * (bawah - atas));
    }
}
