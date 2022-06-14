package github.xtvj.cleanx.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.DialogBottomAppBinding
import github.xtvj.cleanx.databinding.ItemFragmentAppListBinding
import github.xtvj.cleanx.shell.Runner
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.ui.viewmodel.MainViewModel
import github.xtvj.cleanx.ui.viewmodel.SheetViewModel
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SheetDialog() : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomAppBinding
    private lateinit var layoutBinding: ItemFragmentAppListBinding
    private lateinit var sheetItem: AppItem

    @Inject
    lateinit var appItemDao: AppItemDao

    @Inject
    lateinit var pm: PackageManager


    companion object {
        private const val PACKAGE_NAME = "packageName"
    }

    constructor(packageName: String) : this() {
        arguments = Bundle().apply {
            putString(PACKAGE_NAME, packageName)
        }
    }

    private val packageName: String
        get() = requireArguments().getString(PACKAGE_NAME)!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomAppBinding.inflate(layoutInflater)
        layoutBinding = binding.layoutDialog
        binding.lifecycleOwner = this
        return binding.root
    }

    @Inject
    lateinit var sheetViewModelFactory: SheetViewModel.SheetViewModelFactory

    val viewModel: SheetViewModel by viewModels {
        SheetViewModel.provideFactory(sheetViewModelFactory, this, arguments, packageName)
    }
    lateinit var mainViewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        initData()
        initClick()
    }

    private fun initData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.item.filter { data -> data != null }.collectLatest {
                        log(it?.id + "------start-----" + it?.name)
                        binding.item = it
                        layoutBinding.item = it
                        binding.executePendingBindings()
                        layoutBinding.executePendingBindings()
                        layoutBinding.root.isClickable = false
                        layoutBinding.clAppItem.setBackgroundResource(android.R.color.transparent)
                        sheetItem = it!!
                    }
                }

            }
        }
    }

    private fun initClick() {
        binding.btnOpen.setOnClickListener {
            val intent = pm.getLaunchIntentForPackage(sheetItem.id)
            if (sheetItem.isEnable && intent != null) {
                startActivity(intent)
                lifecycleScope.launch {
                    appItemDao.updateRunning(sheetItem.id, true)
                }
            } else {
                requireContext().toast(getString(R.string.can_not_open) + sheetItem.name)
            }
            dismiss()
        }
        binding.btnShare.setOnClickListener {
            val file = File(sheetItem.sourceDir)
            val uri: Uri? = FileUtils.getFileUri(requireContext(), ShareContentType.FILE, file)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "*/*"
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    requireContext().getString(R.string.share_apk)
                )
            )
            dismiss()
        }
        binding.btnDetail.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", sheetItem.id, null)
            requireActivity().startActivity(intent)
            dismiss()
        }
        binding.btnUnInstall.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_DELETE
            intent.data = Uri.parse("package:${sheetItem.id}")
            startActivity(intent)
            dismiss()
        }
        binding.btnFreeze.setOnClickListener {
            lifecycleScope.launch {
                if (hasRoot()) {
                    val cmd = (if (sheetItem.isEnable) PM_DISABLE else PM_ENABLE) + sheetItem.id
                    val result = Runner.runCommand(Runner.rootInstance(), cmd)
                    if (result.isSuccessful) {
                        appItemDao.updateEnable(sheetItem.id, !sheetItem.isEnable)
                    }
                    val toast =
                        (if (sheetItem.isEnable) getString(R.string.disable) else getString(
                            R.string.enable
                        )) + sheetItem.name + (if (result.isSuccessful) getString(
                            R.string.success
                        ) else getString(R.string.fail))
                    requireContext().toast(toast)
                    mainViewModel.showDialog.emit(false)
                } else {
                    mainViewModel.showDialog.emit(true)
                }
            }
            dismiss()
        }
        binding.btnRunning.setOnClickListener {
            lifecycleScope.launch {
                if (hasRoot()) {
                    val result = Runner.runCommand(
                        Runner.rootInstance(),
                        FORCE_STOP + sheetItem.id
                    )
                    if (result.isSuccessful) {
                        appItemDao.updateRunning(sheetItem.id, false)
                    }
                    withContext(Dispatchers.Main) {
                        requireContext().toast(
                            if (result.isSuccessful) getString(R.string.stop_success)
                            else getString(R.string.stop_failed)
                        )
                    }
                    mainViewModel.showDialog.emit(false)
                } else {
                    mainViewModel.showDialog.emit(true)
                }
            }
            dismiss()
        }
    }


    private fun hasRoot(): Boolean {
        return RunnerUtils.isRootGiven()
    }
}