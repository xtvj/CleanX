package github.xtvj.cleanx.ui.custom

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
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
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
    lateinit var fragmentContext: Context

    @Inject
    lateinit var imageLoaderX: ImageLoaderX

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
        layoutBinding.tvAppVersion.text = getString(R.string.version) + item.version
        layoutBinding.tvUpdateTime.text =
            getString(R.string.update_time) + DateUtil.format(item.lastUpdateTime)

        imageLoaderX.displayImage(item.id, layoutBinding.ivIcon)
        layoutBinding.ivIsEnable.visibility = if (item.isEnable) View.INVISIBLE else View.VISIBLE
        binding.tvUnInstall.visibility = if (item.isSystem) View.GONE else View.VISIBLE
        binding.tvFreeze.visibility = if (RunnerUtils.isRootAvailable()) View.VISIBLE else View.GONE

        binding.tvFreeze.text =
            if (item.isEnable) getString(R.string.disable) else getString(
                R.string.enable
            )

        binding.tvOpen.setOnClickListener {
            if (item.isEnable) {
                val intent = pm.getLaunchIntentForPackage(item.id)
                if (intent != null) {
                    context?.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.app_no_intent),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, item.name + "被禁用，无法打", Toast.LENGTH_SHORT).show()
            }
            dismissAllowingStateLoss()
        }
        binding.tvShare.setOnClickListener {
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
        binding.tvDetail.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", item.id, null)
            context?.startActivity(intent)
            dismissAllowingStateLoss()
        }
        binding.tvUnInstall.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_DELETE
            intent.data = Uri.parse("package:${item.id}")
            context?.startActivity(intent)
            dismissAllowingStateLoss()
        }
        binding.tvFreeze.setOnClickListener {

            //如果使用lifecycleScope，UI不更新
            CoroutineScope(Dispatchers.IO).launch {

                val hasRoot = RunnerUtils.isRootGiven()

                if (hasRoot) {
                    if (item.isEnable) {
                        //pm disable package
                        val result = Runner.runCommand(
                            Runner.rootInstance(),
                            RunnerUtils.CMD_PM + " disable " + item.id
                        )
                        if (result.isSuccessful) {
                            appItemDao.update(item.id, false)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    fragmentContext,
                                    "${getString(R.string.disable)}${item.name}${
                                        getString(R.string.success)
                                    }", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    fragmentContext,
                                    "${getString(R.string.disable)}${item.name}${
                                        getString(R.string.fail)
                                    }", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val result = Runner.runCommand(
                            Runner.rootInstance(),
                            RunnerUtils.CMD_PM + " enable " + item.id
                        )
                        //pm enable package
                        if (result.isSuccessful) {
                            appItemDao.update(item.id, true)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    fragmentContext,
                                    "${getString(R.string.enable)}${item.name}${
                                        getString(R.string.success)
                                    }", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    fragmentContext,
                                    "${getString(R.string.enable)}${item.name}${
                                        getString(R.string.fail)
                                    }", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    clickListener?.invoke("")
                }
            }
            dismissAllowingStateLoss()
        }
    }


}