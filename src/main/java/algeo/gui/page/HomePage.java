package algeo.gui.page;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

import algeo.gui.MainFrame;
import algeo.gui.component.CardPanel;
import algeo.gui.component.FeatureTile;
import algeo.gui.component.PixelButton;
import algeo.gui.theme.Theme;

public class HomePage extends JPanel {

    private static final String[][] FITUR = {
        {"SPL", "Gauss, Gauss-Jordan, matriks balikan, dan Cramer", "4 METODE", "SPL"},
        {"DETERMINAN", "Reduksi baris (OBE) dan ekspansi kofaktor", "2 METODE", "DET"},
        {"BALIKAN", "Augmentasi [A | I] dan metode adjoin", "2 METODE", "INV"},
        {"INTERPOLASI", "Polinom P(x) yang melalui semua titik sampel", "+ GRAFIK", "INTERP"},
        {"SPLINE", "Natural cubic spline, satu polinom kubik per segmen", "+ GRAFIK", "SPLINE"},
        {"REGRESI", "Regresi spline dengan truncated power basis", "+ GRAFIK", "REGRESI"},
    };

    private int detak = 0;

    public HomePage(MainFrame frame) {
        super(new BorderLayout(0, 10));
        setBackground(Theme.LATAR);

        JPanel gridTile = new JPanel(new GridLayout(2, 3, 22, 22));
        gridTile.setOpaque(false);
        for (String[] f : FITUR) {
            FeatureTile tile = new FeatureTile(f[0], f[1], f[2]);
            tile.addActionListener(e -> frame.bukaHalaman(f[3]));
            gridTile.add(tile);
        }

        PixelButton tombolKeluar = new PixelButton("KELUAR", Theme.BIRU, Theme.BIRU_TUA);
        tombolKeluar.setFont(Theme.pixel(11f));
        tombolKeluar.addActionListener(e -> System.exit(0));
        JPanel barisBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barisBawah.setOpaque(false);
        barisBawah.add(tombolKeluar);

        CardPanel menu = new CardPanel("PILIH FITUR", new BorderLayout(0, 18));
        menu.setBorder(BorderFactory.createEmptyBorder(62, 32, 34, 36));
        menu.add(gridTile, BorderLayout.CENTER);
        menu.add(barisBawah, BorderLayout.SOUTH);

        Judul judul = new Judul();
        add(judul, BorderLayout.NORTH);
        add(menu, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int samping = getWidth() * 8 / 100;
                setBorder(BorderFactory.createEmptyBorder(10, samping, 36, samping));
                revalidate();
            }
        });

        new Timer(33, e -> {
            detak++;
            if (isShowing()) judul.repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(255, 255, 255, 140));
        for (int x = 20; x < getWidth(); x += 40) {
            for (int y = 20; y < getHeight(); y += 40) {
                g.fillOval(x, y, 4, 4);
            }
        }
    }

    private class Judul extends JComponent {

        Judul() {
            setPreferredSize(new Dimension(800, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.haluskan(g2);
            int w = getWidth();

            double[] geserX = {-130, 0, 130};
            double[] ukuran = {26, 36, 26};
            for (int i = 0; i < 3; i++) {
                double naik = Math.sin(detak * 0.08 + i * 1.3) * 8;
                Shape s = Theme.bintang(w / 2.0 + geserX[i], 52 + naik, ukuran[i], ukuran[i] * 0.45);
                g2.setColor(Theme.KUNING);
                g2.fill(s);
                g2.setColor(Theme.KUNING_TUA);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(s);
            }

            String teks = "ALGEO CALCULATOR";
            g2.setFont(Theme.pixel(46f));
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(teks)) / 2;
            int y = 146;
            g2.setColor(Theme.PINK_GARIS);
            for (int dx = -3; dx <= 3; dx += 3) {
                for (int dy = -3; dy <= 3; dy += 3) {
                    g2.drawString(teks, x + dx, y + dy);
                }
            }
            g2.drawString(teks, x + 4, y + 7);
            g2.setColor(Color.WHITE);
            g2.drawString(teks, x, y);

            String sub = "by profil2spartans";
            g2.setFont(Theme.BIASA_TEBAL.deriveFont(15f));
            fm = g2.getFontMetrics();
            g2.setColor(Theme.TEKS);
            g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, y + 36);
            g2.dispose();
        }
    }
}
