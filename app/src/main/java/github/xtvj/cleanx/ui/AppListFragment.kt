package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.adapter.SimpleItem
import github.xtvj.cleanx.databinding.AppListFragmentBinding
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
@AndroidEntryPoint
class AppListFragment : Fragment() {

    companion object {
        private const val KEY_ITEM_TEXT = "github.xtvj.cleanx.KEY_ITEM_FRAGMENT"
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
    private val itemAdapter = ItemAdapter<SimpleItem>() //create the ItemAdapter holding your Items
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var selectExtension: SelectExtension<SimpleItem>
    val lifecycleScope = lifecycle.coroutineScope


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        log("onCreateView:")
        binding = AppListFragmentBinding.inflate(layoutInflater,container,false)

        fastAdapter.setHasStableIds(true)
        selectExtension = fastAdapter.getSelectExtension()
        selectExtension.apply {
            isSelectable = true
            multiSelect = true
            selectOnLongClick = true
            selectionListener = object : ISelectionListener<SimpleItem> {
                override fun onSelectionChanged(item: SimpleItem, selected: Boolean) {
                    log("FastAdapter", "SelectedCount: " + selectExtension.selections.size + " ItemsCount: " + selectExtension.selectedItems.size)
                }
            }
        }

        binding.rvApp.layoutManager = LinearLayoutManager(context)
//        binding.rvApp.itemAnimator = DefaultAnimator()

        //set our adapters to the RecyclerView
        binding.rvApp.adapter = fastAdapter
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(KEY_ITEM_TEXT)?: throw IllegalStateException()
        log("onCreate: $type")
        lifecycleScope.launch {
            when(type){
                0 ->{
                    observeApps(viewModel.userapps,savedInstanceState)
                }
                1 ->{
                    observeApps(viewModel.systemapps,savedInstanceState)
                }
                else ->{
                    observeApps(viewModel.disabledapps,savedInstanceState)
                }
            }
        }
    }

    private fun observeApps(apps : MutableLiveData<List<String>>, savedInstanceState : Bundle?){
        apps.observe(this, {
                fragmentViewModel.upData(it)

        })

        fragmentViewModel.list.observe(this,  {
            itemAdapter.add(it)
            //restore selections (this has to be done after the items were added
            fastAdapter.withSavedInstanceState(savedInstanceState)
        })
    }

    override fun onPause() {
        super.onPause()

    }


}