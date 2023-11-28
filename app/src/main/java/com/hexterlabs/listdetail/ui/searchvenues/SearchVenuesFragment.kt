package com.hexterlabs.listdetail.ui.searchvenues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.databinding.FragmentSearchVenuesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchVenuesFragment : Fragment() {

    private val searchVenuesViewModel by viewModels<SearchVenuesViewModel>()

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the VenuesListFragment
     * to enable Data Binding to observe LiveData.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = SearchVenuesAdapter { onVenueClicked(it) }
        val binding = DataBindingUtil.inflate<FragmentSearchVenuesBinding>(
            inflater,
            R.layout.fragment_search_venues,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = searchVenuesViewModel
            searchVenuesSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    searchVenuesViewModel.searchVenues(newText.trim())
                    return true
                }
            })
            searchVenuesList.adapter = adapter
        }
        searchVenuesViewModel.searchVenuesResults.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
            }
        }
        return binding.root
    }

    private fun onVenueClicked(id: String) {
        val action = SearchVenuesFragmentDirections.actionSearchVenuesFragmentToVenueFragment(id)
        findNavController().navigate(action)
    }
}