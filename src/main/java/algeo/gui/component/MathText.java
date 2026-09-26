package algeo.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import algeo.gui.theme.Theme;

/**
 * Menggambar satu baris teks dengan notasi matematika sederhana ala LaTeX.
 * Notasi yang dikenali: x_{1} (subscript), x^{2} (superscript), \frac{a}{b} (pecahan).
 * Pada gaya RUMUS, huruf tunggal (variabel) ditulis miring dengan font serif seperti LaTeX.
 */
public class MathText extends JComponent {

    public enum Gaya { RUMUS, TEKS }

    private static final Color WARNA_TEKS = new Color(0x7A4A60);

    private final Gaya gaya;
    private final float ukuran;
    private final Deret isi;
    private int posisi; // posisi baca saat mengurai markup

    public MathText(String markup, Gaya gaya) {
        this.gaya = gaya;
        this.ukuran = (gaya == Gaya.RUMUS) ? 17f : 13.5f;
        this.posisi = 0;
        this.isi = uraiDeret(markup, false);
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);
    }

    @Override
    public Dimension getPreferredSize() {
        Ukuran u = isi.ukur(ukuran);
        return new Dimension(u.lebar + 4, u.naik + u.turun + 6);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.haluskan(g2);
        g2.setColor(gaya == Gaya.RUMUS ? Theme.TEKS : WARNA_TEKS);
        Ukuran u = isi.ukur(ukuran);
        isi.gambar(g2, 2, u.naik + 3, ukuran);
        g2.dispose();
    }

    // ===================== pengurai markup =====================

    /** Membaca markup sampai habis (atau sampai '}' kalau sedang di dalam kurung kurawal). */
    private Deret uraiDeret(String s, boolean dalamKurung) {
        Deret deret = new Deret();
        StringBuilder teks = new StringBuilder();
        while (posisi < s.length()) {
            char c = s.charAt(posisi);
            if (dalamKurung && c == '}') {
                posisi++;
                break;
            }
            if ((c == '_' || c == '^') && posisi + 1 < s.length()) {
                simpanTeks(deret, teks);
                posisi++;
                Deret argumen = uraiArgumen(s);
                deret.tambahIndeks(argumen, c == '^');
            } else if (s.startsWith("\\frac{", posisi)) {
                simpanTeks(deret, teks);
                posisi += "\\frac{".length();
                Deret atas = uraiDeret(s, true);
                posisi++; // lewati '{' pembuka penyebut
                Deret bawah = uraiDeret(s, true);
                deret.anak.add(new Pecahan(atas, bawah));
            } else {
                teks.append(c);
                posisi++;
            }
        }
        simpanTeks(deret, teks);
        return deret;
    }

    private Deret uraiArgumen(String s) {
        if (s.charAt(posisi) == '{') {
            posisi++;
            return uraiDeret(s, true);
        }
        Deret satu = new Deret();
        satu.anak.add(new Teks(String.valueOf(s.charAt(posisi))));
        posisi++;
        return satu;
    }

    private void simpanTeks(Deret deret, StringBuilder teks) {
        if (teks.length() > 0) {
            deret.anak.add(new Teks(teks.toString()));
            teks.setLength(0);
        }
    }

    // ===================== model kotak untuk menggambar =====================

    private static class Ukuran {
        int lebar;
        int naik;   // tinggi di atas garis dasar
        int turun;  // tinggi di bawah garis dasar
    }

    private abstract static class Simpul {
        abstract Ukuran ukur(float s);
        abstract void gambar(Graphics2D g2, int x, int garisDasar, float s);
    }

    /** Beberapa simpul berjajar mendatar. */
    private class Deret extends Simpul {
        final List<Simpul> anak = new ArrayList<>();

        /** Subscript/superscript menempel pada simpul sebelumnya; _{a}^{b} digabung jadi satu. */
        void tambahIndeks(Deret argumen, boolean atas) {
            Simpul terakhir = anak.isEmpty() ? null : anak.get(anak.size() - 1);
            if (terakhir instanceof Indeks) {
                Indeks idx = (Indeks) terakhir;
                if (atas && idx.atas == null) { idx.atas = argumen; return; }
                if (!atas && idx.bawah == null) { idx.bawah = argumen; return; }
            }
            Indeks baru = new Indeks();
            if (atas) baru.atas = argumen;
            else baru.bawah = argumen;
            anak.add(baru);
        }

        @Override
        Ukuran ukur(float s) {
            Ukuran u = new Ukuran();
            for (Simpul a : anak) {
                Ukuran ua = a.ukur(s);
                u.lebar += ua.lebar;
                u.naik = Math.max(u.naik, ua.naik);
                u.turun = Math.max(u.turun, ua.turun);
            }
            return u;
        }

        @Override
        void gambar(Graphics2D g2, int x, int garisDasar, float s) {
            for (Simpul a : anak) {
                a.gambar(g2, x, garisDasar, s);
                x += a.ukur(s).lebar;
            }
        }
    }

    /** Teks biasa. Pada gaya RUMUS, huruf tunggal dibuat miring (variabel). */
    private class Teks extends Simpul {
        final List<String> potongan = new ArrayList<>();

        Teks(String teks) {
            // pisahkan per kelompok huruf dan bukan huruf
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < teks.length(); i++) {
                char c = teks.charAt(i);
                if (sb.length() > 0 && Character.isLetter(c) != Character.isLetter(sb.charAt(0))) {
                    potongan.add(sb.toString());
                    sb.setLength(0);
                }
                sb.append(c);
            }
            if (sb.length() > 0) potongan.add(sb.toString());
        }

        private Font font(String p, float s) {
            if (gaya == Gaya.TEKS) {
                return Theme.BIASA.deriveFont(s);
            }
            boolean variabel = p.length() == 1 && Character.isLetter(p.charAt(0));
            return new Font(Font.SERIF, variabel ? Font.ITALIC : Font.PLAIN, 1).deriveFont(s);
        }

        @Override
        Ukuran ukur(float s) {
            Ukuran u = new Ukuran();
            for (String p : potongan) {
                FontMetrics fm = getFontMetrics(font(p, s));
                u.lebar += fm.stringWidth(p);
                u.naik = Math.max(u.naik, fm.getAscent());
                u.turun = Math.max(u.turun, fm.getDescent());
            }
            return u;
        }

        @Override
        void gambar(Graphics2D g2, int x, int garisDasar, float s) {
            for (String p : potongan) {
                Font f = font(p, s);
                g2.setFont(f);
                g2.drawString(p, x, garisDasar);
                x += getFontMetrics(f).stringWidth(p);
            }
        }
    }

    /** Subscript dan/atau superscript. */
    private static class Indeks extends Simpul {
        Deret bawah;
        Deret atas;

        @Override
        Ukuran ukur(float s) {
            float k = s * 0.7f;
            Ukuran u = new Ukuran();
            if (atas != null) {
                Ukuran ua = atas.ukur(k);
                u.lebar = Math.max(u.lebar, ua.lebar);
                u.naik = Math.max(u.naik, ua.naik + Math.round(s * 0.42f));
            }
            if (bawah != null) {
                Ukuran ub = bawah.ukur(k);
                u.lebar = Math.max(u.lebar, ub.lebar);
                u.turun = Math.max(u.turun, ub.turun + Math.round(s * 0.25f));
            }
            u.lebar += 1;
            return u;
        }

        @Override
        void gambar(Graphics2D g2, int x, int garisDasar, float s) {
            float k = s * 0.7f;
            if (atas != null) atas.gambar(g2, x, garisDasar - Math.round(s * 0.42f), k);
            if (bawah != null) bawah.gambar(g2, x, garisDasar + Math.round(s * 0.25f), k);
        }
    }

    /** Pecahan dengan garis mendatar. */
    private static class Pecahan extends Simpul {
        final Deret atas;
        final Deret bawah;

        Pecahan(Deret atas, Deret bawah) {
            this.atas = atas;
            this.bawah = bawah;
        }

        @Override
        Ukuran ukur(float s) {
            float k = s * 0.85f;
            Ukuran ua = atas.ukur(k);
            Ukuran ub = bawah.ukur(k);
            int sumbu = Math.round(s * 0.3f);
            Ukuran u = new Ukuran();
            u.lebar = Math.max(ua.lebar, ub.lebar) + 8;
            u.naik = sumbu + 3 + ua.naik + ua.turun;
            u.turun = ub.naik + ub.turun + 3 - sumbu;
            return u;
        }

        @Override
        void gambar(Graphics2D g2, int x, int garisDasar, float s) {
            float k = s * 0.85f;
            Ukuran ua = atas.ukur(k);
            Ukuran ub = bawah.ukur(k);
            int lebar = Math.max(ua.lebar, ub.lebar) + 8;
            int sumbu = garisDasar - Math.round(s * 0.3f);
            g2.drawLine(x + 2, sumbu, x + lebar - 2, sumbu);
            atas.gambar(g2, x + (lebar - ua.lebar) / 2, sumbu - 3 - ua.turun, k);
            bawah.gambar(g2, x + (lebar - ub.lebar) / 2, sumbu + 3 + ub.naik, k);
        }
    }
}