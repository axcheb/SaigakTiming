package ru.axcheb.saigaktiming.ui.start

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.ui.ResultItem
import ru.axcheb.saigaktiming.databinding.StartItemBinding
import ru.axcheb.saigaktiming.ui.finish.FinishActivity

class StartAdapter(
    private val eventId: Long,
    private val memberId: Long,
    private val startActiveListener: (ResultItem) -> Unit
) :
    ListAdapter<ResultItem, StartAdapter.ViewHolder>(MemberResultDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(val binding: StartItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResultItem) {
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
        val binding: StartItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.start_item, parent, false)

        binding.edit.setOnClickListener {view ->
            val startId = binding.item?.id
            startId?.let { startId_ -> navigateToMemberStart(startId_, view) }
        }

        binding.check.setOnClickListener {
            binding.item?.let { startActiveListener.invoke(it) }
        }

        return ViewHolder(binding)
    }

    fun navigateToMemberStart(startId: Long, view: View) {
        FinishActivity.start(eventId, memberId, startId, view.context)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].id
    }
}

private class MemberResultDiffCallback : DiffUtil.ItemCallback<ResultItem>() {
    override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
        return oldItem == newItem
    }

}
