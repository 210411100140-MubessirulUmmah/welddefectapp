package com.example.welddefectdetector

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class FullscreenImageActivity : AppCompatActivity() {

    private lateinit var fullscreenImageView: ImageView
    private lateinit var btnDownloadImage: ImageButton
    private lateinit var btnDeleteImage: ImageButton
    private lateinit var labelTextView: TextView
    private var imagePath: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        fullscreenImageView = findViewById(R.id.fullscreenImageView)
        btnDownloadImage = findViewById(R.id.btnDownloadImage)
        btnDeleteImage = findViewById(R.id.btnDeleteImage)
        labelTextView = findViewById(R.id.textViewDetectionLabels)

        // Ambil path gambar dari intent
        imagePath = intent.getStringExtra("image_path")
        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            fullscreenImageView.setImageBitmap(bitmap)
        }

        // Ambil teks hasil deteksi dari intent
        val labelText = intent.getStringExtra("labels")
        labelTextView.text = labelText ?: "Tidak ada label"

        btnDownloadImage.setOnClickListener {
            downloadImage()
        }

        btnDeleteImage.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun downloadImage() {
        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            if (bitmap == null) {
                Toast.makeText(this, "Gambar tidak tersedia", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = "hasil_deteksi_${System.currentTimeMillis()}.png"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val saveDir = File(downloadsDir, "WeldDefectDetector")
            if (!saveDir.exists()) saveDir.mkdirs()

            val file = File(saveDir, fileName)
            try {
                FileOutputStream(file).use { out ->
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        Toast.makeText(this, "Gambar berhasil didownload: ${file.absolutePath}", Toast.LENGTH_LONG).show()

                        // Tambahkan ke galeri
                        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        intent.data = Uri.fromFile(file)
                        sendBroadcast(intent)
                    } else {
                        Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle("Hapus Gambar")
            setMessage("Apakah Anda yakin ingin menghapus gambar ini?")
            setPositiveButton("Ya") { _, _ -> deleteImage() }
            setNegativeButton("Tidak", null)
            show()
        }
    }

    private fun deleteImage() {
        imagePath?.let {
            val file = File(it)
            if (file.exists() && file.delete()) {
                Toast.makeText(this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HistoryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Gagal menghapus gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
