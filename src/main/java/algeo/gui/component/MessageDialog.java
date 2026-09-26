package algeo.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import algeo.gui.theme.Theme;

/**
 * Dialog pesan (error dan informasi) yang mengikuti tema aplikasi.
 */
public class MessageDialog {

    private MessageDialog() {}

    public static void tampilkan(Component induk, String judul, String pesan) {
        Window jendela = SwingUtilities.getWindowAncestor(induk);
        JDialog dialog = new JDialog(jendela, judul, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JTextArea teks = new JTextArea(pesan);
        teks.setFont(Theme.BIASA_TEBAL);
        teks.setForeground(Theme.TEKS);
        teks.setLineWrap(true);
        teks.setWrapStyleWord(true);
        teks.setEditable(false);
        teks.setOpaque(false);
        teks.setColumns(28);
        teks.setFocusable(false);

        PixelButton tombolOk = new PixelButton("OK", Theme.KUNING, Theme.KUNING_TUA);
        tombolOk.setForeground(Theme.TEKS);
        tombolOk.addActionListener(e -> dialog.dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bawah.setOpaque(false);
        bawah.add(tombolOk);

        CardPanel isi = new CardPanel(judul, new BorderLayout(0, 12));
        isi.add(teks, BorderLayout.CENTER);
        isi.add(bawah, BorderLayout.SOUTH);

        dialog.setContentPane(isi);
        dialog.getRootPane().setDefaultButton(tombolOk);
        dialog.pack();
        dialog.setLocationRelativeTo(jendela);
        dialog.setVisible(true);
    }
}
