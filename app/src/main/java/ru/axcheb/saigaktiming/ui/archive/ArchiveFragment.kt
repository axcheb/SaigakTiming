package ru.axcheb.saigaktiming.ui.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.artemchep.bindin.bindIn
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.axcheb.saigaktiming.data.model.ui.ArchiveItem
import ru.axcheb.saigaktiming.databinding.ArchiveFragmentBinding

class ArchiveFragment : Fragment() {

    private val viewModel: ArchiveViewModel by viewModel()

    private val adapter: ArchiveAdapter by inject { parametersOf(::itemClickListener) }

    private fun itemClickListener(item: ArchiveItem) {
        val direction = ArchiveFragmentDirections.actionNavigationArchiveToProtocolFragment(item.eventId)
        view?.findNavController()?.navigate(direction)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = ArchiveFragmentBinding.inflate(inflater, container, false)
        binding.archiveRecycler.adapter = adapter
        observeData()
        return binding.root
    }

    private fun observeData() {
        viewLifecycleOwner.bindIn(viewModel.archivedItems) {
            adapter.submitList(it)
        }
    }
}