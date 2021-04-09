package ru.axcheb.saigaktiming.ui.finish

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.databinding.FinishItemBinding

class FinishAdapter(
    private val finishActiveListener: (ResultItem) -> Unit
) :
    ListAdapter<ResultItem, FinishAdapter.ViewHolder>(MemberFinishDiffCallback()) {

    init {
        // id все постоянные + переопределён метод getItemID
        setHasStableIds(true)
    }

    class ViewHolder(val binding: FinishItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resultItem: ResultItem) {
            binding.item = resultItem
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: FinishItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.finish_item, parent, false)

        binding.check.setOnClickListener {
            binding.item?.let { finishActiveListener.invoke(it) }
        }

        return ViewHolder(binding)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].id
    }
}

private class MemberFinishDiffCallback : DiffUtil.ItemCallback<ResultItem>() {
    override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem == newItem
    }

}