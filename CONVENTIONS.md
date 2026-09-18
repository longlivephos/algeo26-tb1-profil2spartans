# Konvensi Kode
# TOLONG ya ges dibaca dengan BAIK biar repo kita RAPIHHHH yakkk

## Naming Kode
- Class: PascalCase (`GaussEliminationSolver`)
- Method/variabel: camelCase (`solveByGauss`, `augmentedMatrix`)
- Konstanta: UPPER_SNAKE_CASE (`MAX_MANUAL_SIZE`)

## Exception
- Semua error domain (dimensi tidak konsisten, singular, non-persegi, dst) lempar `MatrixException`
  dengan pesan yang SIAP ditampilkan ke user (bukan pesan teknis Java, bukan stack trace mentah).
- Input non-numerik / file not found / format salah ditangani di layer io (parser),
  BUKAN dilempar sebagai MatrixException — beda tanggung jawab (io = masukan mentah, matrix = domain matematis).

## Rounding & Parsing (WAJIB pakai util bersama, jangan bikin sendiri per modul)
- `algeo.core.util.NumberFormatter.round(double)` → bulatkan 3 desimal untuk SEMUA output.
- `algeo.core.util.DecimalParser.parse(String)` → terima "." maupun "," sebagai desimal, dipakai
  semua parser input (manual & file).

## Javadoc
- Setiap public method wajib javadoc: `@param`, `@return`, `@throws`.

## Package Boundary
- `algeo.core.*` TIDAK BOLEH import apa pun dari `algeo.modules.*` (CLI) atau `algeo.gui.*`.
  Arah dependency cuma satu arah: modules/gui -> core, tidak pernah sebaliknya.

## Git Workflow
- Branch fitur: `feature/<nama-modul>-<deskripsi-singkat>`, contoh:
  `feature/spl-gauss-elimination`, `feature/determinant-cofactor`, `feature/gui-spl-screen`.
- Commit pakai semantic commit: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`.
  Contoh: `feat(spl): implement partial pivoting for gauss elimination`.
- Semua branch fitur dibuat dari `main` yang SUDAH include `feature/core-skeleton` (Matrix,
  MatrixException, SPLResult, util), jangan branch dari sebelum itu merge.
- PR checklist sebelum merge ke `main`:
  - `mvn clean install` sukses tanpa error
  - Tidak ada perubahan signature `Matrix` / `MatrixException` / `SPLResult` tanpa broadcast ke grup dulu
  - Javadoc ada di method public baru
  - Tidak ada rounding/parsing manual yang seharusnya pakai `NumberFormatter`/`DecimalParser`