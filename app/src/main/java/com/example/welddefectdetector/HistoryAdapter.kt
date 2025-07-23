package com.example.welddefectdetector

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.welddefectdetector.HistoryItem

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        val bitmap = BitmapFactory.decodeFile(historyItem.imagePath)
        holder.imageView.setImageBitmap(bitmap)

        // Klik gambar untuk membuka fullscreen
        holder.imageView.setOnClickListener {
            val intent = Intent(holder.itemView.context, FullscreenImageActivity::class.java).apply {
                putExtra("image_path", historyItem.imagePath)
                putExtra("labels", historyItem.detectionLabels) // kirim juga label
            }
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = historyList.size
}

