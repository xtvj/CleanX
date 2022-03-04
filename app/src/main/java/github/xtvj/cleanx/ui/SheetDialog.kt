package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.databinding.DialogBottomAppBinding
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class SheetDialog : BottomSheetDialogFragment() {


    companion object {
        private const val KEY_ITEM = "github.xtvj.cleanx.KEY_ITEM_FRAGMENT"
        fun create(item: AppItem) =
            SheetDialog().apply {
                arguments = Bundle(1).apply {
                    putParcelable(KEY_ITEM, item)
                }
            }
    }

    private lateinit var binding: DialogBottomAppBinding
    private lateinit var layoutBinding: ItemFragmentAppListBinding
    private lateinit var item: AppItem

    @Inject
    lateinit var fragmentContext: Context //Application Context

    @Inject
    lateinit var toastUtils: ToastUtils

    @Inject
    lateinit var pm: PackageManager

    @Inject
    lateinit var appItemDao: AppItemDao

    var clickListener: ((string: String) -> Unit)? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        item = arguments?.getParcelable(KEY_ITEM) ?: throw IllegalStateException()

        binding = DialogBottomAppBinding.inflate(inflater, container, false)
        layoutBinding = binding.layoutDialog

        return binding.root

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutBinding.root.isClickable = false
        layoutBinding.tvAppId.text = item.id
        layoutBinding.tvAppName.text = item.name
        layoutBinding.tvAppVersion.text =
            getString(R.string.version) + item.version + " (" + item.versionCode + ")"
        layoutBinding.tvUpdateTime.text =
            getString(R.string.update_time) + DateUtil.format(item.lastUpdateTime)

        if (item.icon != 0) {
            val uri = Uri.parse("android.resource://" + item.id + "/" + item.icon)
            layoutBinding.ivIcon.loadImage(uri)
        } else {
            layoutBinding.ivIcon.loadImage(R.drawable.ic_default_round)
        }

        layoutBinding.ivIsEnable.visibility = if (item.isEnable) View.INVISIBLE else View.VISIBLE
        binding.btnRunning.visibility = if (item.isRunning) View.VISIBLE else View.GONE
        binding.btnUnInstall.visibility = if (item.isSystem) View.GONE else View.VISIBLE
        binding.btnFreeze.visibility =
            if (RunnerUtils.isRootAvailable()) View.VISIBLE else View.GONE

        binding.btnFreeze.text =
            if (item.isEnable) getString(R.string.disable) else getString(
                R.string.enable
            )

        binding.btnOpen.setOnClickListener {
            if (item.isEnable) {
                val intent = pm.getLaunchIntentForPackage(item.id)
                if (intent != null) {
                    lifecycleScope.launch {
                        appItemDao.updateRunning(item.id, true)
                    }
                    context?.startActivity(intent)
                } else {
                    toastUtils.toast(R.string.app_no_intent)
                }
            } else {
                toastUtils.toast(item.name + getString(R.string.disabled_can_not_open))
            }
            dismissAllowingStateLoss()
        }
        binding.btnShare.setOnClickListener {
            val file = File(item.sourceDir)
            val uri: Uri? = FileUtils.getFileUri(fragmentContext, ShareContentType.FILE, file)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "*/*"
            context?.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context?.resources?.getString(R.string.share_apk)
                )
            )
            dismissAllowingStateLoss()
        }
        binding.btnDetail.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", item.id, null)
            context?.startActivity(intent)
            dismissAllowingStateLoss()
        }
        binding.btnUnInstall.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_DELETE
            intent.data = Uri.parse("package:${item.id}")
            context?.startActivity(intent)
            dismissAllowingStateLoss()
        }
        binding.btnFreeze.setOnClickListener {

            lifecycleScope.launch {
                val hasRoot = RunnerUtils.isRootGiven()
                if (hasRoot) {
                    val cmd = (if (item.isEnable) PM_DISABLE else PM_ENABLE) + item.id
                    val result = Runner.runCommand(Runner.rootInstance(), cmd)
                    if (result.isSuccessful) {
                        appItemDao.updateEnable(item.id, !item.isEnable)
                    }
                    withContext(Dispatchers.Main) {
                        val toast =
                            (if (item.isEnable) getString(R.string.disable) else getString(R.string.enable)) + item.name + (if (result.isSuccessful) getString(
                                R.string.success
                            ) else getString(R.string.fail))
                        toastUtils.toast(toast)
                    }
                } else {
                    clickListener?.invoke("")
                }
                dismissAllowingStateLoss()
            }
        }
        binding.btnRunning.setOnClickListener {
            lifecycleScope.launch {
                val hasRoot = RunnerUtils.isRootGiven()
                if (hasRoot) {
                    val result = Runner.runCommand(
                        Runner.rootInstance(),
                        FORCE_STOP + item.id
                    )
                    if (result.isSuccessful) {
                        appItemDao.updateRunning(item.id, false)
                    }
                    withContext(Dispatchers.Main) {
                        toastUtils.toast(
                            if (result.isSuccessful) getString(R.string.stop_success)
                            else getString(R.string.stop_failed)
                        )
                    }
                } else {
                    clickListener?.invoke("")
                }
                dismissAllowingStateLoss()
            }
        }
    }


}