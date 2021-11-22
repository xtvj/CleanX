package github.xtvj.cleanx.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import github.xtvj.cleanx.databinding.ItemAppListFragmentBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import java.util.*

open class ListItemAdapter constructor(private val imageLoaderX: ImageLoaderX):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<AppItem> = ArrayList()
    private lateinit var selectionTracker: SelectionTracker<Long>

    private lateinit var binding : ItemAppListFragmentBinding


    @SuppressLint("NotifyDataSetChanged")
    open fun setItems(items: List<AppItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    open fun setSelectionTracker(selectionTracker : SelectionTracker<Long>) {
        this.selectionTracker = selectionTracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ItemAppListFragmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: AppItem = items[position]
        (holder as ItemViewHolder).bind(item, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    inner class ItemViewHolder(private val binding: ItemAppListFragmentBinding) : RecyclerView.ViewHolder(binding.root) {
        private val details: Details = Details()

        fun bind(item : AppItem, position: Int) {
            details.position = position.toLong()
            binding.tvAppId.text = item.id
            binding.tvAppName.text = item.name
            binding.tvAppVersion.text = item.version
            binding.ivIsSystem.visibility = if (item.isSystem) View.VISIBLE else View.GONE
            if (!item.id.isNullOrEmpty()){
                this@ListItemAdapter.imageLoaderX.displayImage(item.id!!,binding.ivIcon)
            }
            bindSelectedState()
        }

        private fun bindSelectedState() {
            binding.cvAppItem.isChecked = this@ListItemAdapter.selectionTracker.isSelected(details.selectionKey)
        }

        fun getItemDetails() : ItemDetailsLookup.ItemDetails<Long> {
            return details
        }

    }

    class Details : ItemDetailsLookup.ItemDetails<Long>() {
        var position: Long = 0
        override fun getPosition(): Int {
            return position.toInt()
        }

        override fun getSelectionKey(): Long {
            return position
        }

        override fun inSelectionHotspot(e: MotionEvent): Boolean {
            return false
        }

        override fun inDragRegion(e: MotionEvent): Boolean {
            return true
        }
    }


    class DetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
                val viewHolder = view?.let { recyclerView.getChildViewHolder(it)}
                if (viewHolder is ItemViewHolder) {
                    return viewHolder.getItemDetails()
                }
            return null
        }
    }

    class KeyProvider(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) :
        ItemKeyProvider<Long?>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return position.toLong()
        }

        override fun getPosition(@NonNull key: Long): Int {
            return key.toInt()
        }
    }

}

