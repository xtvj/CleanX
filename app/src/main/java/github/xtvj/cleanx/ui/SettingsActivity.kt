package github.xtvj.cleanx.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.DarkModel
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.ui.base.BaseActivity
import github.xtvj.cleanx.utils.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

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

            lifecycleScope.launch(Dispatchers.IO){
                when(dataStoreManager.fetchInitialPreferences().darkModel){
                    DarkModel.AUTO -> {
                        themePreference?.setDefaultValue("default")
                    }
                    DarkModel.NIGHT ->{
                        themePreference?.setDefaultValue("dark")
                    }
                    DarkModel.LIGHT ->{
                        themePreference?.setDefaultValue("light")
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
        }
    }
}