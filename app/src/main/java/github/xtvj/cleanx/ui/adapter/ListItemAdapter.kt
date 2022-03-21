package github.xtvj.cleanx.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import javax.inject.Inject
import kotlin.properties.Delegates

open class ListItemAdapter @Inject constructor(val context: Context) :
    PagingDataAdapter<AppItem, ListItemAdapter.ItemViewHolder>(diffCallback) {


    private lateinit var selectionTracker: SelectionTracker<AppItem>

    private var type by Delegates.notNull<Int>()

    var itemClickListener: ((appItem: AppItem, position: Int) -> Unit)? = null

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

    open fun setSelectionTracker(selectionTracker: SelectionTracker<AppItem>) {
        this.selectionTracker = selectionTracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemFragmentAppListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            holder.getBinding().executePendingBindings()
        }
    }

    fun getSelectItems(): List<AppItem> {
        val list = mutableListOf<AppItem>()
        for (key in selectionTracker.selection) {
            list.add(key)
        }
        return list
    }

    fun setAdapterType(type: Int) {
        this.type = type
    }


    inner class ItemViewHolder(private val holderBinding: ItemFragmentAppListBinding) :
        RecyclerView.ViewHolder(holderBinding.root) {
        private val details = getItemDetails()

        @SuppressLint("SetTextI18n", "ResourceAsColor")
        fun bind(item: AppItem) {
            holderBinding.item = item
            holderBinding.type = type
            holderBinding.selectionTracker = selectionTracker
            holderBinding.details = details
//            holderBinding.tvAppId.text = item.id
//            holderBinding.tvAppName.text = item.name
//            holderBinding.tvAppVersion.text =
//                context.getString(R.string.version) + item.version + " (" + item.versionCode + ")"
//
//            holderBinding.tvUpdateTime.text =
//                context.getString(R.string.update_time) + item.getFormatUpdateTime()
//            holderBinding.ivIcon.loadImage(item.getIconUri())

            //bindSelectedState
//            holderBinding.cvAppItem.isChecked =
//                this@ListItemAdapter.selectionTracker.isSelected(details.selectionKey)

//            if (!holderBinding.cvAppItem.isChecked && item.isRunning) {
//                holderBinding.clAppItem.setBackgroundResource(R.color.card_View_running)
//            } else {
//                holderBinding.clAppItem.setBackgroundResource(android.R.color.transparent)
//            }

//            if (!item.isEnable && !holderBinding.cvAppItem.isChecked && type != 2) {
//                holderBinding.ivIsEnable.visibility = View.VISIBLE
//            } else {
//                holderBinding.ivIsEnable.visibility = View.INVISIBLE
//            }


            holderBinding.cvAppItem.setOnClickListener {
                itemClickListener?.invoke(item, bindingAdapterPosition)
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<AppItem> =
            object : ItemDetailsLookup.ItemDetails<AppItem>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): AppItem? = getItem(bindingAdapterPosition)
            }
        fun getBinding() : ItemFragmentAppListBinding{
            return holderBinding
        }
    }


    class DetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<AppItem>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<AppItem>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            val viewHolder = view?.let { recyclerView.getChildViewHolder(it) }
            if (viewHolder is ListItemAdapter.ItemViewHolder) {
                return viewHolder.getItemDetails()
            }
            return null
        }
    }

    class KeyProvider(private val adapter: ListItemAdapter) :
        ItemKeyProvider<AppItem>(SCOPE_MAPPED) {
        override fun getKey(position: Int): AppItem? {
            return adapter.getItem(position)
        }

        override fun getPosition(key: AppItem): Int {
            return adapter.snapshot().indexOfFirst { it?.id == key.id }
        }

    }

}
