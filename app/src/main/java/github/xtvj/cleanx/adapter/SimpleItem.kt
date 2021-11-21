package github.xtvj.cleanx.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.FastAdapterUIUtils
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ItemAppListFragmentBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX

open class SimpleItem constructor(private val imageLoaderX: ImageLoaderX): AbstractBindingItem<ItemAppListFragmentBinding>() {
    var id: String? = null
    var icon: Drawable? = null
    var name: String? = null
    var version: String? = null
    var isSystem: Boolean = false

    override val type: Int
    get() = R.id.fastadapter_icon_item_id

    fun withID(id : String, name: String, version: String, isSystem : Boolean){
        this.id = id
        this.name = name
        this.version = version
        this.isSystem = isSystem
    }

    @SuppressLint("ResourceAsColor")
    override fun bindView(binding: ItemAppListFragmentBinding, payloads: List<Any>) {

            binding.tvAppId.text = id
            binding.tvAppName.text = name
            binding.tvAppVersion.text = version
            binding.ivIsSystem.visibility = if (isSystem) View.VISIBLE else View.GONE
//            val appInfo = pm.getPackageInfo(id!!,0)
//            icon = appInfo.applicationInfo.loadIcon(pm)
//            binding.ivIcon.load(icon)
            if (!id.isNullOrEmpty()){
                imageLoaderX.displayImage(id!!,binding.ivIcon)
            }

            binding.root.background = FastAdapterUIUtils.getSelectableBackground(binding.root.context, R.color.purple_200, true)

    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemAppListFragmentBinding {
        return ItemAppListFragmentBinding.inflate(inflater, parent, false)
    }

    override fun unbindView(binding: ItemAppListFragmentBinding) {
        super.unbindView(binding)
        icon = null
        binding.tvAppId.text = null
        binding.tvAppName.text = null
        binding.tvAppVersion.text = null
    }

}

