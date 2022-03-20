package github.xtvj.cleanx.ui.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.License


class LicensesViewModel(application: Application) : AndroidViewModel(application) {

    fun getList(): List<License> {
        val moshi = Moshi.Builder().build()
        val listType = Types.newParameterizedType(List::class.java, License::class.java)
        val jsonAdapter: JsonAdapter<List<License>> = moshi.adapter(listType)
        val json = getApplication<Application>().resources.openRawResource(R.raw.licenses)
            .bufferedReader().use { it.readText() }
        return jsonAdapter.fromJson(json) as List<License>
    }

}