package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import github.xtvj.cleanx.R

/**
 * 这种启动方式第一次启动时解决黑屏，但当应用被返回键退回后台再启动时，还会进入些页，闪屏一下再进去MainActivity
 * 也就是还会出现闪白屏情况
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

//    val startLifecycleScope = lifecycle.coroutineScope

//    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivitySplashBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        setContentView(R.layout.activity_splash)
//        startLifecycleScope.launch {
//                delay(200)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
//        }
    }
}