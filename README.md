# Tugas Besar 1 IF2123 Aljabar Linier dan Geometri

**Kelompok profil2spartans**

| Nama | NIM |
| --- | --- |
| Neysa Alya Mukhbita | 13525080 |
| Jonathan Lewie | 13525136 |
| Cathrine Angel Siburian | 13525138 |

## Deskripsi Program

Program ini adalah kalkulator aljabar linier berbasis Java yang dibangun di atas pustaka buatan kelompok sendiri tanpa pustaka matriks eksternal seperti JAMA, EJML, Commons Math, dan sejenisnya. Kelas `Matrix` beserta operasi dasarnya, semua metode SPL, determinan, invers, interpolasi, dan regresi diimplementasikan dari awal sehingga proyek dikelola dengan Maven dan dikemas menjadi satu berkas JAR yang bisa dipakai sebagai aplikasi maupun sebagai library di program Java lain.

Program punya dua antarmuka:

- **GUI (Swing)**: tampilan bertab untuk keenam fitur. Langkah perhitungan ditampilkan per tahap, dan hasil interpolasi, spline, serta regresi divisualisasikan dalam grafik kurva.
- **CLI**: sistem menu di terminal sesuai struktur menu minimum pada spesifikasi.

### Fitur

| No | Fitur | Metode |
| --- | --- | --- |
| 1 | Sistem Persamaan Linier | Eliminasi Gauss, Eliminasi Gauss-Jordan (keduanya dengan partial pivoting), Matriks Balikan, Kaidah Cramer |
| 2 | Determinan | Reduksi Baris (OBE), Ekspansi Kofaktor |
| 3 | Matriks Balikan | Augmentasi [A\|I] dengan Gauss-Jordan, Adjoin |
| 4 | Interpolasi Polinomial | Matriks Vandermonde |
| 5 | Natural Cubic Spline | Sistem tridiagonal dengan syarat batas natural (M₀ = Mₙ = 0) |
| 6 | Regresi Spline | Truncated power basis dan Persamaan Normal (XᵀX)β = Xᵀy, pilihan derajat 1, 2, atau 3 (default 3) |

## Requirements

- Java 17 atau lebih baru
- Maven 3.6.3 atau lebih baru

Periksa instalasi dengan perintah berikut.

```bash
java --version
mvn --version
```

## Cara Menjalankan

Jalankan semua perintah dari root repositori supaya path relatif langsung terbaca.

### Dari JAR (tanpa build kembali)

```bash
java -jar bin/profil2spartans-1.0-SNAPSHOT.jar        # GUI
java -jar bin/profil2spartans-1.0-SNAPSHOT.jar cli    # CLI
```

### Build ulang dari source

```bash
mvn clean package
```

```bash
copy target\profil2spartans-1.0-SNAPSHOT.jar bin\
```

## Alur Program

### CLI

1. Pilih fitur dari menu utama:

   ```text
   MENU UTAMA
   1. Sistem Persamaan Linier (SPL)
   2. Determinan Matriks
   3. Matriks Balikan (Invers)
   4. Interpolasi Polinomial
   5. Natural Cubic Spline Interpolation
   6. Regresi Spline Kubik
   7. Keluar
   ```

2. Pilih metode di submenu. SPL, determinan, dan invers punya submenu metode dimana regresi punya submenu derajat spline (1 linier, 2 kuadratik, 3 kubik).
3. Pilih sumber masukan: `1` untuk keyboard, `2` untuk berkas `.txt`.
   - **Keyboard, matriks**: masukkan jumlah baris, lalu isi baris demi baris dengan elemen dipisah spasi. Jumlah kolom dibaca dari baris yang diketik.
   - **Keyboard, titik data** (interpolasi, spline, regresi): ketik satu pasangan `x y` per baris, lalu tekan ENTER dua kali setelah titik terakhir.
   - **Berkas**: masukkan path berkas, misalnya `test/spl_5_4_1.txt`.
4. Program menampilkan langkah perhitungan dan kesimpulan.
5. Khusus regresi yaitu setelah data dibaca, masukkan jumlah knot `K` lalu posisi knot dipisah spasi.
6. Khusus interpolasi, spline, dan regresi adalah program menawarkan evaluasi P(xₜ), S(xₜ), atau ŷ(xₜ) berulang kali sampai pengguna menjawab `n`. Untuk interpolasi polinomial, program memberi peringatan kalau xₜ di luar domain data.
7. Jawab `y` untuk menyimpan hasil, lalu masukkan nama atau path berkas tujuan **lengkap dengan `.txt`**, misalnya `test/hasil_spl.txt`. Berkas yang sudah ada akan ditimpa.

Contoh sesi SPL 5.4.1 dengan Gauss-Jordan:

```text
Pilihan: 1
Pilihan metode: 2
Pilihan: 2
Masukkan path file (contoh: test/spl_1.txt): test/spl_5_4_1.txt
...
x1 = 1
x2 = -2
x3 = 3
x4 = 2
```

### GUI

1. Pilih tab fitur: SPL, Determinan, Matriks Balikan, Interpolasi Polinomial, Natural Cubic Spline, atau Regresi Spline.
2. Isi ukuran dan elemen matriks atau titik data di panel **Input**, atau tekan **Buka File** untuk memuat berkas `.txt`.
3. Pilih metode, lalu tekan **Hitung**.
4. Langkah perhitungan tampil di panel **Langkah dan Hasil** dengan penanda warna yang terdiri dari baris diubah, nilai yang berubah, baris acuan, dan baris ditukar. Untuk interpolasi, spline, dan regresi, kurva hasil juga digambar dalam grafik.
5. Tekan **Simpan** untuk menyimpan hasil ke `.txt`, atau **Reset** untuk mengosongkan input.

## Format Berkas Masukan

Bilangan dipisahkan spasi, satu baris matriks atau satu titik per baris, tanpa baris kosong di tengah berkas. Nama berkas bebas dan tidak dipakai untuk menentukan jenis masukan.

**SPL** (matriks augmented [A|b]):

```text
3 4.5 2.8 10 12
7 8.3 -3 11 -4
0.5 -10 -9 12 0
```

**Determinan dan Matriks Balikan** (matriks persegi):

```text
3 4.5 2.8
7 8.3 -3
0.5 -10 -9
```

**Interpolasi, Natural Cubic Spline, dan Regresi** (satu titik `x y` per baris):

```text
0 1
1 2
2 0
3 1
```

## Batasan

- Masukan keyboard: matriks maksimal 11×11 (augmented SPL 11×12).
- Masukan berkas: matriks maksimal 1001×1001 (augmented 1001×1002).
- Interpolasi, spline, dan regresi: maksimal 10 titik data.
- Natural cubic spline: nilai x harus terurut naik.
- Regresi: posisi knot harus terurut naik, tidak boleh sama, dan berada di antara x terkecil dan x terbesar.
- Determinan dengan ekspansi kofaktor: maksimal 11×11. Untuk matriks lebih besar gunakan OBE.
- Invers dengan adjoin tetap jalan untuk n > 11, tetapi langkahnya tidak ditampilkan. Untuk matriks besar metode augmentasi jauh lebih cepat.
- Metode Matriks Balikan dan Kaidah Cramer hanya berlaku untuk SPL persegi dengan det(A) ≠ 0. Di luar itu program menampilkan alasannya, bukan crash.

## Struktur Direktori

```text
.
├── bin                                   # JAR (profil2spartans-1.0-SNAPSHOT.jar)
├── docs                                  # laporan (PDF) dan sumber LaTeX
├── src                                   # kode sumber program
│   └── main
│       └── java
│           └── algeo                     # entry point
│               ├── core                  # inti pustaka aljabar linier
│               │   ├── interpolasi       # Natural Cubic Spline
│               │   ├── matrix            # kelas Matrix dan MatrixException
│               │   ├── regression        # Regresi Spline
│               │   ├── spl               # Gauss, Gauss-Jordan, Matriks Balikan, Cramer
│               │   └── util              # parser desimal, pembulatan, toleransi
│               ├── determinant           # determinan: OBE dan Ekspansi Kofaktor
│               ├── gui                   # antarmuka Swing berisi MainFrame
│               │   ├── component         # komponen GUI
│               │   └── page              # halaman per fitur
│               ├── interpolasipolinomial # Interpolasi Polinomial
│               ├── inverse               # matriks balikan: Augmentasi dan Adjoin
│               ├── io                    # pembaca matriks dari teks/berkas, penulis output .txt
│               └── modules               # menu utama dan submenu CLI
├── test                                  # berkas kasus uji (.txt)
├── pom.xml                               # konfigurasi build Maven
└── README.md
```

## Kasus Uji

| Berkas | Kasus |
| --- | --- |
| `det_5_1_1.txt` … `det_5_1_3.txt` | 5.1 Determinan |
| `inverse_5_2_1.txt`, `inverse_5_2_2.txt` | 5.2 Invers |
| `spl_5_3_1.txt` … `spl_5_3_3.txt`, `spl_5_3_4_n6.txt`, `spl_5_3_4_n10.txt` | 5.3 SPL Ax = b |
| `spl_5_4_1.txt`, `spl_5_4_2.txt` | 5.4 SPL augmented |
| `spl_5_5_1.txt`, `spl_5_5_2.txt` | 5.5 SPL bentuk umum |
| `spl_5_6_regresi.txt` | 5.6 Aplikasi SPL (persamaan normal) |
| `interpolasi_5_7a.txt`, `interpolasi_5_7b.txt` | 5.7 Interpolasi polinomial |
| `spline_5_8.txt` | 5.8 Natural cubic spline |
| `regresi_5_9.txt` | 5.9 Regresi spline |

## Laporan dan Video

- Laporan: [`docs/profil2spartans.pdf`](docs/profil2spartans.pdf)
- Video penjelasan: https://youtu.be/vbZljX5lGHU?si=9NXRAqodG93jnZm0
