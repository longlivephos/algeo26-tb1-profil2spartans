package algeo.gui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import algeo.core.util.NumberFormatter;

/**
 * Mengubah teks langkah dari library menjadi notasi matematika yang dapat digambar MathText.
 * Contoh: "Baris 2 = Baris 2 - (3) x Baris 1" menjadi "R_{2} ← R_{2} − 3 · R_{1}".
 * Teks library tidak diubah; konversi ini hanya untuk tampilan GUI.
 */
public final class StepFormatter {

    private StepFormatter() {}

    /** Satu baris tampilan: rumus (serif, besar) atau keterangan (sans, kecil). */
    public static class Baris {
        public final MathText.Gaya gaya;
        public final String markup;

        Baris(MathText.Gaya gaya, String markup) {
            this.gaya = gaya;
            this.markup = markup;
        }
    }

    private static final String KALI = " · ";
    private static final String MINUS = "−";
    private static final String PANAH_KIRI = " ← ";

    private static final Pattern TUKAR = Pattern.compile("Tukar baris (\\d+) dengan baris (\\d+)(?: \\((.*)\\))?:?");
    private static final Pattern OPERASI = Pattern.compile("Baris (\\d+) = Baris \\1 - \\((\\S+)\\) x Baris (\\d+):?");
    private static final Pattern BAGI = Pattern.compile("Baris (\\d+) = Baris \\1 / (\\S+?):?");
    private static final Pattern KOFAKTOR_ADJOIN = Pattern.compile(
            "C\\[(\\d+)\\]\\[(\\d+)\\] = \\(([+-])\\) \\* det\\(Minor\\) = (\\S+)");
    private static final Pattern KOFAKTOR_DET = Pattern.compile(
            "a\\[(\\d+)\\]\\[(\\d+)\\] = (\\S+)\\s*\\|\\s*det\\(Minor\\) = (\\S+)\\s*\\|\\s*C\\[\\d+\\]\\[\\d+\\] = (\\S+)");
    private static final Pattern DILEWATI = Pattern.compile("Elemen a\\[(\\d+)\\]\\[(\\d+)\\] = (\\S+), kofaktor dilewati\\.");
    private static final Pattern CRAMER = Pattern.compile(
            "det\\(A(\\d+)\\) = (\\S+), x(\\d+) = det\\(A\\d+\\) / det\\(A\\) = (\\S+)");
    private static final Pattern RUMUS_AKHIR = Pattern.compile("Rumus Akhir: det = (.+)");
    private static final Pattern DIAGONAL = Pattern.compile(
            "Determinan = hasil kali diagonal \\(dengan tanda dari tukar baris\\) = (\\S+)");
    private static final Pattern JUMLAH_BASIS = Pattern.compile(
            "Jumlah titik N = (\\d+), jumlah knot K = (\\d+), derajat = (\\d+), jumlah basis M = (\\d+)");
    private static final Pattern TRANSFORMASI = Pattern.compile("Transformasi u = \\(x - (\\S+)\\) / (\\S+) (.*)");
    private static final Pattern KNOT = Pattern.compile("Knot x_(\\d+) \\((\\S+)\\) -> S''\\(x\\) = 2 \\* c_(\\d+) = (\\S+)");
    private static final Pattern SEGMEN = Pattern.compile("Segmen (\\d+) \\[(\\S+) <= x <= (\\S+)\\]:");
    private static final Pattern KOEFISIEN_U = Pattern.compile("Koefisien dalam u \\(.*?\\): (.*)");
    private static final Pattern SOLUSI_INVERS = Pattern.compile("x = A\\^-1 \\* b, (.*)");
    private static final Pattern KALI_VANDERMONDE = Pattern.compile("Mengalikan A\\^-1 dengan Matriks Y.*");
    private static final Pattern PERKALIAN_KOFAKTOR = Pattern.compile("\\((\\S+) \\* (\\S+)\\)");

    /** Mengubah satu baris teks langkah menjadi satu atau beberapa baris tampilan. */
    public static List<Baris> format(String baris) {
        String s = baris.trim();
        List<Baris> hasil = new ArrayList<>();
        Matcher m;

        if ((m = TUKAR.matcher(s)).matches()) {
            rumus(hasil, "R_{" + m.group(1) + "} ↔ R_{" + m.group(2) + "}");
            if (m.group(3) != null) teks(hasil, umum(m.group(3)));
        } else if ((m = OPERASI.matcher(s)).matches()) {
            double k = Double.parseDouble(m.group(2));
            String i = m.group(1);
            String j = m.group(3);
            String faktor = (Math.abs(k) == 1) ? "" : NumberFormatter.teks(Math.abs(k)) + KALI;
            String tanda = (k < 0) ? " + " : " " + MINUS + " ";
            rumus(hasil, "R_{" + i + "}" + PANAH_KIRI + "R_{" + i + "}" + tanda + faktor + "R_{" + j + "}");
        } else if ((m = BAGI.matcher(s)).matches()) {
            double p = Double.parseDouble(m.group(2));
            String i = m.group(1);
            String pecahan = (p < 0 ? MINUS : "") + "\\frac{1}{" + NumberFormatter.teks(Math.abs(p)) + "}";
            rumus(hasil, "R_{" + i + "}" + PANAH_KIRI + pecahan + " R_{" + i + "}");
        } else if ((m = KOFAKTOR_ADJOIN.matcher(s)).matches()) {
            String ij = indeks(m.group(1), m.group(2));
            rumus(hasil, "C_{" + ij + "} = (" + MINUS + "1)^{" + m.group(1) + "+" + m.group(2) + "}" + KALI
                    + "det(M_{" + ij + "}) = " + angka(m.group(4)));
        } else if ((m = KOFAKTOR_DET.matcher(s)).matches()) {
            String ij = indeks(m.group(1), m.group(2));
            rumus(hasil, "a_{" + ij + "} = " + angka(m.group(3)) + ",     det(M_{" + ij + "}) = " + angka(m.group(4)));
            rumus(hasil, "C_{" + ij + "} = (" + MINUS + "1)^{" + m.group(1) + "+" + m.group(2) + "}" + KALI
                    + "det(M_{" + ij + "}) = " + angka(m.group(5)));
        } else if ((m = DILEWATI.matcher(s)).matches()) {
            rumus(hasil, "a_{" + indeks(m.group(1), m.group(2)) + "} = " + angka(m.group(3)));
            teks(hasil, "elemen bernilai nol, jadi kofaktornya tidak perlu dihitung");
        } else if ((m = CRAMER.matcher(s)).matches()) {
            String k = m.group(1);
            rumus(hasil, "det(A_{" + k + "}) = " + angka(m.group(2)));
            rumus(hasil, "x_{" + k + "} = \\frac{det(A_{" + k + "})}{det(A)} = " + angka(m.group(4)));
        } else if ((m = RUMUS_AKHIR.matcher(s)).matches()) {
            teks(hasil, "Jumlahkan hasil kali setiap elemen baris acuan dengan kofaktornya:");
            rumus(hasil, "det(A) = " + perkalianKofaktor(m.group(1)));
        } else if ((m = DIAGONAL.matcher(s)).matches()) {
            teks(hasil, "Determinan = hasil kali elemen diagonal, tandanya berganti setiap tukar baris:");
            rumus(hasil, "det(A) = " + angka(m.group(1)));
        } else if ((m = JUMLAH_BASIS.matcher(s)).matches()) {
            rumus(hasil, "N = " + m.group(1) + ",     K = " + m.group(2) + ",     derajat d = " + m.group(3)
                    + ",     M = d + 1 + K = " + m.group(4));
        } else if ((m = TRANSFORMASI.matcher(s)).matches()) {
            rumus(hasil, "u = \\frac{x " + MINUS + " " + angka(m.group(1)) + "}{" + angka(m.group(2)) + "}");
            teks(hasil, m.group(3));
        } else if ((m = KNOT.matcher(s)).matches()) {
            rumus(hasil, "S''(x_{" + m.group(1) + "}) = 2c_{" + m.group(3) + "} = " + angka(m.group(4))
                    + ",     x_{" + m.group(1) + "} = " + angka(m.group(2)));
        } else if ((m = SEGMEN.matcher(s)).matches()) {
            rumus(hasil, "Segmen " + m.group(1) + ":   " + angka(m.group(2)) + " ≤ x ≤ " + angka(m.group(3)));
        } else if ((m = KOEFISIEN_U.matcher(s)).matches()) {
            teks(hasil, "Koefisien dalam variabel u (solusi SPL di atas):");
            String isi = m.group(1).replaceAll(";\\s*$", "").replace(";", ",   ");
            rumus(hasil, umum(isi));
        } else if (KALI_VANDERMONDE.matcher(s).matches()) {
            teks(hasil, "Koefisien polinom didapat dari invers Vandermonde dikali vektor y:");
            rumus(hasil, "C = A^{" + MINUS + "1}" + KALI + "Y");
        } else if ((m = SOLUSI_INVERS.matcher(s)).matches()) {
            rumus(hasil, "x = A^{" + MINUS + "1}" + KALI + "b");
            teks(hasil, m.group(1));
        } else {
            formatUmum(hasil, s);
        }
        return hasil;
    }

    /** Label kategori langkah untuk chip di kartu, kosong kalau tidak ada yang cocok. */
    public static String kategori(String langkah) {
        String s = langkah.trim();
        if (s.startsWith("Tukar baris")) return "TUKAR BARIS";
        if (OPERASI.matcher(s.split("\n")[0]).matches() || BAGI.matcher(s.split("\n")[0]).matches()) return "OPERASI BARIS";
        if (s.contains("det(Minor)") || s.contains("kofaktor") || s.startsWith("Rumus Akhir")) return "KOFAKTOR";
        if (s.matches("(?s)x\\d+ = .*") || s.startsWith("Substitusi")) return "SUBSTITUSI MUNDUR";
        if (CRAMER.matcher(s).matches()) return "KAIDAH CRAMER";
        if (s.startsWith("Matriks") || s.startsWith("Vektor") || s.startsWith("X^T")) return "MATRIKS";
        return "";
    }

    // ===================== aturan umum =====================

    /** Baris tanpa aturan khusus: dipisah menjadi keterangan dan rumus kalau memungkinkan. */
    private static void formatUmum(List<Baris> hasil, String s) {
        String bullet = "";
        if (s.startsWith("- ")) {
            bullet = "•  ";
            s = s.substring(2);
        }
        int titikDua = s.indexOf(": ");
        if (titikDua > 0 && s.substring(titikDua).contains("=")) {
            teks(hasil, umum(s.substring(0, titikDua + 1)));
            rumus(hasil, bullet + umum(s.substring(titikDua + 2)));
        } else if (terlihatRumus(s)) {
            rumus(hasil, bullet + umum(s));
        } else {
            teks(hasil, bullet + umum(s));
        }
    }

    // rumus kalau ada "=" dan ruas kirinya hanya satu "kata", misalnya "x4 = 2" atau "P(x) = ..."
    private static boolean terlihatRumus(String s) {
        int sama = s.indexOf('=');
        if (sama <= 0) return false;
        String kiri = s.substring(0, sama).trim();
        return !kiri.isEmpty() && kiri.split("\\s+").length == 1;
    }

    /** Konversi notasi yang berlaku untuk semua baris. */
    static String umum(String s) {
        // angka desimal dibulatkan maksimal 3 angka di belakang koma
        s = gantiSemua(s, Pattern.compile("(?<![\\w.])-?\\d+\\.\\d+(E-?\\d+)?"), m -> angka(m.group()));
        // koefisien 1 di depan variabel tidak ditulis, lalu perkalian angka-variabel ditulis berdempet
        s = s.replaceAll("(?<![\\d.])1\\*(?=[A-Za-z(])", "");
        s = s.replaceAll("(\\d)\\*(?=[A-Za-z(])", "$1");
        s = s.replace(" * ", KALI).replace("*", KALI);
        // indeks: a[1][2] -> a_{12}, h[0] -> h_{0}, c_0 -> c_{0}, x1 -> x_{1}
        s = gantiSemua(s, Pattern.compile("(?<![A-Za-z])([A-Za-z])\\[(\\d+)\\]\\[(\\d+)\\]"),
                m -> m.group(1) + "_{" + indeks(m.group(2), m.group(3)) + "}");
        s = s.replaceAll("(?<![A-Za-z])([A-Za-z])\\[(\\d+)\\]", "$1_{$2}");
        s = s.replaceAll("(?<![A-Za-z])([A-Za-z])_(\\d+)", "$1_{$2}");
        s = s.replaceAll("(?<![A-Za-z])([A-Za-z])(\\d+)\\b", "$1_{$2}");
        // pangkat: ^-1, ^2, ^T, dan (x - c)_+^3
        s = s.replace("_+^", "_{+}^");
        s = s.replaceAll("\\^(-?[A-Za-z0-9]+)", "^{$1}");
        // simbol
        s = s.replace("<=", "≤").replace(">=", "≥").replace("->", "→");
        s = s.replace(" - ", " " + MINUS + " ");
        s = s.replaceAll("(^|[\\s(=,{\\[])-(?=[\\w(])", "$1" + MINUS);
        return s;
    }

    // "(2.0 * 5.0) + (3.0 * -3.0)" -> "2 · 5 + 3 · (−3)"
    private static String perkalianKofaktor(String s) {
        String hasil = gantiSemua(s, PERKALIAN_KOFAKTOR, m -> kurungNegatif(m.group(1)) + KALI + kurungNegatif(m.group(2)));
        return umum(hasil);
    }

    private static String kurungNegatif(String x) {
        String a = angka(x);
        return a.startsWith(MINUS) ? "(" + a + ")" : a;
    }

    /** Angka dibulatkan maksimal 3 desimal, tanda minus diganti minus matematika. */
    private static String angka(String x) {
        try {
            String t = NumberFormatter.teks(Double.parseDouble(x.replace(MINUS, "-")));
            return t.startsWith("-") ? MINUS + t.substring(1) : t;
        } catch (NumberFormatException e) {
            return x;
        }
    }

    // indeks ganda ditulis rapat (12), atau dipisah koma kalau ada yang dua digit (1,12)
    private static String indeks(String i, String j) {
        return (i.length() > 1 || j.length() > 1) ? i + "," + j : i + j;
    }

    private static void rumus(List<Baris> hasil, String markup) {
        hasil.add(new Baris(MathText.Gaya.RUMUS, markup));
    }

    private static void teks(List<Baris> hasil, String markup) {
        hasil.add(new Baris(MathText.Gaya.TEKS, markup));
    }

    private interface Pengganti {
        String ganti(Matcher m);
    }

    private static String gantiSemua(String s, Pattern pola, Pengganti pengganti) {
        Matcher m = pola.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(pengganti.ganti(m)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
