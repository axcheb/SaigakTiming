package ru.axcheb.saigaktiming.ui.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.data.model.ui.ArchiveItem
import ru.axcheb.saigaktiming.databinding.ArchiveItemBinding

class ArchiveAdapter(
    private val itemClickListener: (ArchiveItem) -> Unit
) :
    ListAdapter<ArchiveItem, ArchiveAdapter.ViewHolder>(ArchiveItemDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(val binding: ArchiveItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArchiveItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArchiveItemBinding.inflate(inflater, parent, false)
        binding.setClickListener {
            binding.item?.let {
                itemClickListener.invoke(it)
            }
        }
        return ViewHolder(binding)
    }

}

private class ArchiveItemDiffCallback : DiffUtil.ItemCallback<ArchiveItem>() {
    override fun areItemsTheSame(oldItem: ArchiveItem, newItem: ArchiveItem): Boolean {
        return oldItem.eventId == newItem.eventId
    }

    override fun areContentsTheSame(oldItem: ArchiveItem, newItem: ArchiveItem): Boolean {
        return oldItem == newItem
    }

}