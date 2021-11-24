package github.xtvj.cleanx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.repository.AppLocalRepository
import github.xtvj.cleanx.data.repository.AppRemoteRepository
import github.xtvj.cleanx.shell.RunnerUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val remoteRepository: AppRemoteRepository,
    private val localRepository: AppLocalRepository
) : ViewModel() {


    val userList: Flow<PagingData<AppItem>> = Pager(
        config = PagingConfig(
            pageSize = 12,
            enablePlaceholders = true,
            maxSize = 40
        )
    ) {
        localRepository.getUser()
    }.flow.cachedIn(viewModelScope)

    val systemList: Flow<PagingData<AppItem>> = Pager(
        config = PagingConfig(
            pageSize = 12,
            enablePlaceholders = true,
            maxSize = 40
        )
    ) {
        localRepository.getSystem()
    }.flow.cachedIn(viewModelScope)

    val disableList: Flow<PagingData<AppItem>> = Pager(
        config = PagingConfig(
            pageSize = 12,
            enablePlaceholders = true,
            maxSize = 40
        )
    ) {
        localRepository.getDisable()
    }.flow.cachedIn(viewModelScope)


    fun getUserApps() {
        remoteRepository.getApps(RunnerUtils.GETUSER)
    }

    fun getSystemApps() {
        remoteRepository.getApps(RunnerUtils.GETSYS)
    }

    fun getDisabledApps() {
        remoteRepository.getApps(RunnerUtils.GETDISABLED)
    }
}