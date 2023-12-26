package com.hexterlabs.listdetail.ui.venue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.databinding.FragmentVenueBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenueFragment : Fragment() {

    private val venueViewModel by viewModels<VenueViewModel>()

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the VenueFragment
     * to enable Data Binding to observe StateFlow.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentVenueBinding>(
            inflater,
            R.layout.fragment_venue,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = venueViewModel
            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }
        }
        return binding.root
    }
}