package github.xtvj.cleanx.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.databinding.DialogBottomAppBinding
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX


@SuppressLint("SetTextI18n")
class SheetDialog constructor(
    context: Context,
    imageLoaderX: ImageLoaderX,
    pm: PackageManager,
    item: AppItem
) : BottomSheetDialog(context, R.style.ThemeOverlay_MaterialComponents_BottomSheetDialog) {

    private var binding: DialogBottomAppBinding =
        DialogBottomAppBinding.inflate(layoutInflater)
    private var layoutBinding: ItemFragmentAppListBinding = binding.layoutDialog


    init {
        layoutBinding.root.isClickable = false
        layoutBinding.tvAppId.text = item.id
        layoutBinding.tvAppName.text = item.name
        layoutBinding.tvAppVersion.text = context.getString(R.string.version) + item.version
        layoutBinding.tvUpdateTime.text = context.getString(R.string.update_time) + DateUtil.format(item.lastUpdateTime)

        imageLoaderX.displayImage(item.id, layoutBinding.ivIcon)
        layoutBinding.ivIsEnable.visibility = if (item.isEnable) View.INVISIBLE else View.VISIBLE

        binding.tvFreeze.text =
            if (item.isEnable) context.getString(R.string.disable) else context.getString(
                R.string.enable
            )

        binding.tvOpen.setOnClickListener {
            val intent = pm.getLaunchIntentForPackage(item.id)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.app_no_intent),
                    Toast.LENGTH_SHORT
                ).show()
            }
            dismiss()
        }
        binding.tvShare.setOnClickListener {
            Toast.makeText(
                context,
                "tvShare id: ${item.id}  name: ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
        binding.tvDetail.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", item.id, null)
            context.startActivity(intent)
            dismiss()
        }
        binding.tvUnInstall.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_DELETE
            intent.data = Uri.parse("package:${item.id}")
            context.startActivity(intent)
            dismiss()
        }
        binding.tvFreeze.setOnClickListener {
            if (item.isEnable){
                //pm disable package
            }else{
                //pm enable package
            }
            //更新数据到主ui
            dismiss()
        }
        setContentView(binding.root)

    }


//    companion object {
//        class SheetFactory @Inject constructor(
//            private val context: Context,
//            private val imageLoaderX: ImageLoaderX,
//            private val pm: PackageManager
//        ) {
//            fun create(appItem: AppItem) = SheetDialog(context, imageLoaderX, pm, appItem)
//        }
//    }

}