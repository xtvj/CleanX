package github.xtvj.cleanx.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/*
此写法仅示例ViewModel使用Hilt时传参
 */
class SheetViewModel @AssistedInject constructor(
    val appItemDao: AppItemDao,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val packageName: String
) : ViewModel() {

    val item = MutableStateFlow<AppItem?>(null)

    init {
        viewModelScope.launch {
            item.value = appItemDao.findWithID(packageName)
        }
    }

    @AssistedFactory
    interface SheetViewModelFactory {
        fun create(handle: SavedStateHandle, packageName: String): SheetViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: SheetViewModelFactory,
            packageName: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                assistedFactory.create(createSavedStateHandle(), packageName)
            }
        }
    }
}
