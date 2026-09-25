package algeo.core.spl;
import algeo.core.util.NumberFormatter;

public class SPLFormatter {
    /**
     * Membuat teks solusi parametrik dimana digunakan bersama oleh Gauss dan Gauss-Jordan
     * Variabel bebas diberi nama t1, t2, ... sesuai urutan kolomnya
     * @param x x[j][c] = koefisien t(c+1) pada variabel x(j+1), kolom terakhir x[j][bebas] = konstanta
     * @param adaPivot adaPivot[j] = true jika kolom j punya pivot (bukan variabel bebas)
     * @return teks solusi, satu variabel per baris
     */
    public static String parametrik(double[][] x, boolean[] adaPivot) {
        int n = x.length;
        int bebas = x[0].length - 1;
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < n; j++) {
            sb.append("x").append(j + 1).append(" =");
            boolean adaSuku = false;
            double konstanta = NumberFormatter.round(x[j][bebas]);
            if (konstanta != 0) {
                sb.append(" ").append(NumberFormatter.teks(konstanta));
                adaSuku = true;
            }

            for (int c = 0; c < bebas; c++) {
                double koef = NumberFormatter.round(x[j][c]);
                if (koef == 0) continue;
                if (adaSuku) {
                    sb.append(koef < 0 ? " - " : " + ");
                } else {
                    sb.append(koef < 0 ? " -" : " ");
                }
                if (Math.abs(koef) != 1) {
                    sb.append(NumberFormatter.teks(Math.abs(koef)));
                }
                sb.append("t").append(c + 1);
                adaSuku = true;
            }
            if (!adaSuku) sb.append(" 0");
            sb.append("\n");
        }
        sb.append("dengan ");
        int k = 0;
        for (int j = 0; j < n; j++) {
            if (!adaPivot[j]) {
                if (k > 0) sb.append(", ");
                k++;
                sb.append("t").append(k).append(" = x").append(j + 1);
            }
        }
        sb.append(" (bilangan real bebas)");
        return sb.toString();
    }
}