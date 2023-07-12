package com.example.pianoanalytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.pianoanalytics.databinding.ListItemBinding

class MainAdapter(
    private val data: List<String>,
    private val clickListener: (String) -> Unit
)  : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false),
        clickListener
    )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class ViewHolder(view: View, clickListener: (String) -> Unit) : RecyclerView.ViewHolder(view) {
        private val binding: ListItemBinding by viewBinding()
        lateinit var item: String

        init {
            binding.root.setOnClickListener {clickListener(item) }
        }

        fun bind(data: String) {
            item = data
            binding.title.text = data
        }
    }
}
