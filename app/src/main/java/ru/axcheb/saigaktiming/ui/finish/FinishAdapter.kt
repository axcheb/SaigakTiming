package ru.axcheb.saigaktiming.ui.finish

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.data.model.ui.FinishItem
import ru.axcheb.saigaktiming.databinding.FinishItemBinding

class FinishAdapter(
    private val finishActiveListener: (FinishItem) -> Unit
) :
    ListAdapter<FinishItem, FinishAdapter.ViewHolder>(FinishItemDiffCallback()) {

    init {
        // id все постоянные + переопределён метод getItemID
        setHasStableIds(true)
    }

    class ViewHolder(val binding: FinishItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resultItem: FinishItem) {
            binding.item = resultItem
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FinishItemBinding.inflate(inflater, parent, false)
        binding.check.setOnClickListener {
            binding.item?.let { finishActiveListener.invoke(it) }
        }

        return ViewHolder(binding)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].id
    }
}

private class FinishItemDiffCallback : DiffUtil.ItemCallback<FinishItem>() {
    override fun areItemsTheSame(oldItem: FinishItem, newItem: FinishItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FinishItem, newItem: FinishItem): Boolean {
        return oldItem == newItem
    }

}