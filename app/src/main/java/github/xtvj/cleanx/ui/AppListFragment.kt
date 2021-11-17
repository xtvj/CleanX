package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import github.xtvj.cleanx.databinding.AppListFragmentBinding
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlin.properties.Delegates

class AppListFragment : Fragment() {

    companion object {
        private const val KEY_ITEM_TEXT = "github.xtvj.cleanx.KEY_ITEM_FRAGMENT"
        private const val KEY_COUNT = "github.xtvj.cleanx.KEY_COUNT"
        fun create(item: Int) =
            AppListFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(KEY_ITEM_TEXT, item)
                }
            }
    }

    private val fragmentViewModel: ListViewModel by viewModels() //Fragment自己的ViewModel
    private val viewModel: MainViewModel by activityViewModels() //与Activity共用的ViewModel
    private var type by Delegates.notNull<Int>()
    private lateinit var binding: AppListFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        log("onCreateView:")
        binding = AppListFragmentBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_COUNT, type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(KEY_ITEM_TEXT)?: throw IllegalStateException()
        log("onCreate: $type")
        when(type){
            0 ->{
                viewModel.userapps.observe(this, { t -> binding.tv.text = t.toString() })
            }
            1 ->{
                viewModel.systemapps.observe(this, { t -> binding.tv.text = t.toString() })
            }
            else ->{
                viewModel.disabledapps.observe(this, { t -> binding.tv.text = t.toString() })
            }
        }
    }

}