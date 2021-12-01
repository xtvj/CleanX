package github.xtvj.cleanx.adapter

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.ui.custom.SheetDialog
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import java.util.*
import javax.inject.Inject

open class ListItemAdapter @Inject constructor(
    private val imageLoaderX: ImageLoaderX,
    private val pm: PackageManager,
    private val appItemDao: AppItemDao
) :PagingDataAdapter<AppItem, ListItemAdapter.ItemViewHolder>(diffCallback) {


    private lateinit var selectionTracker: SelectionTracker<Long>

    private lateinit var binding: ItemFragmentAppListBinding

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<AppItem>() {
            override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
                return oldItem.id == newItem.id
            }

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    open fun setSelectionTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionTracker = selectionTracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        binding =
            ItemFragmentAppListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }



//    override fun onBindViewHolder(
//        holder: ItemViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position);
//        } else {
//            onBindItemHolder(holder, position);
//        }
//    }
//
//    private fun onBindItemHolder(holder: ItemViewHolder, position: Int) {
//        val viewType = getItemViewType(position)
//        if (viewType == 0) {
//            (holder).bind(getItem(position),position)
//        }
//    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ItemViewHolder(private val holderBinding: ItemFragmentAppListBinding) : RecyclerView.ViewHolder(binding.root) {
        private val details = ItemDetails()

        @SuppressLint("SetTextI18n")
        fun bind(item: AppItem?, position: Int) {
            if (item == null){
                return
            }
            details.position = position.toLong()
            holderBinding.tvAppId.text = item.id
            holderBinding.tvAppName.text = item.name
            holderBinding.tvAppVersion.text =
                holderBinding.tvAppVersion.context.getString(R.string.version) + item.version
            holderBinding.ivIsEnable.visibility =
                if (item.isEnable) View.INVISIBLE else View.VISIBLE
            holderBinding.tvUpdateTime.text =
                holderBinding.tvUpdateTime.context.getString(R.string.update_time) + DateUtil.format(
                    item.lastUpdateTime
                )
            this@ListItemAdapter.imageLoaderX.displayImage(item.id, holderBinding.ivIcon)

            bindSelectedState()
            holderBinding.cvAppItem.setOnClickListener {
                SheetDialog(holderBinding.cvAppItem.context, imageLoaderX, pm, item,appItemDao).show()
            }
        }

        private fun bindSelectedState() {
            holderBinding.cvAppItem.isChecked =
                this@ListItemAdapter.selectionTracker.isSelected(details.selectionKey)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            return details
        }

    }

    class ItemDetails : ItemDetailsLookup.ItemDetails<Long>() {
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
            val viewHolder = view?.let { recyclerView.getChildViewHolder(it) }
            if (viewHolder is ListItemAdapter.ItemViewHolder) {
                return viewHolder.getItemDetails()
            }
            return null
        }
    }

    class KeyProvider : ItemKeyProvider<Long?>(SCOPE_MAPPED) {
        override fun getKey(position: Int): Long {
            return position.toLong()
        }

        override fun getPosition(@NonNull key: Long): Int {
            return key.toInt()
        }
    }

}
