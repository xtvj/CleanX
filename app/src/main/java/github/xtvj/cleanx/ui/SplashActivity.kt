package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import github.xtvj.cleanx.R
import github.xtvj.cleanx.ui.base.BaseActivity

/**
 * 这种启动方式第一次启动时解决黑屏，但当应用被返回键退回后台再启动时，还会进入些页，闪屏一下再进去MainActivity
 * 也就是还会出现闪白屏情况
 */
@SuppressLint("CustomSplashScreen")
//@AndroidEntryPoint
//初始页面的windowsBackground不适用于DarkModel,只有当系统设置为深色模式时才起作用
//app内部设置为深色模式时，背景色不会改变
class SplashActivity : BaseActivity() {

//    @Inject
//    lateinit var dataStoreManager : DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}