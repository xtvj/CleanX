package github.xtvj.cleanx.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.android.material.bottomsheet.BottomSheetDialog
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.databinding.DialogBottomAppBinding
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.utils.FileUtils
import github.xtvj.cleanx.utils.ShareContentType
import java.io.File


class SheetDialog constructor(
    context: Context
) : BottomSheetDialog(context) {

    fun setItem(item: AppItem) {
        this.item = item
        binding.item = item
        layoutBinding.item = item
        binding.executePendingBindings()
        layoutBinding.executePendingBindings()
        layoutBinding.root.isClickable = false
        layoutBinding.clAppItem.setBackgroundResource(android.R.color.transparent)
    }

    private var binding: DialogBottomAppBinding = DialogBottomAppBinding.inflate(layoutInflater)
    private var layoutBinding: ItemFragmentAppListBinding = binding.layoutDialog
    private var item: AppItem? = null

    var clickListener: ((item: AppItem, open_run_or_enable: Int, newStatus: Boolean) -> Unit)? =
        null

    init {
        setContentView(binding.root)
        binding.btnOpen.setOnClickListener {
            dismiss()
            clickListener?.invoke(item!!, 1, true)
        }
        binding.btnShare.setOnClickListener {
            dismiss()
            val file = File(item!!.sourceDir)
            val uri: Uri? = FileUtils.getFileUri(context, ShareContentType.FILE, file)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "*/*"
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.share_apk)
                )
            )
        }
        binding.btnDetail.setOnClickListener {
            dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", item!!.id, null)
            context.startActivity(intent)
        }
        binding.btnUnInstall.setOnClickListener {
            dismiss()
            val intent = Intent()
            intent.action = Intent.ACTION_DELETE
            intent.data = Uri.parse("package:${item!!.id}")
            context.startActivity(intent)
        }
        binding.btnFreeze.setOnClickListener {
            dismiss()
            clickListener?.invoke(item!!, 3, !item!!.isEnable)
        }
        binding.btnRunning.setOnClickListener {
            dismiss()
            clickListener?.invoke(item!!, 2, false)
        }
    }

}