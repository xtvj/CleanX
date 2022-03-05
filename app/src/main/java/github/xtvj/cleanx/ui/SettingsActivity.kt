package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.BuildConfig
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.DarkModel
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.databinding.SettingsActivityBinding
import github.xtvj.cleanx.databinding.ToolbarBinding
import github.xtvj.cleanx.utils.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding
    private lateinit var toolbarBinding: ToolbarBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        toolbarBinding = binding.includeToolbar
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.tbCustom)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initNavController()
    }

    private fun initNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.settings) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration.Builder().build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!(navController.navigateUp() || super.onSupportNavigateUp())) {
            onBackPressed()
        }
        return true
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
            val licensesPreference = findPreference<Preference>("licenses")

            lifecycleScope.launch(Dispatchers.IO) {
                dataStoreManager.userPreferencesFlow.collectLatest {
                    when (it.darkModel) {
                        DarkModel.AUTO -> {
                            themePreference?.setValueIndex(0)
                        }
                        DarkModel.NIGHT -> {
                            themePreference?.setValueIndex(2)
                        }
                        DarkModel.LIGHT -> {
                            themePreference?.setValueIndex(1)
                        }
                    }
                    when (it.sortOrder) {
                        SortOrder.BY_ID -> {
                            sortPreference?.setValueIndex(0)
                        }
                        SortOrder.BY_NAME -> {
                            sortPreference?.setValueIndex(1)
                        }
                        SortOrder.BY_UPDATE_TIME -> {
                            sortPreference?.setValueIndex(2)
                        }
                    }
                }
            }
            themePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val darkModel = when (newValue) {
                        "light" -> DarkModel.LIGHT
                        "dark" -> DarkModel.NIGHT
                        else -> DarkModel.AUTO
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.updateDarkModel(darkModel)
                    }
                    ThemeHelper.applyTheme(darkModel)

                    true
                }
            sortPreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val sortModel = when (newValue) {
                        "id" -> SortOrder.BY_ID
                        "name" -> SortOrder.BY_NAME
                        else -> SortOrder.BY_UPDATE_TIME
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        dataStoreManager.updateSortOrder(sortModel)
                    }
                    true
                }
            versionPreference?.summary =
                BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"

            licensesPreference?.setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_settings_fragment_to_licenses_fragment)
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    activity?.title = getString(R.string.setting)
                }
            }
        }
    }
}