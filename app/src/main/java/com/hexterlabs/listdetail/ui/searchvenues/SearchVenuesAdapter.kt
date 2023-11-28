package com.hexterlabs.listdetail.ui.searchvenues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hexterlabs.listdetail.databinding.ListItemVenueBinding
import com.hexterlabs.listdetail.domain.SearchVenuesResult

/**
 * Adapts [SearchVenuesResult]s from the data layer to be displayed in a [RecyclerView].
 */
class SearchVenuesAdapter(
    private val onClickListener: OnClickListener
) : ListAdapter<SearchVenuesResult, SearchVenuesAdapter.ViewHolder>(VenueDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListItemVenueBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchVenuesResult: SearchVenuesResult, onClickListener: OnClickListener) {
            binding.searchVenuesResult = searchVenuesResult
            binding.onClickListener = onClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemVenueBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

typealias OnClickListener = (String) -> Unit

class VenueDiffCallback : DiffUtil.ItemCallback<SearchVenuesResult>() {
    override fun areItemsTheSame(oldItem: SearchVenuesResult, newItem: SearchVenuesResult): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SearchVenuesResult, newItem: SearchVenuesResult): Boolean {
        return oldItem == newItem
    }
}