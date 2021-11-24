package github.xtvj.cleanx.viewmodel

import androidx.lifecycle.ViewModel
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


    fun getListUser(): Flow<List<AppItem>> {
        return localRepository.getUser()
    }

    fun getListSystem(): Flow<List<AppItem>> {
        return localRepository.getSystem()
    }

    fun getListDisable(): Flow<List<AppItem>> {
        return localRepository.getDisable()
    }


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