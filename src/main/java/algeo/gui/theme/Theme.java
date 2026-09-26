package algeo.gui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Palet warna, font, dan helper tampilan yang dipakai seluruh komponen GUI.
 */
public final class Theme {

    private Theme() {}

    public static final Color LATAR = new Color(0xDDF1F5);
    public static final Color PINK = new Color(0xF48FB8);
    public static final Color PINK_MUDA = new Color(0xFBC9DC);
    public static final Color PINK_TUA = new Color(0xD9487F);
    public static final Color PINK_GARIS = new Color(0xB8295F);
    public static final Color KUNING = new Color(0xFFD23F);
    public static final Color KUNING_TUA = new Color(0xD99A00);
    public static final Color BIRU = new Color(0x7FC8E8);
    public static final Color BIRU_TUA = new Color(0x3C8DB5);
    public static final Color TEKS = new Color(0x5A1E3A);
    public static final Color SOROT_UBAH = new Color(0xFFE680);   // sel yang berubah pada langkah OBE
    public static final Color SOROT_SUMBER = new Color(0xC4E8F5); // baris acuan pada langkah OBE
    public static final Color MERAH_ERROR = new Color(0xFFB3B3);

    public static final Font ANGKA = new Font(Font.MONOSPACED, Font.BOLD, 13);
    public static final Font BIASA = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font BIASA_TEBAL = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    // font pixel "Press Start 2P" (lisensi OFL) disimpan di src/main/resources/fonts
    private static final Font PIXEL = muatFontPixel();

    private static Font muatFontPixel() {
        try (InputStream in = Theme.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf")) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            // file font tidak ketemu atau rusak: pakai font bawaan supaya program tetap jalan
            return new Font(Font.MONOSPACED, Font.BOLD, 12);
        }
    }

    /** Font pixel untuk judul dan tombol. */
    public static Font pixel(float ukuran) {
        return PIXEL.deriveFont(ukuran);
    }

    public static JLabel label(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(BIASA_TEBAL);
        l.setForeground(TEKS);
        return l;
    }

    public static JLabel judul(String teks, float ukuran) {
        JLabel l = new JLabel(teks);
        l.setFont(pixel(ukuran));
        l.setForeground(TEKS);
        return l;
    }

    /** Kotak isian putih dengan garis pink, dipakai untuk text field, spinner, combo box. */
    public static void gayakan(JComponent c) {
        c.setFont(ANGKA);
        c.setBackground(Color.WHITE);
        c.setForeground(TEKS);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PINK_GARIS, 2),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    }

    public static void haluskan(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Bentuk bintang 5 sudut dengan pusat (cx, cy). */
    public static Path2D bintang(double cx, double cy, double rLuar, double rDalam) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? rLuar : rDalam;
            double sudut = -Math.PI / 2 + i * Math.PI / 5;
            double x = cx + r * Math.cos(sudut);
            double y = cy + r * Math.sin(sudut);
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }
}
