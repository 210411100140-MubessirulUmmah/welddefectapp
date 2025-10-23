# 🧠 Welding Defect Detection Android App

Aplikasi Android untuk mendeteksi **cacat las** menggunakan model **DETR (DEtection TRansformer)** yang diintegrasikan melalui layanan **cloud-based API**.
Proyek ini dirancang untuk membantu pengguna dalam melakukan deteksi cacat pada hasil pengelasan langsung dari perangkat seluler.

---

## 🎬 Demo Aplikasi

https://github.com/user-attachments/assets/dfcac90e-3976-4b90-9f67-b45692cbd911

---

## 🚀 Deskripsi Singkat

Aplikasi ini menggabungkan teknologi **Deep Learning**, **Flask API**, dan **Android** untuk memberikan solusi deteksi cacat berbasis citra.
Prosesnya berlangsung secara cloud-based, sehingga aplikasi tidak perlu menyimpan model secara lokal di perangkat.

### Alur Umum Sistem:

1. Pengguna memilih atau mengambil gambar hasil pengelasan melalui aplikasi.
2. Aplikasi mengirimkan gambar ke server yang menjalankan model deteksi cacat.
3. Server melakukan inferensi menggunakan model **DETR** dan mengembalikan hasil deteksi berupa label cacat, koordinat bounding box, dan confidece score.
4. Aplikasi menampilkan hasil deteksi langsung di atas gambar.
5. Hasil deteksi dapat disimpan ke direktori tersembunyi sebagai **riwayat deteksi**.

---

## ☁️ Integrasi Model ke Cloud

Model deteksi cacat las yang telah dilatih dikonversi ke format **TorchScript** agar dapat dijalankan di server tanpa ketergantungan penuh pada PyTorch.
Beberapa berkas utama yang digunakan dalam deployment model meliputi:

* `app.py` → Skrip utama untuk menangani request dan melakukan inferensi.
* `requirements.txt` → Berisi dependensi Python seperti `torch`, `flask`, dan `Pillow`.
* `Dockerfile` → Mengatur environment berbasis Python dan melakukan instalasi otomatis seluruh dependensi.

Model tersebut dijalankan di lingkungan cloud menggunakan **Flask** dan container Docker, sehingga dapat diakses oleh aplikasi Android melalui komunikasi HTTP.

---

## 🔄 Arsitektur dan Proses Sistem

Sistem terdiri dari dua komponen utama:

### 1. Aplikasi Android (Client)

* Dibangun menggunakan **Kotlin** di Android Studio.
* Mengizinkan pengguna memilih gambar pengelasan dari galeri atau kamera.
* Mengirimkan gambar ke server untuk diproses.
* Menampilkan hasil deteksi dalam bentuk bounding box dan label cacat.
* Menyimpan hasil deteksi ke penyimpanan lokal.

### 2. API Server (Cloud)

* Dibangun dengan **Flask** dan dijalankan di lingkungan cloud.
* Menjalankan model **TorchScript** untuk mendeteksi cacat pada gambar.
* Mengembalikan hasil deteksi dalam format JSON ke aplikasi Android.

---

## 📱 Hasil Implementasi

Aplikasi menunjukkan kemampuan mendeteksi berbagai jenis cacat las (Spatter, Slag Inclusion, Undercut).
Setiap hasil deteksi divisualisasikan dalam bentuk **bounding box** dan label yang menggambarkan jenis cacatnya, beserta confidence scorenya.
Selain itu, pengguna dapat menyimpan hasil deteksi untuk ditinjau kembali melalui menu riwayat.

---

## 👨‍💻 Author

**Mubessirul Ummah**
