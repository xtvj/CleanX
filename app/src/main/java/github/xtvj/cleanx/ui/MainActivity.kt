package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.ui.adapter.MainViewPageAdapter
import github.xtvj.cleanx.ui.base.BaseActivity


@AndroidEntryPoint
class MainActivity : BaseActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
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
                    tab.icon = getDrawable(R.drawable.ic_sys)
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
        super.onBackPressed()
        moveTaskToBack(false)
    }

}