package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mikepenz.aboutlibraries.LibsBuilder
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

    private lateinit var binding:SettingsActivityBinding
    private lateinit var toolbarBinding: ToolbarBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        toolbarBinding = binding.includeToolbar
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.tbCustom)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

            licensesPreference?.setOnPreferenceClickListener {
//                findNavController().navigate(R.id.open_OssLicensesMenuActivity)
//                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                LibsBuilder()
                    .withAboutMinimalDesign(true)
                    .withEdgeToEdge(true)
                    .withActivityTitle(getString(R.string.third_party_licenses))
                    .withAboutIconShown(false)
                    .withSearchEnabled(false)
                    .withLicenseDialog(true)
                    .start(requireContext())
                true
            }
        }
    }
}