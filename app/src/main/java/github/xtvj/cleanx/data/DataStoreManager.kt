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
    val sortOrder: SortOrder,
    val darkModel: DarkModel,
    val asc : Boolean//正反排序
)

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val dataStore = appContext.dataStore

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val DARK_MODEL = stringPreferencesKey("dark_model")
        val ASC_MODEL = booleanPreferencesKey("asc_model")
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

    suspend fun updateDarkModel(model: DarkModel){
        dataStore.edit {
            it[PreferencesKeys.DARK_MODEL] = model.name
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit {
            it[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateASCOrder(asc: Boolean){
        dataStore.edit {
            it[PreferencesKeys.ASC_MODEL] = asc
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        // Get the sort order from preferences and convert it to a [SortOrder] object
        val sortOrder =
            SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_ID.name
            )

        val darkModel =
            DarkModel.valueOf(
                preferences[PreferencesKeys.DARK_MODEL] ?: DarkModel.AUTO.name
            )

        val asc = preferences[PreferencesKeys.ASC_MODEL] ?: true


        return UserPreferences(sortOrder,darkModel,asc)
    }
}