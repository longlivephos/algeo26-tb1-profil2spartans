package algeo;

import java.util.Scanner;

import algeo.modules.MenuUtama;

public class App {
    public static void main(String[] args) {
        boolean modeCli = args.length > 0 && args[0].equalsIgnoreCase("cli");

        if (modeCli) {
            Scanner sc = new Scanner(System.in);
            new MenuUtama().jalankan(sc);
        } else {
            // TODO: launch GUI
            System.out.println("Mode GUI belum tersedia. Jalankan dengan argumen 'cli':");
            System.out.println("  java -jar app.jar cli");
        }
    }
}