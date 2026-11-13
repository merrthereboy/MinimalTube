package com.minimal.tube

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.comments.CommentsInfoItem

data class CommentModel(
    val author: String,
    val avatarUrl: String,
    val text: String,
    val likeCount: Int,
    val date: String
)

class CommentsBottomSheet(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val dialog = BottomSheetDialog(context)

    fun show(videoUrl: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_comments, null)
        dialog.setContentView(view)

        val txtStatus = view.findViewById<TextView>(R.id.txtCommentsStatus)
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseComments)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewComments)
        val txtHeader = view.findViewById<TextView>(R.id.txtCommentsHeader)

        btnClose.setOnClickListener { dialog.dismiss() }

        val adapter = CommentsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        dialog.show()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val extractor = service.getCommentsExtractor(videoUrl)
                extractor.fetchPage()

                val items = extractor.initialPage.items.filterIsInstance<CommentsInfoItem>().map {
                    val avatar = it.uploaderAvatars.firstOrNull()?.url.orEmpty()
                    val textContent = it.commentText?.content.orEmpty()

                    CommentModel(
                        author = it.uploaderName.orEmpty(),
                        avatarUrl = avatar,
                        text = textContent,
                        likeCount = it.likeCount,
                        date = it.textualUploadDate.orEmpty()
                    )
                }

                withContext(Dispatchers.Main) {
                    if (items.isEmpty()) {
                        txtStatus.text = "Video này chưa có bình luận hoặc đã tắt bình luận."
                    } else {
                        txtStatus.visibility = View.GONE
                        txtHeader.text = "Bình luận (${items.size}+)"
                        adapter.updateData(items)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    txtStatus.text = "Không thể tải bình luận: ${e.localizedMessage}"
                }
            }
        }
    }

    private class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
        private val list = mutableListOf<CommentModel>()

        fun updateData(newItems: List<CommentModel>) {
            list.clear()
            list.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(v)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imgAvatar: ImageView = itemView.findViewById(R.id.imgCommentAuthor)
            private val txtAuthor: TextView = itemView.findViewById(R.id.txtCommentAuthor)
            private val txtDate: TextView = itemView.findViewById(R.id.txtCommentDate)
            private val txtBody: TextView = itemView.findViewById(R.id.txtCommentBody)
            private val txtLikes: TextView = itemView.findViewById(R.id.txtCommentLikes)

            fun bind(item: CommentModel) {
                txtAuthor.text = item.author
                txtDate.text = item.date
                txtBody.text = item.text
                txtLikes.text = if (item.likeCount > 0) "👍 ${item.likeCount}" else ""
                if (item.avatarUrl.isNotEmpty()) {
                    imgAvatar.load(item.avatarUrl) { crossfade(true) }
                }
            }
        }
    }
}
