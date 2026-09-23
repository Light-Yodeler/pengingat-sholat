# Noor Waktu

Aplikasi Android pengingat waktu salat, penunjuk arah kiblat, dan panduan dzikir harian berbasis Jetpack Compose dan Material 3. Menggunakan kalkulasi astronomi standar Kementerian Agama Republik Indonesia (Kemenag RI) dan audio azan rekaman Masjidil Haram Makkah serta Masjid Nabawi Madinah.

## Fitur

### Jadwal Salat dan Kalender Hijriah
- Menghitung waktu Subuh, Terbit, Duha, Dzuhur, Asar, Maghrib, dan Isya secara presisi.
- Menggunakan sudut hisab Kemenag RI (Subuh -20 derajat, Isya -18 derajat) dengan koreksi ikhtiyat +2 menit.
- Database bawaan mencakup seluruh 38 provinsi di Indonesia (WIB, WITA, WIT) lengkap dengan koordinat lintang, bujur, dan elevasi.
- Bilah pencarian interaktif untuk menemukan kota atau provinsi dengan cepat.
- Dukungan deteksi lokasi otomatis via GPS perangkat. Kalkulasi waktu salat diproses sepenuhnya secara lokal di dalam ponsel tanpa membutuhkan koneksi internet.

### Pengingat Azan Latar Belakang
- Penjadwalan alarm presisi menggunakan sistem AlarmClock Android (`AlarmManager.setAlarmClock`).
- Audio azan tetap berbunyi tepat waktu meskipun aplikasi sedang ditutup, dibersihkan dari daftar aplikasi aktif, atau layar ponsel dalam keadaan terkunci.
- Notifikasi status prioritas tinggi dengan tombol aksi langsung untuk menghentikan audio azan.
- Opsi mode notifikasi per waktu salat: Azan bersuara penuh, notifikasi senyap (banner tanpa audio), atau nonaktif.

### Pilihan Audio Muazzin
- Waktu Subuh: Pilihan antara Syeikh Muhammad Nafea dan Masjid Nabawi Madinah (dengan lafaz As-salatu khayrum minan-nawm).
- Salat Lainnya (Dzuhur, Asar, Maghrib, Isya): Pilihan antara Masjidil Haram Makkah dan Masjid Nabawi Madinah (Muhammad Marwan Qassas).
- Tombol pratinjau audio langsung pada menu pengaturan pengingat.

### Kompas Arah Kiblat
- Menghitung azimuth kiblat menggunakan rumus Great Circle spherical trigonometry berdasarkan koordinat Ka'bah di Makkah (21.4225 N, 39.8262 E).
- Mengintegrasikan sensor akselerometer dan magnetometer perangkat dengan penyaringan perataan nilai (low-pass filtering) agar jarum kompas tidak bergetar liar.
- Dilengkapi indikator visual dan getaran haptik ketika arah ponsel telah sejajar tepat dengan kiblat.

### Dzikir Harian dan Tasbih Digital
- Empat kategori dzikir berlandaskan dalil sahih: Dzikir Setelah Salat, Dzikir Pagi, Dzikir Petang, dan Dzikir Bebas.
- Memuat teks Arab asli berharakat lengkap, transliterasi Latin, dan terjemahan bahasa Indonesia.
- Tombol tasbih digital lingkaran dengan penghitung sentuh per butir doa.
- Tombol reset per item dan tombol konfirmasi untuk mereset seluruh hitungan dzikir ke angka nol.

## Kebutuhan Sistem

- Perangkat: Ponsel atau tablet Android dengan sistem operasi Android 7.0 (API level 24) ke atas.
- Rekomendasi: Android 8.0 (API level 26) atau yang lebih baru untuk manajemen background alarm optimal.
- Sensor: Magnetometer dan akselerometer untuk fungsi kompas kiblat.
- Alat Pengembangan: Android Studio Ladybug (2024.2) atau versi lebih baru, dengan JDK 17 atau Java Runtime bawaan Android Studio.

## Izin Aplikasi (Permissions)

Aplikasi mendefinisikan izin berikut di `AndroidManifest.xml`:

- `android.permission.SCHEDULE_EXACT_ALARM` dan `android.permission.USE_EXACT_ALARM`: Diperlukan agar sistem Android membangunkan alarm azan pada detik yang tepat.
- `android.permission.POST_NOTIFICATIONS`: Menampilkan notifikasi visual waktu salat dan kontrol pemutar audio pada Android 13 (API 33) ke atas.
- `android.permission.WAKE_LOCK`: Menjaga audio azan tetap berputar penuh saat layar ponsel mati.
- `android.permission.VIBRATE`: Memberikan getaran notifikasi saat masuk waktu salat dan haptik saat jarum kompas menghadap kiblat.
- `android.permission.ACCESS_FINE_LOCATION` dan `android.permission.ACCESS_COARSE_LOCATION`: Mengambil koordinat GPS lokal perangkat untuk menghitung jadwal salat di lokasi pengguna saat ini.

## Cara Memasang dan Menjalankan

### Menggunakan Android Studio

1. Kloning repositori ini:
   ```bash
   git clone https://github.com/Light-Yodeler/pengingat-sholat.git
   ```
2. Buka Android Studio, pilih menu **File > Open**, lalu arahkan ke direktori proyek hasil kloning.
3. Tunggu hingga proses sinkronisasi Gradle selesai.
4. Hubungkan perangkat Android fisik via kabel USB (aktifkan opsi USB Debugging pada Pengaturan Pengembang) atau jalankan Android Virtual Device (AVD).
5. Klik tombol **Run 'app'** (ikon segitiga hijau) pada toolbar Android Studio.

### Menggunakan Terminal (Command Line)

Pastikan variabel lingkungan `JAVA_HOME` dan `ANDROID_HOME` telah terpasang di sistem operasi Anda.

1. Buka terminal dan masuk ke direktori proyek:
   ```bash
   cd pengingat-sholat
   ```
2. Berikan izin eksekusi pada wrapper Gradle (Linux/macOS):
   ```bash
   chmod +x gradlew
   ```
3. Kompilasi aplikasi dalam mode Debug:
   ```bash
   ./gradlew assembleDebug
   ```
   File APK hasil kompilasi akan berada di direktori `app/build/outputs/apk/debug/app-debug.apk`.
4. Pasang langsung ke ponsel yang terhubung via ADB:
   ```bash
   ./gradlew installDebug
   ```

## Struktur Proyek

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/myapplication/
│   ├── core/
│   │   ├── alarm/          # AzanAlarmScheduler, AzanBroadcastReceiver, BootReceiver
│   │   ├── calculation/    # Algoritma hisab astronomi dan koreksi ikhtiyat
│   │   ├── location/       # Database koordinat 38 provinsi dan LocationHelper
│   │   ├── model/          # Model data jadwal, doa dzikir, dan preferensi
│   │   └── preferences/    # Persistensi preferensi dan setelan (AppPreferences)
│   ├── ui/
│   │   ├── screens/        # JadwalScreen, KiblatScreen, PengingatScreen, DzikirScreen
│   │   ├── theme/          # Palet warna, tipografi, dan tema Material 3
│   │   ├── NoorWaktuMainScreen.kt
│   │   └── NoorWaktuViewModel.kt
│   └── MainActivity.kt
└── res/
    ├── drawable/           # Aset grafis, app_logo.png
    ├── mipmap-*/           # Ikon launcher aplikasi resmi
    └── raw/                # Audio rekaman azan (Makkah, Madinah, Nafea)
```
