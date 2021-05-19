package ru.axcheb.saigaktiming.ui.memberselect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem
import ru.axcheb.saigaktiming.databinding.MemberSelectItemBinding

class MemberSelectAdapter(
    private val bindMemberListener: (MemberSelectItem) -> Unit
) :
    ListAdapter<MemberSelectItem, MemberSelectAdapter.ViewHolder>(MemberDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(val binding: MemberSelectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: MemberSelectItem) {
            binding.item = member
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MemberSelectItemBinding.inflate(inflater, parent, false)
        binding.setClickListener {
            binding.item?.let { bindMemberListener.invoke(it) }
        }
        return ViewHolder(binding)
    }
}

private class MemberDiffCallback : DiffUtil.ItemCallback<MemberSelectItem>() {
    override fun areItemsTheSame(oldItem: MemberSelectItem, newItem: MemberSelectItem): Boolean {
        return oldItem.memberId == newItem.memberId
    }

    override fun areContentsTheSame(oldItem: MemberSelectItem, newItem: MemberSelectItem): Boolean {
        return oldItem == newItem
    }

}