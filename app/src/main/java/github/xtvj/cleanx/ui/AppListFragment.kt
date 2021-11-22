package github.xtvj.cleanx.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.adapter.ListItemAdapter
import github.xtvj.cleanx.databinding.AppListFragmentBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class AppListFragment : Fragment(),ActionMode.Callback {

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
//    private val itemAdapter = ItemAdapter<SimpleItem>() //create the ItemAdapter holding your Items
//    private val fastAdapter = FastAdapter.with(itemAdapter)
//    private lateinit var selectExtension: SelectExtension<SimpleItem>
//    private lateinit var mActionModeHelper: ActionModeHelper<SimpleItem>
    private val lifecycleScope = lifecycle.coroutineScope

    private var actionMode : ActionMode? = null
    private lateinit var selectionTracker : SelectionTracker<Long>
    private lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var imageLoaderX : ImageLoaderX


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreateView: $type")

        binding = AppListFragmentBinding.inflate(layoutInflater, container, false)

        adapter = ListItemAdapter(imageLoaderX)
        //set our adapters to the RecyclerView
        binding.rvApp.adapter = adapter
        selectionTracker = SelectionTracker.Builder<Long>(
            "selection",
            binding.rvApp,
            ListItemAdapter.KeyProvider(adapter),
            ListItemAdapter.DetailsLookup(binding.rvApp),
            StorageStrategy.createLongStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
        adapter.setSelectionTracker(selectionTracker)
        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long?>() {
                override fun onSelectionChanged() {
                    if (selectionTracker.selection.size() > 0) {
                        actionMode?.title = selectionTracker.selection.size().toString()
                    } else {
                        actionMode?.finish()
                    }
                }
            })

        binding.rvApp.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            when (type) {
                0 -> {
                    observeApps(viewModel.userapps, savedInstanceState)
                }
                1 -> {
                    observeApps(viewModel.systemapps, savedInstanceState)
                }
                else -> {
                    observeApps(viewModel.disabledapps, savedInstanceState)
                }
            }

            fragmentViewModel.list.observe(viewLifecycleOwner, Observer {
                adapter.setItems(it)
            })
        }
        return binding.root
    }


    private fun observeApps(apps: MutableLiveData<List<String>>, savedInstanceState: Bundle?) {

        apps.observe(viewLifecycleOwner, Observer {
            fragmentViewModel.upData(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        requireActivity().onBackPressedDispatcher.addCallback(onBackPressed)
    }

//    private val onBackPressed = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            if (isResumed){
//                log("type: $type launchWhenResumed")
//                    if (selectionTracker.hasSelection()) {
//                        selectionTracker.clearSelection()
//                    } else {
//                        requireActivity().onBackPressed()
//                    }
//            }
//        }
//    }


    override fun onDestroyActionMode(actionMode: ActionMode?) {
        selectionTracker.clearSelection()
        this.actionMode = null
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
       return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }
}