package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {

    val showDialog = MutableSharedFlow<Boolean>()
    val offset = MutableStateFlow<Int>(0)

}