package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.adapter.MainViewPageAdapter
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    //    private val mainViewModel: MainViewModel by viewModels()
//    val lifecycleScope = lifecycle.coroutineScope
//    private lateinit var dialog : AlertDialog
    private lateinit var adapter: MainViewPageAdapter

    @Inject
    lateinit var appItemDao: AppItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
//        checkRoot()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
//        val view = layoutInflater.inflate(R.layout.dialog_request_root,binding.root,false) as LinearLayoutCompat
//        val textView = view.findViewById<MaterialTextView>(R.id.tv_quest_root)
//        textView.text = HtmlCompat.fromHtml(getString(R.string.requestrootmessage),HtmlCompat.FROM_HTML_MODE_LEGACY)
//        textView.movementMethod = LinkMovementMethod.getInstance()
//        dialog = MaterialAlertDialogBuilder(this)
//            .setView(view)
//            .setPositiveButton(getString(R.string.requestrootok),object : DialogInterface.OnClickListener{
//                override fun onClick(dialog: DialogInterface?, which: Int) {
//                    dialog?.dismiss()
//                    lifecycleScope.launch {
//                        mainViewModel.isAppRoot()
//                    }
//                }
//            })
//            .setNegativeButton(getString(R.string.requestrootcancel)){
//                    dialog, which ->
//                dialog.dismiss()
//            }.create()
        setSupportActionBar(binding.tbMain)
        supportActionBar?.hide()
        lifecycleScope.launch(Dispatchers.IO) {
            appItemDao.deleteAll()
        }
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
    }


//    private fun checkRoot(){
//        mainViewModel.root.observe(this,{
//            when {
//                it -> {
//                    if (dialog.isShowing){
//                        dialog.dismiss()
//                    }
//                }
//                else -> {
//                    dialog.show()
//                }
//            }
//
//        })
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(false)
    }

}