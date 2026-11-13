package com.minimal.tube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ChannelBarAdapter(
    private val items: MutableList<ChannelItem> = mutableListOf(),
    private val onChannelClick: (ChannelItem) -> Unit
) : RecyclerView.Adapter<ChannelBarAdapter.ChannelViewHolder>() {

    fun updateData(newItems: List<ChannelItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel_circle, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgChannelAvatar)
        private val txtName: TextView = itemView.findViewById(R.id.txtChannelName)

        fun bind(item: ChannelItem) {
            txtName.text = item.name
            if (item.avatarUrl.isNotEmpty()) {
                imgAvatar.load(item.avatarUrl) {
                    crossfade(true)
                }
            } else {
                imgAvatar.setImageDrawable(null)
            }
            itemView.setOnClickListener { onChannelClick(item) }
        }
    }
}
