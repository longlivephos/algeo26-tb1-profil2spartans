package algeo.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import algeo.core.util.NumberFormatter;

// satu kotak untuk satu langkah perhitungan: judul, keterangan, dan tabel matriks (kalau ada)
public class LangkahPanel extends JPanel {

    public static final Color KUNING = new Color(255, 240, 170);
    public static final Color ORANYE = new Color(255, 190, 110);
    public static final Color BIRU = new Color(190, 220, 255);
    public static final Color UNGU = new Color(215, 195, 255);
    private double[][] matriks = null;

    public LangkahPanel(String judul, String isi, double[][] matriksSebelum, Color warnaLatar) {
        super(new BorderLayout(0, 5));
        setBackground(warnaLatar);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 150, 180)), BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(labelJudul.getFont().deriveFont(Font.BOLD));
        add(labelJudul, BorderLayout.NORTH);

        String keterangan = "";
        List<double[]> barisMatriks = new ArrayList<>();
        int kolomPemisah = -1;
        String[] semuaBaris = isi.split("\n");
        for (int i = 0; i < semuaBaris.length; i++) {
            double[] angka = bacaBarisMatriks(semuaBaris[i]);
            if (angka != null) {
                barisMatriks.add(angka);
                if (kolomPemisah == -1) {
                    kolomPemisah = cariPemisah(semuaBaris[i]);
                }
            } else if (!semuaBaris[i].trim().isEmpty()) {
                keterangan = keterangan + semuaBaris[i].trim() + "\n";
            }
        }

        if (!barisMatriks.isEmpty() && !samaPanjang(barisMatriks)) {
            keterangan = isi.trim();
            barisMatriks.clear();
        }

        if (!keterangan.trim().isEmpty()) {
            JTextArea teks = new JTextArea(keterangan.trim());
            teks.setEditable(false);
            teks.setOpaque(false);
            teks.setFont(labelJudul.getFont().deriveFont(Font.PLAIN));
            add(teks, BorderLayout.CENTER);
        }

        if (!barisMatriks.isEmpty()) {
            matriks = new double[barisMatriks.size()][];
            for (int i = 0; i < matriks.length; i++) {
                matriks[i] = barisMatriks.get(i);
            }
            boolean[][] selBerubah = cariSelBerubah(matriksSebelum, matriks);
            int diubah = -1;
            int acuan = -1;
            int tukar1 = -1;
            int tukar2 = -1;
            if (keterangan.startsWith("Tukar baris")) {
                tukar1 = ambilNomorBaris(keterangan, "Tukar baris ");
                tukar2 = ambilNomorBaris(keterangan, " dengan baris ");
            } else if (keterangan.startsWith("Baris ") && keterangan.contains(" = Baris ")) {
                diubah = ambilNomorBaris(keterangan, "Baris ");
                acuan = ambilNomorBaris(keterangan, "x Baris ");
            }

            JPanel wadahTabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wadahTabel.setOpaque(false);
            wadahTabel.add(buatTabel(matriks, selBerubah, diubah, acuan, tukar1, tukar2, kolomPemisah));
            add(wadahTabel, BorderLayout.SOUTH);
        }

        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    public double[][] getMatriks() {
        return matriks;
    }

    private static double[] bacaBarisMatriks(String baris) {
        String s = baris.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return null;
        }
        s = s.substring(1, s.length() - 1).replace("|", " ").trim();
        if (s.isEmpty()) {
            return null;
        }
        String[] token = s.split("\\s+");
        double[] hasil = new double[token.length];
        try {
            for (int j = 0; j < token.length; j++) {
                hasil[j] = Double.parseDouble(token[j]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return hasil;
    }

    private static int cariPemisah(String baris) {
        int posisi = baris.indexOf('|');
        if (posisi < 0) {
            return -1;
        }
        String kiri = baris.substring(baris.indexOf('[') + 1, posisi).trim();
        if (kiri.isEmpty()) {
            return 0;
        }
        return kiri.split("\\s+").length;
    }

    private static boolean samaPanjang(List<double[]> baris) {
        for (int i = 1; i < baris.size(); i++) {
            if (baris.get(i).length != baris.get(0).length) {
                return false;
            }
        }
        return true;
    }

    private static boolean[][] cariSelBerubah(double[][] lama, double[][] baru) {
        boolean[][] berubah = new boolean[baru.length][baru[0].length];
        if (lama == null || lama.length != baru.length || lama[0].length != baru[0].length) {
            return berubah;
        }
        for (int i = 0; i < baru.length; i++) {
            for (int j = 0; j < baru[i].length; j++) {
                if (Math.abs(lama[i][j] - baru[i][j]) > 1e-9) {
                    berubah[i][j] = true;
                }
            }
        }
        return berubah;
    }

    private static int ambilNomorBaris(String teks, String kataDepan) {
        int posisi = teks.indexOf(kataDepan);
        if (posisi < 0) {
            return -1;
        }
        String sisa = teks.substring(posisi + kataDepan.length());
        int akhir = 0;
        while (akhir < sisa.length() && Character.isDigit(sisa.charAt(akhir))) {
            akhir++;
        }
        if (akhir == 0) {
            return -1;
        }
        return Integer.parseInt(sisa.substring(0, akhir)) - 1;
    }

    private static JTable buatTabel(double[][] data, boolean[][] selBerubah, int diubah, int acuan, int tukar1, int tukar2, int kolomPemisah) {
        int baris = data.length;
        int kolom = data[0].length;

        DefaultTableModel model = new DefaultTableModel(baris, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (int i = 0; i < baris; i++) {
            for (int j = 0; j < kolom; j++) {
                model.setValueAt(NumberFormatter.teks(data[i][j]), i, j);
            }
        }

        JTable tabel = new JTable(model);
        tabel.setRowHeight(22);
        tabel.setFocusable(false);
        tabel.setRowSelectionAllowed(false);
        tabel.setGridColor(Color.LIGHT_GRAY);
        tabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int j = 0; j < kolom; j++) {
            tabel.getColumnModel().getColumn(j).setPreferredWidth(65);
        }

        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, false, false, row, column);
                setHorizontalAlignment(RIGHT);
                if (row == tukar1 || row == tukar2) {
                    setBackground(UNGU);
                } else if (row == diubah && selBerubah[row][column]) {
                    setBackground(ORANYE);
                } else if (row == diubah) {
                    setBackground(KUNING);
                } else if (row == acuan) {
                    setBackground(BIRU);
                } else {
                    setBackground(Color.WHITE);
                }
                if (kolomPemisah > 0 && column == kolomPemisah) {
                    setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.GRAY));
                } else {
                    setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                }
                return this;
            }
        });
        return tabel;
    }
}
