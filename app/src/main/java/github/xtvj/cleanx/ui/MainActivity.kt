package github.xtvj.cleanx.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.adapter.MainViewPageAdapter
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.ui.custom.SheetDialog

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
//    private val mainViewModel: MainViewModel by viewModels()
//    val lifecycleScope = lifecycle.coroutineScope
//    private lateinit var dialog : AlertDialog
    private lateinit var adapter: MainViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
//        checkRoot()
    }

    private fun initView(){
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

        adapter = MainViewPageAdapter(supportFragmentManager,lifecycle)
        binding.vp2Apps.adapter = adapter
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