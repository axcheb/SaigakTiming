package ru.axcheb.saigaktiming.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.ui.EventMemberItem
import ru.axcheb.saigaktiming.databinding.EventMemberItemBinding

class EventMemberAdapter() :
    ListAdapter<EventMemberItem, EventMemberAdapter.ViewHolder>(EventMemberDiffCallback()) {

    class ViewHolder(val binding: EventMemberItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.editImg.setOnClickListener {
                binding.item?.let { item -> navigateToMemberResult(item, it) }
            }
        }

        fun bind(member: EventMemberItem) {
            binding.item = member
            if (member.isNext) {
                binding.sequenceNumber.setTextColor(
                    ContextCompat.getColor(
                        binding.sequenceNumber.context,
                        R.color.deep_orange_500
                    )
                )
            }
            binding.executePendingBindings()
        }

        private fun navigateToMemberResult(item: EventMemberItem, view: View) {
            val direction = EventFragmentDirections.actionNavigationEventToNavigationStart(
                item.eventId,
                item.memberId
            )
            view.findNavController().navigate(direction)
        }

    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EventMemberItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }
}

private class EventMemberDiffCallback : DiffUtil.ItemCallback<EventMemberItem>() {
    override fun areItemsTheSame(oldItem: EventMemberItem, newItem: EventMemberItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EventMemberItem, newItem: EventMemberItem): Boolean {
        return oldItem == newItem
    }

}