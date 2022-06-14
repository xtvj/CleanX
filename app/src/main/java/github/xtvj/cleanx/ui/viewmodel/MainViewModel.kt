package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

class MainViewModel : ViewModel() {

    val showDialog = MutableSharedFlow<Boolean>()

}