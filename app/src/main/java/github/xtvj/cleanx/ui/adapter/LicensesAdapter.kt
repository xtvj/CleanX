package github.xtvj.cleanx.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import github.xtvj.cleanx.data.License
import github.xtvj.cleanx.databinding.ItemFragmentLicensesBinding
import javax.inject.Inject

open class LicensesAdapter @Inject constructor() :
    PagingDataAdapter<License, LicensesAdapter.ItemViewHolder>(diff) {

    var onItemClick: ((License) -> Unit)? = null

    companion object {
        val diff = object : DiffUtil.ItemCallback<License>() {
            override fun areItemsTheSame(oldItem: License, newItem: License): Boolean {
                return oldItem.address == newItem.address
            }
            override fun areContentsTheSame(oldItem: License, newItem: License): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ItemViewHolder(private val binding: ItemFragmentLicensesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: License) {
            binding.about.text = item.about
            binding.address.text = item.address
            binding.version.text = item.version
            binding.name.text = item.name
            binding.author.text = item.author
            binding.license.text = item.license
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemFragmentLicensesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }
}