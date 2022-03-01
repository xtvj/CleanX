package github.xtvj.cleanx.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.BuildConfig
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.DarkModel
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.utils.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {

        @Inject
        lateinit var dataStoreManager: DataStoreManager

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val themePreference = findPreference<ListPreference>("themeMode")
            val sortPreference = findPreference<ListPreference>("sortMode")
            val versionPreference = findPreference<Preference>("version")

            lifecycleScope.launch(Dispatchers.IO){
               dataStoreManager.userPreferencesFlow.collectLatest {
                        when(it.darkModel){
                            DarkModel.AUTO -> {
                                themePreference?.setValueIndex(0)
                            }
                            DarkModel.NIGHT ->{
                                themePreference?.setValueIndex(2)
                            }
                            DarkModel.LIGHT ->{
                                themePreference?.setValueIndex(1)
                            }
                        }
                        when(it.sortOrder){
                            SortOrder.BY_ID ->{
                                sortPreference?.setValueIndex(0)
                            }
                            SortOrder.BY_NAME ->{
                                sortPreference?.setValueIndex(1)
                            }
                            SortOrder.BY_UPDATE_TIME ->{
                                sortPreference?.setValueIndex(2)
                            }
                        }
               }
            }
            themePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
//                    <item>default</item>
//                    <item>light</item>
//                    <item>dark</item>
                    val darkModel = when(newValue){
                        "light" -> DarkModel.LIGHT
                        "dark" -> DarkModel.NIGHT
                        else -> DarkModel.AUTO
                    }
                    lifecycleScope.launch(Dispatchers.IO){
                        dataStoreManager.updateDarkModel(darkModel)
                    }
                    ThemeHelper.applyTheme(darkModel)

                    true
                }
            sortPreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
//                    <item>id</item>
//                    <item>name</item>
//                    <item>update_time</item>
                    val sortModel = when(newValue){
                        "id" -> SortOrder.BY_ID
                        "name" -> SortOrder.BY_NAME
                        else -> SortOrder.BY_UPDATE_TIME
                    }
                    lifecycleScope.launch(Dispatchers.IO){
                        dataStoreManager.updateSortOrder(sortModel)
                    }
                    true
                }
            versionPreference?.summary = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
        }
    }
}