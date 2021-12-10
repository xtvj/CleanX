package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.whenResumed
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.ui.adapter.ListItemAdapter
import github.xtvj.cleanx.ui.viewmodel.ListViewModel
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class AppListFragment : Fragment(), ActionMode.Callback, SwipeRefreshLayout.OnRefreshListener {

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

    //private val viewModel: MainViewModel by activityViewModels() //与Activity共用的ViewModel
    private var type by Delegates.notNull<Int>()
    private lateinit var binding: FragmentAppListBinding
    private val lifecycleScope = lifecycle.coroutineScope

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var needLoadData = true

    @Inject
    lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var imageLoaderX: ImageLoaderX

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreateView: $type")

        binding = FragmentAppListBinding.inflate(layoutInflater, container, false)
        adapter.setAdapterType(type)
        binding.rvApp.adapter = adapter
        selectionTracker = SelectionTracker.Builder<Long>(
            "selection",
            binding.rvApp,
            ListItemAdapter.KeyProvider(),
            ListItemAdapter.DetailsLookup(binding.rvApp),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
        adapter.setSelectionTracker(selectionTracker)
        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    if (selectionTracker.selection.size() > 0) {
                        if (actionMode == null) {
                            actionMode =
                                (activity as AppCompatActivity).startSupportActionMode(this@AppListFragment)
                        }
                        actionMode?.title = selectionTracker.selection.size().toString()
                    } else {
                        actionMode?.finish()
                    }
                }
            })
        binding.rvApp.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            lifecycle.whenResumed {

                //viewModel是懒加载，必需在主线程中创建
                fragmentViewModel.run {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (needLoadData) {
                            when (type) {
                                0 -> {
                                    getUserApps()
                                    observeReload(userReload)
                                }
                                1 -> {
                                    getSystemApps()
                                    observeReload(systemReload)
                                }
                                else -> {
                                    getDisabledApps()
                                    observeReload(disableReload)
                                }
                            }
                        }
                        needLoadData = false

                        dataStoreManager.userPreferencesFlow.collectLatest {
                            log("sortOrder: " + it.sortOrder.name + "-----" + "darkModel: " + it.darkModel.name)
                            when (it.sortOrder) {
                                SortOrder.BY_ID -> {
                                    sortByColumn.postValue("id")
                                }
                                SortOrder.BY_NAME -> {
                                    sortByColumn.postValue("name")
                                }
                                SortOrder.BY_UPDATE_TIME -> {
                                    sortByColumn.postValue("lastUpdateTime")
                                }
                            }
                            when (type) {
                                0 -> {
                                    observeApps(userList())
                                }
                                1 -> {
                                    observeApps(systemList())
                                }
                                else -> {
                                    observeApps(disableList())
                                }
                            }
                        }
                    }

                }
            }
        }
        binding.srlFragmentList.setOnRefreshListener(this)
    }

    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        lifecycleScope.launch(Dispatchers.Main) {
            apps.collectLatest {
                adapter.submitData(lifecycle,it)
            }
        }
        //todo 滑动到顶部
        lifecycleScope.launch{
            adapter.loadStateFlow
                .distinctUntilChanged { old, new ->
                    (old.mediator?.prepend?.endOfPaginationReached == true) ==
                            (new.mediator?.prepend?.endOfPaginationReached == true) }
                .filter { it.refresh is LoadState.NotLoading && it.prepend.endOfPaginationReached && !it.append.endOfPaginationReached}
                .collectLatest {
                    binding.rvApp.scrollTo(0,0)
                }
        }
    }

    private fun observeReload(reload: MutableLiveData<Boolean>) {
        lifecycleScope.launch(Dispatchers.Main) {
            reload.observe(viewLifecycleOwner, {
                binding.srlFragmentList.isRefreshing = it
            })
        }
    }


    override fun onDestroyActionMode(actionMode: ActionMode?) {
        selectionTracker.clearSelection()
        this.actionMode = null
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.cab, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_disable -> {
                fragmentViewModel.setApps("disable", adapter.getSelectItems())
                mode?.finish()
            }
            R.id.item_enable -> {
                fragmentViewModel.setApps("enable", adapter.getSelectItems())
                mode?.finish()
            }
        }
        return false
    }

    override fun onRefresh() {
        fragmentViewModel.reFresh(type)
    }


}