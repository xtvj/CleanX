package github.xtvj.cleanx.ui

import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.coroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.utils.AppUtils
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    val lifecycleScope = lifecycle.coroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //todo 使用lifecycle
        lifecycleScope.launch {
//            if (AppUtils.isAppRoot()){
//                test()
//            }else{
                requestRoot()
//            }
        }
    }


    fun test(){

//        mainViewModel.getRunningApp()
//        mainViewModel.info.observe(this,{ t -> binding.tvInfo.text = t })

    }


    fun requestRoot(){
        val view = layoutInflater.inflate(R.layout.dialog_request_root,binding.root,false) as LinearLayoutCompat
        val textView = view.findViewById<MaterialTextView>(R.id.tv_quest_root)
        textView.text = HtmlCompat.fromHtml(getString(R.string.requestrootmessage),HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()
        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(getString(R.string.requestrootok),object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                    AppUtils.isAppRoot()
                }
            })
            .setNegativeButton(getString(R.string.requestrootcancel)){
                    dialog, which ->
                dialog.dismiss()
            }
            .show()
    }








    override fun onBackPressed() {
        //防止闪白屏
        moveTaskToBack(false)
    }


}