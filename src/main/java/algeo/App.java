package algeo;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;

import algeo.gui.MainFrame;
import algeo.modules.MenuUtama;

public class App {
    public static void main(String[] args) {
        boolean modeCli = args.length > 0 && args[0].equalsIgnoreCase("cli");

        if (modeCli) {
            Scanner sc = new Scanner(System.in);
            new MenuUtama().jalankan(sc);
        } else if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Tidak ada layar untuk menampilkan GUI. Jalankan mode CLI:");
            System.out.println("  java -jar <nama-jar>.jar cli");
        } else {
            MainFrame.tampilkan();
        }
    }
}