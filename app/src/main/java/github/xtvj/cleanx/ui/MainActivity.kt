package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.receiver.InstallReceiver
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.ui.adapter.MainViewPageAdapter
import github.xtvj.cleanx.ui.viewmodel.MainViewModel
import github.xtvj.cleanx.utils.toastLong
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPageAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var rootDialog: AlertDialog

    @Inject
    lateinit var installReceiver: InstallReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initView()
        initDialog()
        initReceiver()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        setSupportActionBar(binding.tbMain)
        adapter = MainViewPageAdapter(supportFragmentManager, lifecycle)
        binding.vp2Apps.adapter = adapter
        TabLayoutMediator(
            binding.tlMain,
            binding.vp2Apps
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon = getDrawable(R.drawable.ic_user)
                    tab.contentDescription = getString(R.string.user_app)
                }
                1 -> {
                    tab.icon = getDrawable(R.drawable.ic_system)
                    tab.contentDescription = getString(R.string.system_app)
                }
                2 -> {
                    tab.icon = getDrawable(R.drawable.ic_disable)
                    tab.contentDescription = getString(R.string.disabled_app)
                }
            }
        }.attach()

        binding.vp2Apps.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        title = getString(R.string.user_app)
                    }
                    1 -> {
                        title = getString(R.string.system_app)
                    }
                    2 -> {
                        title = getString(R.string.disabled_app)
                    }
                }
                super.onPageSelected(position)
            }
        })

    }

    private fun initReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }
        registerReceiver(installReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(installReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return true
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }


    private fun initDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_request_root, null)
        val textView = dialogView.findViewById<MaterialTextView>(R.id.tv_quest_root)
        textView.text = HtmlCompat.fromHtml(
            getString(R.string.request_root_message),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        textView.movementMethod = LinkMovementMethod.getInstance()

        rootDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.request_root_ok)) { dialog, _ ->
                lifecycleScope.launch {
                    val isRoot = RunnerUtils.isRootGiven()
                    if (isRoot) {
                        toastLong(R.string.got_root)
                    } else {
                        toastLong(R.string.need_to_open_root)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.request_root_cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.showDialog.collectLatest {
                    if (it && !rootDialog.isShowing){
                        rootDialog.show()
                    }
                }
            }
        }
    }

}