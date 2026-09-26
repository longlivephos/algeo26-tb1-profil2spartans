package algeo.gui;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import algeo.gui.page.DeterminantPage;
import algeo.gui.page.FeaturePage;
import algeo.gui.page.HomePage;
import algeo.gui.page.InterpolationPage;
import algeo.gui.page.InversePage;
import algeo.gui.page.RegressionPage;
import algeo.gui.page.SplPage;
import algeo.gui.page.SplinePage;
import algeo.gui.theme.Theme;

/**
 * Jendela utama aplikasi. Mengatur perpindahan antara halaman beranda dan halaman fitur.
 */
public class MainFrame extends JFrame {

    private final CardLayout kartu = new CardLayout();
    private final JPanel wadah = new JPanel(kartu);

    public MainFrame() {
        super("ALGEO CALCULATOR - profil2spartans");

        wadah.add(new HomePage(this), "LANDING");
        tambahHalaman("SPL", new SplPage());
        tambahHalaman("DET", new DeterminantPage());
        tambahHalaman("INV", new InversePage());
        tambahHalaman("INTERP", new InterpolationPage());
        tambahHalaman("SPLINE", new SplinePage());
        tambahHalaman("REGRESI", new RegressionPage());
        add(wadah);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
    }

    public void bukaHalaman(String nama) {
        kartu.show(wadah, nama);
    }

    private void tambahHalaman(String nama, FeaturePage halaman) {
        halaman.setAksiKembali(() -> bukaHalaman("LANDING"));
        wadah.add(halaman, nama);
    }

    /** Dipanggil dari App.main untuk membuka GUI. */
    public static void tampilkan() {
        // tooltip ikut tema
        UIManager.put("ToolTip.background", Theme.PINK_MUDA);
        UIManager.put("ToolTip.foreground", Theme.TEKS);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Theme.PINK_GARIS, 2));
        UIManager.put("ToolTip.font", Theme.BIASA);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // langsung layar penuh
            frame.setVisible(true);
        });
    }
}
