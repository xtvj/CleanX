package github.xtvj.cleanx.ui

import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.coroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import github.xtvj.cleanx.R
import github.xtvj.cleanx.adapter.MainViewPageAdapter
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    val lifecycleScope = lifecycle.coroutineScope
    private lateinit var dialog : AlertDialog
    private lateinit var adapter: MainViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initApps()
    }

    private fun initView(){
        val view = layoutInflater.inflate(R.layout.dialog_request_root,binding.root,false) as LinearLayoutCompat
        val textView = view.findViewById<MaterialTextView>(R.id.tv_quest_root)
        textView.text = HtmlCompat.fromHtml(getString(R.string.requestrootmessage),HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()
        dialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(getString(R.string.requestrootok),object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                    lifecycleScope.launch {
                        mainViewModel.isAppRoot()
                    }

                }
            })
            .setNegativeButton(getString(R.string.requestrootcancel)){
                    dialog, which ->
                dialog.dismiss()
            }.create()

        adapter = MainViewPageAdapter(supportFragmentManager,lifecycle)
        binding.vp2Apps.adapter = adapter
    }


    private fun initApps(){
        lifecycleScope.launch {
            if (mainViewModel.isAppRoot()){
                //有root权限-->获取列表
                getApps()
            }else{
                //没有权限-->弹窗提示
                requestRoot()
            }
        }
        mainViewModel.root.observe(this,{
            when {
                it -> {
                    if (dialog.isShowing){
                        dialog.dismiss()
                        getApps()
                    }
                }
                else -> {
                    dialog.show()
                }
            }

        })
    }

    private fun getApps(){
        mainViewModel.getUserApps()
        mainViewModel.getSystemApps()
        mainViewModel.getDisabledApps()

    }

    private fun requestRoot(){
        dialog.show()
    }

    override fun onBackPressed() {
        //防止闪白屏
        moveTaskToBack(false)
    }


}