package algeo.gui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import algeo.core.matrix.Matrix;
import algeo.core.matrix.MatrixException;
import algeo.core.util.DecimalParser;

public class MatrixInput extends JPanel {

    private final int maksBaris;
    private final int maksKolom;
    private final boolean adaKolomB;   // true untuk SPL
    private final boolean kolomTetap;  // true untuk titik (x, y)
    private final String[] judulKolomTetap;

    private JSpinner spinBaris;
    private JSpinner spinKolom;
    private JPanel panelKotak = new JPanel();
    private JLabel labelInfo = new JLabel(" ");
    private JTextField[][] kotak = new JTextField[0][0];
    private Matrix matriksFile = null;

    public MatrixInput(String labelBaris, int baris, int maksBaris, String labelKolom, int kolom, int maksKolom, boolean adaKolomB) {
        super(new BorderLayout(0, 5));
        this.maksBaris = maksBaris;
        this.maksKolom = maksKolom;
        this.adaKolomB = adaKolomB;
        this.kolomTetap = false;
        this.judulKolomTetap = null;
        spinBaris = new JSpinner(new SpinnerNumberModel(baris, 1, maksBaris, 1));
        spinKolom = new JSpinner(new SpinnerNumberModel(kolom, 1, maksKolom, 1));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        atas.add(new JLabel(labelBaris));
        atas.add(spinBaris);
        atas.add(new JLabel(labelKolom));
        atas.add(spinKolom);
        susun(atas);
    }

    // input titik
    public MatrixInput(String labelBaris, int baris, int maksBaris, String[] judulKolom) {
        super(new BorderLayout(0, 5));
        this.maksBaris = maksBaris;
        this.maksKolom = judulKolom.length;
        this.adaKolomB = false;
        this.kolomTetap = true;
        this.judulKolomTetap = judulKolom;
        spinBaris = new JSpinner(new SpinnerNumberModel(baris, 1, maksBaris, 1));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        atas.add(new JLabel(labelBaris));
        atas.add(spinBaris);
        susun(atas);
    }

    private void susun(JPanel atas) {
        spinBaris.addChangeListener(e -> buatKotak());
        if (spinKolom != null) {
            spinKolom.addChangeListener(e -> buatKotak());
        }
        JPanel wadahKotak = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wadahKotak.add(panelKotak);

        add(atas, BorderLayout.NORTH);
        add(wadahKotak, BorderLayout.CENTER);
        add(labelInfo, BorderLayout.SOUTH);
        buatKotak();
    }

    private int jumlahKolomKotak() {
        if (kolomTetap) {
            return judulKolomTetap.length;
        }
        int n = (Integer) spinKolom.getValue();
        if (adaKolomB) {
            n = n + 1;
        }
        return n;
    }

    private void buatKotak() {
        matriksFile = null;
        labelInfo.setText(" ");

        int baris = (Integer) spinBaris.getValue();
        int kolom = jumlahKolomKotak();
        JTextField[][] lama = kotak;
        kotak = new JTextField[baris][kolom];

        panelKotak.removeAll();
        panelKotak.setLayout(new GridLayout(baris + 1, kolom, 3, 3));

        for (int j = 0; j < kolom; j++) {
            String judul;
            if (kolomTetap) {
                judul = judulKolomTetap[j];
            } else if (adaKolomB && j == kolom - 1) {
                judul = "b";
            } else if (adaKolomB) {
                judul = "x" + (j + 1);
            } else {
                judul = "k" + (j + 1);
            }
            panelKotak.add(new JLabel(judul, JLabel.CENTER));
        }

        for (int i = 0; i < baris; i++) {
            for (int j = 0; j < kolom; j++) {
                JTextField f = new JTextField(4);
                if (i < lama.length && j < lama[i].length) {
                    f.setText(lama[i][j].getText());
                }
                kotak[i][j] = f;
                panelKotak.add(f);
            }
        }
        panelKotak.revalidate();
        panelKotak.repaint();
    }

    public Matrix bacaMatriks() throws MatrixException {
        if (matriksFile != null) {
            return matriksFile;
        }
        double[][] data = new double[kotak.length][kotak[0].length];
        for (int i = 0; i < kotak.length; i++) {
            for (int j = 0; j < kotak[i].length; j++) {
                String teks = kotak[i][j].getText().trim();
                if (teks.isEmpty()) {
                    throw new MatrixException("Kotak baris " + (i + 1) + " kolom " + (j + 1) + " masih kosong");
                }
                try {
                    data[i][j] = DecimalParser.parse(teks);
                } catch (NumberFormatException e) {
                    throw new MatrixException("Kotak baris " + (i + 1) + " kolom " + (j + 1) + ": " + e.getMessage());
                }
            }
        }
        return new Matrix(data);
    }

    public void muatMatriks(Matrix m) throws MatrixException {
        int baris = m.getRows();
        int kolomData = m.getCols();
        if (adaKolomB) {
            if (m.getCols() < 2) {
                throw new MatrixException("Matriks augmented minimal punya 2 kolom");
            }
            kolomData = m.getCols() - 1;
        }
        if (kolomTetap && m.getCols() != judulKolomTetap.length) {
            throw new MatrixException("Setiap baris harus berisi " + judulKolomTetap.length + " angka");
        }

        if (baris > maksBaris || kolomData > maksKolom) {
            if (kolomTetap) {
                throw new MatrixException("Jumlah titik maksimal " + maksBaris);
            }
            matriksFile = m;
            panelKotak.removeAll();
            panelKotak.revalidate();
            panelKotak.repaint();
            labelInfo.setText("Memakai matriks dari file (" + m.getRows() + " x " + m.getCols() + "), terlalu besar untuk ditampilkan");
            return;
        }

        spinBaris.setValue(baris);
        if (spinKolom != null) {
            spinKolom.setValue(kolomData);
        }
        buatKotak();
        for (int i = 0; i < baris; i++) {
            for (int j = 0; j < m.getCols(); j++) {
                kotak[i][j].setText(teksAngka(m.get(i, j)));
            }
        }
    }

    public void kosongkan() {
        buatKotak();
        for (int i = 0; i < kotak.length; i++) {
            for (int j = 0; j < kotak[i].length; j++) {
                kotak[i][j].setText("");
            }
        }
    }

    // bikin pembulatan angka
    private static String teksAngka(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
