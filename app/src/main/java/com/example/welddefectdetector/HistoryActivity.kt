package com.example.welddefectdetector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.example.welddefectdetector.HistoryItem


class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        loadHistory()
    }

    private fun loadHistory() {
        val historyDir = File(getExternalFilesDir(null), ".WeldDefectDetector/History")
        if (historyDir.exists()) {
            val files = historyDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.extension == "png") {
                        val labelFile = File(file.parent, file.nameWithoutExtension + ".txt")
                        val labelText = if (labelFile.exists()) labelFile.readText() else "Tidak ada label"
                        historyList.add(HistoryItem(file.absolutePath, labelText))
                    }
                }
            }
        }
    }

}
