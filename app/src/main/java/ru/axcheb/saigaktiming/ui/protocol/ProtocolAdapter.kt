package ru.axcheb.saigaktiming.ui.protocol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.data.model.ui.ProtocolItem
import ru.axcheb.saigaktiming.databinding.ProtocolItemBinding

class ProtocolAdapter :
    ListAdapter<ProtocolItem, ProtocolAdapter.ViewHolder>(ProtocolDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].memberId
    }

    class ViewHolder(val binding: ProtocolItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProtocolItem, position: Int) {
            binding.item = item
            binding.position = position + 1
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProtocolItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position), position)
    }
}

private class ProtocolDiffCallback : DiffUtil.ItemCallback<ProtocolItem>() {
    override fun areItemsTheSame(oldItem: ProtocolItem, newItem: ProtocolItem): Boolean {
        return oldItem.eventMemberId == newItem.eventMemberId
    }

    override fun areContentsTheSame(oldItem: ProtocolItem, newItem: ProtocolItem): Boolean {
        return oldItem == newItem
    }

}