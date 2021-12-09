package github.xtvj.cleanx.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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
    val darkModel: DarkModel
)

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val dataStore = appContext.dataStore

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val DARK_MODEL = stringPreferencesKey("dark_model")
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

        return UserPreferences(sortOrder,darkModel)
    }
}