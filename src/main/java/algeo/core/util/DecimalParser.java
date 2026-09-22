package algeo.core.util;

public class DecimalParser {
    /**
     * Mengubah teks jadi angka double. Titik (.) dan koma (,) sama-sama diterima sebagai desimal. Hanya karakter 0-9 . , + - e E yang boleh muncul, jadi teks seperti "NaN", "Infinity", "1f", atau "0x10" ditolak
     * @param input teks angka
     * @return nilai angka
     * @throws NumberFormatException jika teks bukan angka yang valid atau terlalu besar
     */
    public static double parse(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Input kosong, harap masukkan angka");
        }
        String asli = input.trim();
        String s = asli.replace(",", ".");

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean boleh = (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E';
            if (!boleh) {
                throw new NumberFormatException("'" + asli + "' bukan angka yang valid");
            }
        }

        double hasil;
        try {
            hasil = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("'" + asli + "' bukan angka yang valid");
        }

        if (Double.isInfinite(hasil)) {
            throw new NumberFormatException("Angka '" + asli + "' terlalu besar");
        }
        return hasil;
    }
}