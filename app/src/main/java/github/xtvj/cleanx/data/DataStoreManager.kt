package github.xtvj.cleanx.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

enum class SortOrder {
    BY_ID,
    BY_NAME,
    BY_UPDATE_TIME
}

enum class DarkModel {
    AUTO,
    LIGHT,
    NIGHT
}

data class UserPreferences(
    val sortOrder: SortOrder,//列表排序方式
    val darkModel: DarkModel,//夜间模式
    val asc: Boolean,//正反排序
    val enable: Int,//0:不过滤数据 1:过滤掉禁用/运行的 2:过滤掉启用/不运行的
    val running: Int//0:不过滤数据 1:过滤掉禁用/运行的 2:过滤掉启用/不运行的
)

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val dataStore = appContext.dataStore

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val DARK_MODEL = stringPreferencesKey("dark_model")
        val ASC_MODEL = booleanPreferencesKey("asc_model")
        val ENABLE = intPreferencesKey("enable")
        val RUNNING = intPreferencesKey("running")
    }

    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                log("Error reading preferences: " + exception.message)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun updateDarkModel(model: DarkModel) {
        dataStore.edit {
            it[PreferencesKeys.DARK_MODEL] = model.name
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit {
            it[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateASCOrder(asc: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.ASC_MODEL] = asc
        }
    }

    suspend fun updateEnable(enable: Int) {
        dataStore.edit {
            it[PreferencesKeys.ENABLE] = enable
        }
    }

    suspend fun updateRunning(running: Int) {
        dataStore.edit {
            it[PreferencesKeys.RUNNING] = running
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        // Get the sort order from preferences and convert it to a [SortOrder] object
        //默认包名排序
        val sortOrder =
            SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_NAME.name
            )

        //默认自动
        val darkModel =
            DarkModel.valueOf(
                preferences[PreferencesKeys.DARK_MODEL] ?: DarkModel.AUTO.name
            )

        //默认正序
        val asc = preferences[PreferencesKeys.ASC_MODEL] ?: true

        //默认不过滤是否禁用或运行
        val enable = preferences[PreferencesKeys.ENABLE] ?: 0
        val running = preferences[PreferencesKeys.RUNNING] ?: 0

        return UserPreferences(sortOrder, darkModel, asc, enable, running)
    }
}