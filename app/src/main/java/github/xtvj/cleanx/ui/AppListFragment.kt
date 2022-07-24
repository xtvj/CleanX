package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.ui.adapter.ListItemAdapter
import github.xtvj.cleanx.ui.viewmodel.ListViewModel
import github.xtvj.cleanx.ui.viewmodel.MainViewModel
import github.xtvj.cleanx.utils.FORCE_STOP
import github.xtvj.cleanx.utils.PM_DISABLE
import github.xtvj.cleanx.utils.PM_ENABLE
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


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

    //private val fragmentViewModel: ListViewModel by viewModels()
    //by viewModels() Fragment自己的ViewModel
    //by activityViewModels() 与Activity共用的ViewModel
    private lateinit var fragmentViewModel: ListViewModel //Fragment自己的ViewModel
    lateinit var mainViewModel: MainViewModel

    private var type = -1
    private lateinit var binding: FragmentAppListBinding
    private val lifecycleScope = lifecycle.coroutineScope

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<AppItem>

    private var job: Job? = null
    private var firstLoad = true

    //    private lateinit var workInfo: LiveData<WorkInfo>

    //viewModel为三个页面共用，状态值临时放在这里
    private val isEmpty = MutableStateFlow(true)
    private val loading = MutableStateFlow<Boolean>(true)

    @Inject
    lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var pm: PackageManager

    @Inject
    lateinit var appItemDao: AppItemDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreateView: $type")

        binding = FragmentAppListBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setAdapterType(type)
        adapter.itemClickListener = { item, _ ->
            SheetDialog(item.id).showNow(childFragmentManager, "Product $item.id")
        }
        binding.rvApp.adapter = adapter
        selectionTracker = SelectionTracker.Builder(
            "selection",
            binding.rvApp,
            ListItemAdapter.KeyProvider(adapter),
            ListItemAdapter.DetailsLookup(binding.rvApp),
            StorageStrategy.createParcelableStorage(AppItem::class.java)
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
        adapter.setSelectionTracker(selectionTracker)
        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<AppItem>() {
                override fun onSelectionChanged() {
                    if (selectionTracker.hasSelection()) {
                        binding.groupSelect.visibility = View.VISIBLE
                        binding.srlFragmentList.isEnabled = false
                        if (actionMode == null) {
                            actionMode =
                                (activity as AppCompatActivity).startSupportActionMode(this@AppListFragment)
                        }
                        actionMode?.title = selectionTracker.selection.size().toString()
                    } else {
                        binding.groupSelect.visibility = View.INVISIBLE
                        actionMode?.finish()
                        binding.srlFragmentList.isEnabled = true
                    }
                }
            })
        binding.rvApp.layoutManager = LinearLayoutManager(context)
//        binding.rvApp.setHasFixedSize(true)
//        (binding.rvApp.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        binding.rvApp.addOnScrollListener(scrollListener)
        binding.srlFragmentList.setOnRefreshListener(this)
        fragmentViewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.mbStop.setOnClickListener {
            fragmentViewModel.setApps(FORCE_STOP, adapter.getSelectItems(), loading)
            actionMode?.finish()
        }
        binding.mbEnable.setOnClickListener {
            fragmentViewModel.setApps(PM_ENABLE, adapter.getSelectItems(), loading)
            actionMode?.finish()
        }
        binding.mbDisable.setOnClickListener {
            fragmentViewModel.setApps(PM_DISABLE, adapter.getSelectItems(), loading)
            actionMode?.finish()
        }
        observeUI()
        lifecycleScope.launch {
            lifecycle.whenResumed {
                collectData()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectData() {
        fragmentViewModel.run {
            val list = when (type) {
                0 -> {
                    userList
                }
                1 -> {
                    systemList
                }
                2 -> {
                    disableList
                }
                else -> {
                    throw Exception("异常 type: $type")
                }
            }
            observeApps(list)
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "UnsafeRepeatOnLifecycleDetector")
    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        log("observer Apps type =$type")
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            //使用Created，如果使用Resumed，每次页面显示都要重新加载
            //如果使用Started，页面返回后台再进入会进入onStart会重新加载数据
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                apps.collectLatest {
                    adapter.submitData(lifecycle, it)
                }
            }
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
            R.id.item_select_all -> {
                val itemsArray = arrayListOf<AppItem>()
                adapter.snapshot().items.forEach {
                    if (!selectionTracker.isSelected(it))
                        itemsArray.add(it)
                }
                selectionTracker.setItemsSelected(itemsArray.asIterable(), true)
            }
            R.id.item_invert_select -> {
                val select = arrayListOf<AppItem>()
                val cancel = arrayListOf<AppItem>()
                adapter.snapshot().items.forEach {
                    if (!selectionTracker.isSelected(it)) {
                        select.add(it)
                    } else {
                        cancel.add(it)
                    }
                }
                selectionTracker.setItemsSelected(select.asIterable(), true)
                selectionTracker.setItemsSelected(cancel.asIterable(), false)
            }
        }
        if (!selectionTracker.hasSelection()) {
            mode?.finish()
        }
        return false
    }

    override fun onRefresh() {
        adapter.refresh()
    }

    private fun observeUI() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.Main) {
                    loading.collectLatest {
                        binding.pgbLoading.isVisible = it
                    }
                }
                launch(Dispatchers.Main) {
                    isEmpty.collectLatest {
                        binding.tvNoData.isVisible = it
                    }
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            launch(Dispatchers.Main) {
                adapter.loadStateFlow.collect { loadStates ->
                    val refresher = loadStates.refresh
                    if (!firstLoad){
                        binding.srlFragmentList.isRefreshing = refresher is LoadState.Loading
                    }
                    loading.value = firstLoad
                    firstLoad = false
                    binding.tvError.isVisible = false
                    val errorState = loadStates.source.append as? LoadState.Error
                        ?: loadStates.source.prepend as? LoadState.Error
                        ?: loadStates.append as? LoadState.Error
                        ?: loadStates.prepend as? LoadState.Error
                    errorState?.let {
                        binding.tvError.isVisible = true
                        binding.tvError.text = errorState.error.localizedMessage
                    }
                }
            }
            launch {
                //数据变更后划动到第一项
                adapter.loadStateFlow
                    // Only emit when REFRESH LoadState for RemoteMediator changes.
                    .distinctUntilChangedBy { it.refresh }
                    // Only react to cases where REFRESH completes, such as NotLoading.
                    .filter { it.refresh is LoadState.NotLoading }
                    // Scroll to top is synchronous with UI updates, even if remote load was
                    // triggered.
                    .collect { binding.rvApp.scrollToPosition(0) }
            }
            launch {
                adapter.onPagesUpdatedFlow.collectLatest {
                    log("列表数据大小为：${adapter.itemCount}")
                    isEmpty.value = adapter.itemCount == 0
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            binding.groupSelect.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(
                    0, 0, 0, mainViewModel.offset.value
                )
            }
        }
    }
}
