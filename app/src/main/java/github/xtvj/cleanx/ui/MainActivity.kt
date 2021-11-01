package github.xtvj.cleanx.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import github.xtvj.cleanx.R

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    override fun onBackPressed() {
        //防止闪白屏
        moveTaskToBack(false)
    }


}