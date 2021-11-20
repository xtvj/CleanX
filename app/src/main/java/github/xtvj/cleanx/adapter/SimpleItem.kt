package github.xtvj.cleanx.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.clear
import coil.load
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ItemAppListFragmentBinding

open class SimpleItem : AbstractBindingItem<ItemAppListFragmentBinding>() {
    private lateinit var id: String
    private lateinit var icon: Drawable
    private lateinit var name: String
    private lateinit var version: String
    private var isSystem: Boolean = false

    override val type: Int
    get() = R.id.fastadapter_icon_item_id

    fun withID(id : String,name: String,version: String, icon: Drawable,isSystem : Boolean) : SimpleItem{
        this.id = id
        this.name = name
        this.version = version
        this.icon = icon
        this.isSystem = isSystem
        return this
    }

    override fun bindView(binding: ItemAppListFragmentBinding, payloads: List<Any>) {
        binding.tvAppId.text = id
        binding.tvAppName.text = name
        binding.ivIcon.load(icon)
        binding.tvAppVersion.text = version
        binding.ivIsSystem.visibility = if (isSystem) View.VISIBLE else View.GONE
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemAppListFragmentBinding {
        return ItemAppListFragmentBinding.inflate(inflater, parent, false)
    }

    override fun unbindView(binding: ItemAppListFragmentBinding) {
        super.unbindView(binding)
        binding.tvAppId.text = null
        binding.tvAppName.text = null
        binding.tvAppVersion.text = null
        binding.ivIcon.clear()
        binding.ivIcon.setImageDrawable(null)
    }

}

