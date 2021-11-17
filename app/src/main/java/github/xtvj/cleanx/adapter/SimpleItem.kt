package github.xtvj.cleanx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ItemAppListFragmentBinding

open class SimpleItem : AbstractBindingItem<ItemAppListFragmentBinding>() {
    var id: String? = null

    override val type: Int
    get() = R.id.fastadapter_icon_item_id

    fun withID(id : String) : SimpleItem{
        this.id = id
        return this
    }

    override fun bindView(binding: ItemAppListFragmentBinding, payloads: List<Any>) {
        binding.tvAppId.text = id
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemAppListFragmentBinding {
        return ItemAppListFragmentBinding.inflate(inflater, parent, false)
    }
}

