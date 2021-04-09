package ru.axcheb.saigaktiming.ui.memberselect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.R
import ru.axcheb.saigaktiming.data.model.ui.MemberSelectItem
import ru.axcheb.saigaktiming.databinding.MemberSelectActivityBinding

class MemberSelectActivity : AppCompatActivity() {

    private val TAG = this::class.qualifiedName

    private val viewModel: MemberSelectViewModel by viewModel {
        parametersOf(
            intent.getLongExtra(
                EVENT_ID_EXTRA,
                0
            )
        )
    }
// TODO убрать. Оставлено для примера
//    private val bindMemberListener: (MemberSelectItem) -> Unit = {
//        viewModel.handleBind(it.memberId)
//    }

    private fun bindMemberListener(item: MemberSelectItem) {
        viewModel.handleBind(item.memberId)
    }

    private val adapter: MemberSelectAdapter by inject { parametersOf(::bindMemberListener) }

    private var _binding: MemberSelectActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.member_select_activity)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        binding.allMembersItemRecycler.adapter = adapter
        setListeners()
        observeData()
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { view ->
            onBackPressed() // TODO настроить навигацию?
//            view.findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener {
            val newMemberDialog = NewMemberDialogFragment(viewModel.eventId)
            newMemberDialog.show(supportFragmentManager, NEW_MEMBER_DIALOG_TAG)
            true
        }
    }

    private fun observeData() {
        viewModel.members.observe(this, { members ->
            adapter.submitList(members)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val EVENT_ID_EXTRA = "EVENT_ID"
        private const val NEW_MEMBER_DIALOG_TAG = "NEW_MEMBER_DIALOG"

        fun start(eventId: Long, context: Context) {
            val intent = Intent(context, MemberSelectActivity::class.java)
            intent.putExtra(EVENT_ID_EXTRA, eventId)
            context.startActivity(intent)
        }
    }

}