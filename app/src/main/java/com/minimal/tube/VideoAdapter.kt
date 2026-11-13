package com.minimal.tube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.text.DecimalFormat

class VideoAdapter(
    private val items: MutableList<VideoItem> = mutableListOf(),
    private val onPlayClick: (VideoItem) -> Unit,
    private val onDownloadClick: (VideoItem) -> Unit = {},
    private val onSaveClick: (VideoItem) -> Unit = {}
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    fun updateData(newItems: List<VideoItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val txtDurationBadge: TextView = itemView.findViewById(R.id.txtDurationBadge)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtMeta: TextView = itemView.findViewById(R.id.txtMeta)
        private val btnPlay: TextView = itemView.findViewById(R.id.btnPlayVideo)
        private val btnDownload: TextView = itemView.findViewById(R.id.btnDownloadVideo)
        private val btnSave: TextView = itemView.findViewById(R.id.btnSaveVideo)

        fun bind(item: VideoItem) {
            txtTitle.text = item.title

            val formattedViews = formatViews(item.viewCount)
            val dateStr = if (item.uploadDate.isNotEmpty()) " • ${item.uploadDate}" else ""
            txtMeta.text = "${item.uploaderName} • $formattedViews$dateStr"

            if (item.durationText.isNotEmpty()) {
                txtDurationBadge.text = item.durationText
                txtDurationBadge.visibility = View.VISIBLE
            } else {
                txtDurationBadge.visibility = View.GONE
            }

            if (!item.thumbnailUrl.isNullOrEmpty()) {
                imgThumbnail.load(item.thumbnailUrl) {
                    crossfade(true)
                }
            } else {
                imgThumbnail.setImageDrawable(null)
            }

            btnPlay.setOnClickListener { onPlayClick(item) }
            itemView.setOnClickListener { onPlayClick(item) }
            btnDownload.setOnClickListener { onDownloadClick(item) }
            btnSave.setOnClickListener { onSaveClick(item) }
        }

        private fun formatViews(count: Long): String {
            if (count <= 0) return "0 lượt xem"
            return when {
                count >= 1_000_000 -> {
                    val df = DecimalFormat("#.#")
                    "${df.format(count / 1_000_000.0)} Tr lượt xem"
                }
                count >= 1_000 -> {
                    val df = DecimalFormat("#.#")
                    "${df.format(count / 1_000.0)} N lượt xem"
                }
                else -> "$count lượt xem"
            }
        }
    }
}
