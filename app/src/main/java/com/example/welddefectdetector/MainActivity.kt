package com.example.welddefectdetector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import androidx.appcompat.widget.Toolbar

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var imageRecyclerView: RecyclerView
    private val imageList = mutableListOf<DetectionResult>()
    private lateinit var imageAdapter: ImageAdapter
    private var resultText: StringBuilder = StringBuilder()
    private lateinit var imageViewBefore: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCamera: ImageButton
    private lateinit var btnHistory: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var btnDownload: ImageButton
    private lateinit var toolbar: Toolbar
    private lateinit var textViewBefore: TextView
    private lateinit var textViewDetectionResults: TextView
    private var imageUri: Uri? = null
    private var mutableBitmap: Bitmap? = null
    private var cameraImageUri: Uri? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hubungkan Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Weld Defect Detector"

        imageViewBefore = findViewById(R.id.imageViewBefore)
        progressBar = findViewById(R.id.progressBar)
        btnCamera = findViewById(R.id.btnCamera)
        btnClear = findViewById(R.id.btnClear)
        btnDownload = findViewById(R.id.btnDownload)
        textViewBefore = findViewById(R.id.textViewBefore)
        btnHistory = findViewById(R.id.btnHistory)
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        imageRecyclerView = findViewById(R.id.imageRecyclerView)
        imageAdapter = ImageAdapter(imageList)
        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageRecyclerView.adapter = imageAdapter

        textViewDetectionResults = findViewById(R.id.textViewDetectionResults)

        textViewBefore.text = "Masukkan gambar"
        textViewDetectionResults.text = ""
        imageViewBefore.setImageResource(android.R.color.darker_gray)

        imageViewBefore.setOnClickListener { selectImage() }
        btnCamera.setOnClickListener { openCamera() }
        btnClear.setOnClickListener {
            resetImage()
        }
        btnDownload.setOnClickListener {
            saveDetectedImage()
        }


    }

    private fun resetImage() {
        imageViewBefore.setImageResource(android.R.color.darker_gray)
        textViewBefore.text = "Masukkan gambar"
        textViewDetectionResults.text = ""
        imageUri = null
        mutableBitmap = null

        imageViewBefore.visibility = View.VISIBLE
        textViewBefore.visibility = View.VISIBLE

        imageList.clear()
        imageAdapter.notifyDataSetChanged()

        Toast.makeText(this, "Semua gambar dibersihkan", Toast.LENGTH_SHORT).show()
//        Toast.makeText(this, "Gambar dibersihkan", Toast.LENGTH_SHORT).show()
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    private fun saveDetectedImage() {
        if (mutableBitmap != null) {
            val fileName = "hasil_deteksi_${System.currentTimeMillis()}.png"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                FileOutputStream(file).use { out ->
                    mutableBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                    Toast.makeText(this, "Hasil disimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DOWNLOAD_ERROR", "Gagal menyimpan hasil: ${e.message}")
                Toast.makeText(this, "Gagal menyimpan hasil", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tidak ada gambar yang dideteksi", Toast.LENGTH_SHORT).show()
        }
    }


    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 101)
    }


//    private fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent, 200)
//    }
    private fun openCamera() {
        val photoFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        startActivityForResult(intent, 200)
    }

    private fun createImageFile(): File {
        val timeStamp: String = System.currentTimeMillis().toString()
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                101 -> {
                    val clipData = data?.clipData
                    if (clipData != null) {
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until clipData.itemCount) {
                            uris.add(clipData.getItemAt(i).uri)
                        }
                        uploadImagesSequentially(uris)
                    } else {
                        data?.data?.let { uri ->
                            uploadImage(uri)
                        }
                    }
                }
                200 -> {
                    cameraImageUri?.let { uri ->
                        imageViewBefore.setImageURI(uri)
                        textViewBefore.text = ""
                        uploadImage(uri)
                    }
                }
            }
        }
    }

    private fun uploadImagesSequentially(uris: List<Uri>, index: Int = 0) {
        if (index >= uris.size) return

        uploadImage(uris[index], onComplete = {
            uploadImagesSequentially(uris, index + 1)
        })
    }

    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }



    private fun uploadImage(uri: Uri, onComplete: (() -> Unit)? = null) {
        imageUri = uri
        progressBar.visibility = View.VISIBLE

        val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val resizedBitmap = resizeBitmap(originalBitmap, 640, 640)
        val file = bitmapToFile(resizedBitmap, "resized_image.jpg")

        if (!file.exists()) {
            progressBar.visibility = View.GONE
            onComplete?.invoke()
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        ApiClient.instance.uploadImage(body).enqueue(object : Callback<DetectionResponse> {
            override fun onResponse(call: Call<DetectionResponse>, response: Response<DetectionResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val detections = response.body()?.detections
                    imageViewBefore.visibility = View.GONE
                    textViewBefore.visibility = View.GONE
                    Log.d("API_RESPONSE", "Jumlah deteksi: ${detections?.size}")
                    Log.d("API_RESPONSE", "Isi deteksi: $detections")
                    if (!detections.isNullOrEmpty()) {
//                        drawBoundingBoxes(detections)
                        drawBoundingBoxes(detections, resizedBitmap)
                    } else {
                        Toast.makeText(this@MainActivity, "Tidak ada deteksi terdeteksi", Toast.LENGTH_SHORT).show()
                        drawBoundingBoxes(emptyList(), resizedBitmap)
                    }
                }
                onComplete?.invoke()
            }

            override fun onFailure(call: Call<DetectionResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("API_FAILURE", "Error saat upload: ${t.message}")
                onComplete?.invoke()
            }
        })
    }

    private fun drawBoundingBoxes(detections: List<Detection>, inputBitmap: Bitmap) {
        val classColors = mapOf(
            "spatter" to Color.RED,
            "slag inclusion" to Color.BLUE,
            "undercut" to Color.GREEN
        )

        // Gunakan bitmap yang sudah di-resize, bukan dari imageUri
        mutableBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap!!)
        val textPaint = Paint().apply {
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }

        resultText.clear()

        if (detections.isEmpty()) {
            resultText.append("Tidak ada deteksi\n")
        }

        Log.d("DRAW_BOXES", "Jumlah deteksi: ${detections.size}")
        for (detection in detections) {
            val (xMin, yMin, xMax, yMax) = detection.bbox
            val label = "${detection.`class`} (${(detection.score * 100).toInt()}%)"
            val color = classColors[detection.`class`] ?: Color.YELLOW

            val paint = Paint().apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = 5f
            }

            textPaint.color = color
            canvas.drawRect(xMin, yMin, xMax, yMax, paint)
            canvas.drawText(label, xMin, yMin - 10, textPaint)

            Log.d("DETECTION_ITEM", "Deteksi: ${detection.`class`} skor: ${detection.score}, bbox: ${detection.bbox}")

            resultText.append("$label\n")
        }

        // Simpan hasil deteksi ke riwayat
        saveToHistory(mutableBitmap!!)

        runOnUiThread {
            imageViewBefore.visibility = View.GONE
            textViewBefore.visibility = View.GONE

            val labelText = "Gambar ${imageList.size + 1}:\n${resultText}\n"
            imageList.add(DetectionResult(mutableBitmap!!, labelText))
            imageAdapter.notifyItemInserted(imageList.size - 1)
        }
    }


    // Menyimpan hasil deteksi ke folder riwayat
    // Menyimpan hasil deteksi ke folder riwayat tersembunyi
    private fun saveToHistory(bitmap: Bitmap) {
        val fileName = "hasil_deteksi_${System.currentTimeMillis()}.png"
        val historyDir = File(getExternalFilesDir(null), ".WeldDefectDetector/History")
        if (!historyDir.exists()) historyDir.mkdirs()
        val file = File(historyDir, fileName)
        val labelFile = File(historyDir, fileName.replace(".png", ".txt"))

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Log.d("HISTORY_SAVE", "Hasil disimpan di riwayat: ${file.absolutePath}")
            }
            labelFile.writeText(resultText.toString())
        } catch (e: Exception) {
            Log.e("HISTORY_SAVE_ERROR", "Gagal menyimpan hasil ke riwayat: ${e.message}")
        }
    }



    private fun saveTempBitmap(bitmap: Bitmap): Uri? {
        val file = File(cacheDir, "camera_image.jpg")
        return try {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            Uri.fromFile(file)
        } catch (e: IOException) {
            null
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriToFile(uri)?.absolutePath
        } else {
            contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use {
                it.moveToFirst()
                it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val file = File(cacheDir, "temp_image.jpg")
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
