package ru.axcheb.saigaktiming.ui.start

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.data.model.ui.StartItem
import ru.axcheb.saigaktiming.databinding.StartItemBinding
import ru.axcheb.saigaktiming.ui.finish.FinishActivity

class StartAdapter(
    private val eventId: Long,
    private val memberId: Long,
    private val startActiveListener: (StartItem) -> Unit
) :
    ListAdapter<StartItem, StartAdapter.ViewHolder>(StartItemDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(val binding: StartItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StartItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StartItemBinding.inflate(inflater, parent, false)

        binding.setClickListener { view ->
            val startId = binding.item?.id
            startId?.let { startId_ -> navigateToMemberStart(startId_, view) }
        }

        binding.check.setOnClickListener {
            binding.item?.let { startActiveListener.invoke(it) }
        }

        return ViewHolder(binding)
    }

    private fun navigateToMemberStart(startId: Long, view: View) {
        FinishActivity.start(eventId, memberId, startId, view.context)
    }

    override fun getItemId(position: Int) = getItem(position).id

}

private class StartItemDiffCallback : DiffUtil.ItemCallback<StartItem>() {
    override fun areItemsTheSame(oldItem: StartItem, newItem: StartItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StartItem, newItem: StartItem): Boolean {
        return oldItem == newItem
    }

}
