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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
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
            fragmentViewModel.setApps(FORCE_STOP, adapter.getSelectItems())
            actionMode?.finish()
        }
        binding.mbEnable.setOnClickListener {
            fragmentViewModel.setApps(PM_ENABLE, adapter.getSelectItems())
            actionMode?.finish()
        }
        binding.mbDisable.setOnClickListener {
            fragmentViewModel.setApps(PM_DISABLE, adapter.getSelectItems())
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
//            refreshData()
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "UnsafeRepeatOnLifecycleDetector")
    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        log("observer Apps type =$type")
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            //使用Started，如果使用RESUMED，每次页面显示都要重新加载
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
//        refreshData()
    }

//    private fun refreshData() {
//        when (type) {
//            0 -> {
//                workInfo = fragmentViewModel.getAppsByCode(GET_USER)
//            }
//            1 -> {
//                workInfo = fragmentViewModel.getAppsByCode(GET_SYS)
//            }
//            2 -> {
//                workInfo = fragmentViewModel.getAppsByCode(GET_DISABLED)
//            }
//        }
//        lifecycleScope.launch(Dispatchers.Main) {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                workInfo.observe(viewLifecycleOwner, Observer {
//                    log(it.state.name)
//                    when (it.state) {
//                        WorkInfo.State.SUCCEEDED -> {
//                            //获取数据完成
//                            fragmentViewModel.loading.value = false
//                            binding.tvNoData.visibility = View.INVISIBLE
//                            binding.srlFragmentList.isRefreshing = false
//                            firstLoad = false
//                        }
//                        WorkInfo.State.FAILED -> {
//                            binding.srlFragmentList.isRefreshing = false
//                            fragmentViewModel.loading.value = false
//                            binding.tvNoData.visibility = View.VISIBLE
//                            firstLoad = false
//                        }
//                        WorkInfo.State.RUNNING -> {
//                            //正在获取数据
//                            if (firstLoad) {
//                                fragmentViewModel.loading.value = true
//                            }
//                            binding.tvNoData.visibility = View.INVISIBLE
//                        }
//                        else -> {}
//                    }
//                })
//            }
//        }
//    }

    private fun observeUI() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.Main) {
                    fragmentViewModel.loading.collectLatest {
                        binding.pgbLoading.isVisible = it
                    }
                }
                launch(Dispatchers.Main) {
                    adapter.loadStateFlow.collect { loadStates ->
                        val refresher = loadStates.refresh
                        log("refresher: ${refresher.toString()}")
//                        binding.tvNoData.isVisible = (refresher is LoadState.NotLoading && adapter.itemCount < 1)
                        binding.srlFragmentList.isRefreshing = refresher is LoadState.Loading
                        fragmentViewModel.loading.value = firstLoad
                        binding.tvError.isVisible = false
                        if (refresher !is LoadState.Loading){
                            firstLoad = false
                        }
                        if (refresher is LoadState.Error){
                            binding.tvError.isVisible = true
                            binding.tvError.text = refresher.error.localizedMessage
                        }
                    }
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
