package github.xtvj.cleanx.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
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
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.utils.loadImage
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

open class ListItemAdapter @Inject constructor(val context: Context) :
    PagingDataAdapter<AppItem, ListItemAdapter.ItemViewHolder>(diffCallback) {


    private lateinit var selectionTracker: SelectionTracker<Long>

    private lateinit var binding: ItemFragmentAppListBinding

    private var type by Delegates.notNull<Int>()

    var itemClickListener: ((appItem: AppItem) -> Unit)? = null

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

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, position)
        }
    }

    fun getSelectItems(): List<AppItem> {
        val list = mutableListOf<AppItem>()
        for (position in selectionTracker.selection) {
            val item = getItem(position.toInt())
            list.add(item!!)
        }
        return list
    }

    fun setAdapterType(type: Int) {
        this.type = type
    }


    inner class ItemViewHolder(private val holderBinding: ItemFragmentAppListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val details = ItemDetails()

        @SuppressLint("SetTextI18n", "ResourceAsColor")
        fun bind(item: AppItem, position: Int) {
            details.position = position.toLong()
            holderBinding.tvAppId.text = item.id
            holderBinding.tvAppName.text = item.name
            holderBinding.tvAppVersion.text =
                context.getString(R.string.version) + item.version

            holderBinding.ivIsEnable.visibility =
                if (type == 2 || item.isEnable) View.INVISIBLE else View.VISIBLE
            holderBinding.tvUpdateTime.text =
                context.getString(R.string.update_time) + item.lastUpdateTime

            if (item.icon != 0) {
                val uri = Uri.parse("android.resource://" + item.id + "/" + item.icon)
                holderBinding.ivIcon.loadImage(uri)
            } else {
                holderBinding.ivIcon.loadImage(R.drawable.ic_default_round)
            }

            if (item.isRunning){
                holderBinding.cvAppItem.setCardBackgroundColor(context.getColorStateList(R.color.running_card_view_background))
            }else{
                holderBinding.cvAppItem.setCardBackgroundColor(context.getColorStateList(R.color.selector_card_view_background))
            }

            bindSelectedState()
            holderBinding.cvAppItem.setOnClickListener {
                itemClickListener?.invoke(item)
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
