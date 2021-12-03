package github.xtvj.cleanx.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.DialogBottomAppBinding
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.utils.DateUtil
import github.xtvj.cleanx.utils.FileUtils
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.ShareContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@SuppressLint("SetTextI18n")
class SheetDialog constructor(
    context: Context,
    imageLoaderX: ImageLoaderX,
    pm: PackageManager,
    item: AppItem,
    appItemDao: AppItemDao
) : BottomSheetDialog(context, R.style.ThemeOverlay_MaterialComponents_BottomSheetDialog) {

    private var binding: DialogBottomAppBinding =
        DialogBottomAppBinding.inflate(layoutInflater)
    private var layoutBinding: ItemFragmentAppListBinding = binding.layoutDialog
    private lateinit var rootDialog: AlertDialog

    init {
        layoutBinding.root.isClickable = false
        layoutBinding.tvAppId.text = item.id
        layoutBinding.tvAppName.text = item.name
        layoutBinding.tvAppVersion.text = context.getString(R.string.version) + item.version
        layoutBinding.tvUpdateTime.text =
            context.getString(R.string.update_time) + DateUtil.format(item.lastUpdateTime)

        imageLoaderX.displayImage(item.id, layoutBinding.ivIcon)
        layoutBinding.ivIsEnable.visibility = if (item.isEnable) View.INVISIBLE else View.VISIBLE
        binding.tvUnInstall.visibility = if (item.isSystem) View.GONE else View.VISIBLE
        binding.tvFreeze.visibility = if (RunnerUtils.isRootAvailable()) View.VISIBLE else View.GONE

        binding.tvFreeze.text =
            if (item.isEnable) context.getString(R.string.disable) else context.getString(
                R.string.enable
            )

        binding.tvOpen.setOnClickListener {
            if (item.isEnable){
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
            }else{
                Toast.makeText(context,item.name + "被禁用，无法打",Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
        binding.tvShare.setOnClickListener {
            val file = File(item.sourceDir)
            val uri: Uri? = FileUtils.getFileUri(context, ShareContentType.FILE, file)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "*/*"
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.resources.getString(R.string.share_apk)
                )
            )
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

            CoroutineScope(Dispatchers.IO).launch {
               val given = withContext(Dispatchers.IO){
                        RunnerUtils.isRootGiven()
                    }
                if (given){
                    if (item.isEnable) {
                        //pm disable package
                        val result = Runner.needRoot().runCommand(RunnerUtils.CMD_PM + " disable " + item.id)
                        if (result.isSuccessful){
                            val newItem = AppItem(
                                item.id,
                                item.name,
                                item.version,
                                item.isSystem,
                                false,
                                item.firstInstallTime,
                                item.lastUpdateTime,
                                item.dataDir,
                                item.sourceDir,
                                item.deviceProtectedDataDir,
                                item.publicSourceDir
                            )
                            appItemDao.updateItem(newItem)
                            withContext(Dispatchers.Main){
                                Toast.makeText(context,
                                    "${context.getString(R.string.disable)}${item.name}${
                                        context.getString(R.string.success)
                                    }",Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            withContext(Dispatchers.Main){
                                Toast.makeText(context,
                                    "${context.getString(R.string.disable)}${item.name}${
                                        context.getString(R.string.fail)
                                    }",Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        val result = Runner.needRoot().runCommand(RunnerUtils.CMD_PM + " enable " + item.id)
                        //pm enable package
                        if (result.isSuccessful){
                            val newItem = AppItem(
                                item.id,
                                item.name,
                                item.version,
                                item.isSystem,
                                true,
                                item.firstInstallTime,
                                item.lastUpdateTime,
                                item.dataDir,
                                item.sourceDir,
                                item.deviceProtectedDataDir,
                                item.publicSourceDir
                            )
                            appItemDao.updateItem(newItem)
                            withContext(Dispatchers.Main){
                                Toast.makeText(context,
                                    "${context.getString(R.string.enable)}${item.name}${
                                        context.getString(R.string.success)
                                    }",Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            withContext(Dispatchers.Main){
                                Toast.makeText(context,
                                    "${context.getString(R.string.enable)}${item.name}${
                                        context.getString(R.string.fail)
                                    }",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    withContext(Dispatchers.Main){
                        initDialog()
                        rootDialog.show()
                    }
                }
            }
            dismiss()
        }
        setContentView(binding.root)

    }

    private fun initDialog() {
        val view =
            layoutInflater.inflate(R.layout.dialog_request_root, null, false) as LinearLayoutCompat
        val textView = view.findViewById<MaterialTextView>(R.id.tv_quest_root)
        textView.text = HtmlCompat.fromHtml(
            context.getString(R.string.request_root_message),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        textView.movementMethod = LinkMovementMethod.getInstance()
        rootDialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setPositiveButton(context.getString(R.string.request_root_ok)) { dialog, _ ->
                Toast.makeText(context,context.getString(R.string.need_to_open_root),Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.request_root_cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()
    }


}