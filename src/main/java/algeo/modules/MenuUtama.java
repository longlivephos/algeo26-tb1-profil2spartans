package algeo.modules;

import java.util.Scanner;

public class MenuUtama implements Module {

    @Override
    public void jalankan(Scanner sc) {
        boolean jalan = true;

        Module splModule = new SPLModule();
        Module determinanModule = new DeterminanModule();
        Module inversModule = new InversModule();
        Module interpolasiModule = new ModuleInterpolasi();
        Module splineModule = new ModuleSplineKubik();
        Module regresiSplineModule = new ModuleRegresiSplineKubik();

        while (jalan) {
            System.out.println();
            System.out.println("*~* MENU UTAMA *~*");
            System.out.println("1. Sistem Persamaan Linier (SPL)");
            System.out.println("2. Determinan Matriks");
            System.out.println("3. Matriks Balikan (Invers)");
            System.out.println("4. Interpolasi Polinomial");
            System.out.println("5. Natural Cubic Spline Interpolation");
            System.out.println("6. Regresi Spline Kubik");
            System.out.println("7. Keluar");
            System.out.print("Pilihan: ");

            String pilihan = sc.nextLine().trim();
            try {
                switch (pilihan) {
                    case "1": splModule.jalankan(sc); break;
                    case "2": determinanModule.jalankan(sc); break;
                    case "3": inversModule.jalankan(sc); break;
                    case "4": interpolasiModule.jalankan(sc); break;
                    case "5": splineModule.jalankan(sc); break;
                    case "6": regresiSplineModule.jalankan(sc); break;
                    case "7":
                        jalan = false;
                        System.out.println("Bye bye ^__^");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid, coba lagi!");
                }
            } catch (Exception e) {
                System.out.println("Terjadi kesalahan: " + e.getMessage());
            }
        }
    }
}