package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.receiver.InstallReceiver
import github.xtvj.cleanx.ui.adapter.MainViewPageAdapter
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPageAdapter

    @Inject
    lateinit var installReceiver: InstallReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
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

}