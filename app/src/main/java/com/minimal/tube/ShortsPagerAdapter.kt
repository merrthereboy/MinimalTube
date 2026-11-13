package com.minimal.tube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ShortsPagerAdapter(
    private val items: MutableList<VideoItem> = mutableListOf(),
    private val onPlayClick: (VideoItem) -> Unit,
    private val onCommentClick: (VideoItem) -> Unit,
    private val onChannelClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<ShortsPagerAdapter.ShortViewHolder>() {

    fun updateData(newItems: List<VideoItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_short_page, parent, false)
        return ShortViewHolder(v)
    }

    override fun onBindViewHolder(holder: ShortViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ShortViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgThumbnail: ImageView = itemView.findViewById(R.id.imgShortThumbnail)
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgShortAvatar)
        private val txtChannel: TextView = itemView.findViewById(R.id.txtShortChannelName)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtShortTitle)
        private val btnLike: TextView = itemView.findViewById(R.id.btnShortLike)
        private val btnComment: ImageView = itemView.findViewById(R.id.btnShortComment)
        private val btnPlayFull: TextView = itemView.findViewById(R.id.btnShortPlayFull)
        private val btnSubscribe: TextView = itemView.findViewById(R.id.btnShortSubscribe)

        private var liked = false
        private var subscribed = false

        fun bind(item: VideoItem) {
            txtTitle.text = item.title
            txtChannel.text = "@${item.uploaderName}"

            if (!item.thumbnailUrl.isNullOrEmpty()) {
                imgThumbnail.load(item.thumbnailUrl) { crossfade(true) }
                imgAvatar.load(item.thumbnailUrl) { crossfade(true) }
            }

            btnLike.setOnClickListener {
                liked = !liked
                btnLike.setTextColor(if (liked) 0xFFFF2E93.toInt() else 0xFFFFFFFF.toInt())
            }

            btnSubscribe.setOnClickListener {
                subscribed = !subscribed
                btnSubscribe.text = if (subscribed) "ĐÃ ĐĂNG KÝ" else "ĐĂNG KÝ"
                btnSubscribe.setBackgroundResource(
                    if (subscribed) R.drawable.bg_button_pill_secondary else R.drawable.gradient_brand_button
                )
            }

            btnComment.setOnClickListener { onCommentClick(item) }
            btnPlayFull.setOnClickListener { onPlayClick(item) }
            txtChannel.setOnClickListener { onChannelClick(item) }
            itemView.setOnClickListener { onPlayClick(item) }
        }
    }
}
