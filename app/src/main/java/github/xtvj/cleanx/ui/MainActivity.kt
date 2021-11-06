package github.xtvj.cleanx.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import github.xtvj.cleanx.databinding.ActivityMainBinding
import github.xtvj.cleanx.utils.AppUtils
import github.xtvj.cleanx.viewmodel.MainViewModel


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (AppUtils.isAppRoot()){
            test()
        }else{
            requestRoot()
        }
    }


    fun test(){

//        mainViewModel.getRunningApp()
//        mainViewModel.info.observe(this,{ t -> binding.tvInfo.text = t })

    }

    fun requestRoot(){

    }








    override fun onBackPressed() {
        //防止闪白屏
        moveTaskToBack(false)
    }


}