package algeo.gui;

import java.awt.Color;

import javax.swing.JFrame; 
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import algeo.gui.page.DeterminantPage;
import algeo.gui.page.InterpolationPage;
import algeo.gui.page.InversePage;
import algeo.gui.page.RegressionPage;
import algeo.gui.page.SplPage;
import algeo.gui.page.SplinePage;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Algeo Calculator by profil2spartans");

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("SPL", new SplPage());
        tab.addTab("Determinan", new DeterminantPage());
        tab.addTab("Matriks Balikan", new InversePage());
        tab.addTab("Interpolasi Polinomial", new InterpolationPage());
        tab.addTab("Natural Cubic Spline", new SplinePage());
        tab.addTab("Regresi Spline", new RegressionPage());
        add(tab);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    public static void tampilkan() {
        aturWarnaPink();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static void aturWarnaPink() {
        Color pinkMuda = new Color(255, 228, 238);
        Color pink = new Color(245, 185, 210);
        Color pinkPutih = new Color(255, 245, 250);

        UIManager.put("Panel.background", pinkMuda);
        UIManager.put("OptionPane.background", pinkMuda);
        UIManager.put("TabbedPane.background", pink);
        UIManager.put("TabbedPane.selected", pinkMuda);
        UIManager.put("TabbedPane.contentAreaColor", pinkMuda);
        UIManager.put("Button.background", pink);
        UIManager.put("Button.gradient", null);
        UIManager.put("ComboBox.background", pinkPutih);
        UIManager.put("Spinner.background", pinkPutih);
        UIManager.put("FormattedTextField.background", pinkPutih);
        UIManager.put("TextField.background", pinkPutih);
        UIManager.put("TextArea.background", pinkPutih);
        UIManager.put("Viewport.background", pinkMuda);
        UIManager.put("ScrollPane.background", pinkMuda);
        UIManager.put("SplitPane.background", pinkMuda);
    }
}
