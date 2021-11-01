package github.xtvj.cleanx.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import github.xtvj.cleanx.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 这种启动方式第一次启动时解决黑屏，但当应用被返回键退回后台再启动时，还会进入些页，闪屏一下再进去MainActivity
 * 也就是还会出现闪白屏情况
 */
class FullscreenActivity : AppCompatActivity() {

    val startLifecycleScope = lifecycle.coroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        val intent = Intent(this, MainActivity::class.java).apply {
            //示例传参
//            putExtra("EXTRA_MESSAGE", "message")
        }
        startLifecycleScope.launch() {
            delay(200)
            startActivity(intent)
            finish()
        }
    }
}