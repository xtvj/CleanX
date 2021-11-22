package github.xtvj.cleanx.ui.custom

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import github.xtvj.cleanx.adapter.AppItem
import github.xtvj.cleanx.databinding.BottomDialogAppBinding
import github.xtvj.cleanx.databinding.ItemAppListFragmentBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.log
import javax.inject.Inject


class SheetDialog @Inject constructor(context: Context, private val imageLoaderX :ImageLoaderX, val item: AppItem) : BottomSheetDialog(context) {


    private lateinit var binding: BottomDialogAppBinding
    private lateinit var layoutBinding: ItemAppListFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomDialogAppBinding.inflate(layoutInflater)
        layoutBinding = binding.layoutDialog
        layoutBinding.root.isClickable = false
        layoutBinding.tvAppId.text = item.id
        layoutBinding.tvAppName.text = item.name
        layoutBinding.tvAppVersion.text = item.version
        imageLoaderX.displayImage(item.id!!, layoutBinding.ivIcon)
        layoutBinding.ivIsSystem.visibility = View.GONE


        binding.tvOpen.setOnClickListener{
            log(item.toString())
            Toast.makeText(
                context,
                "tvOpen id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        binding.tvShare.setOnClickListener{
            Toast.makeText(
                context,
                "tvShare id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        binding.tvDetail.setOnClickListener{
            Toast.makeText(
                context,
                "tvDetail id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        binding.tvUnInstall.setOnClickListener{
            Toast.makeText(
                context,
                "tvUnInstall id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        binding.tvFreeze.setOnClickListener {
            Toast.makeText(
                context,
                "tvFreeze id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        setContentView(binding.root)
    }

}