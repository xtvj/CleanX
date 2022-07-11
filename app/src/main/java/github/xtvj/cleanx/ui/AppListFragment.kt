package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.WorkInfo
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.ui.adapter.ListItemAdapter
import github.xtvj.cleanx.ui.viewmodel.ListViewModel
import github.xtvj.cleanx.ui.viewmodel.MainViewModel
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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

    //    private val fragmentViewModel: ListViewModel by viewModels() //Fragment自己的ViewModel
    private lateinit var fragmentViewModel: ListViewModel //Fragment自己的ViewModel
    lateinit var mainViewModel: MainViewModel

    //    private val fragmentViewModel: ListViewModel by activityViewModels() //与Activity共用的ViewModel
    private var type = -1
    private lateinit var binding: FragmentAppListBinding
    private val lifecycleScope = lifecycle.coroutineScope

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<AppItem>

    private var job: Job? = null
    private var firstLoad = true
    private lateinit var workInfo: LiveData<WorkInfo>

    @Inject
    lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var dataStoreManager: DataStoreManager

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
        binding.btSelectAll.setOnClickListener(selectAll)
        binding.btReverseSelect.setOnClickListener(reverseSelect)
        fragmentViewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        observeLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            viewLifecycleOwner.whenResumed {
                if (firstLoad) {
                    collectData()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectData() {
        firstLoad = false
        fragmentViewModel.run {
            if (type == 0) {//三个fragment共用一个ViewModel，只需要第运行一次
                lifecycleScope.launch(Dispatchers.IO) {
                    repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        dataStoreManager.userPreferencesFlow.collectLatest {
                            log("sortOrder: " + it.sortOrder.name + "-----" + "darkModel: " + it.darkModel.name)
                            when (it.sortOrder) {
                                SortOrder.BY_ID -> {
                                    sortByColumnFlow.update { APPS_BY_ID }
                                }
                                SortOrder.BY_NAME -> {
                                    sortByColumnFlow.update { APPS_BY_NAME }
                                }
                                SortOrder.BY_UPDATE_TIME -> {
                                    sortByColumnFlow.update { APPS_BY_LAST_UPDATE_TIME }
                                }
                            }
                            filterEnable.value = it.enable
                            filterRunning.value = it.running
                            asc.value = it.asc
                        }
                    }
                }
            }
            lifecycleScope.launch(Dispatchers.IO) {
                when (type) {
                    0 -> {
                        observeApps(userList)
                    }
                    1 -> {
                        observeApps(systemList)
                    }
                    2 -> {
                        observeApps(disableList)
                    }
                }
                refreshData()
            }
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "UnsafeRepeatOnLifecycleDetector")
    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        log("observer Apps type =$type")
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
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
            R.id.item_disable -> {
                fragmentViewModel.setApps(PM_DISABLE, adapter.getSelectItems())
                mode?.finish()
            }
            R.id.item_enable -> {
                fragmentViewModel.setApps(PM_ENABLE, adapter.getSelectItems())
                mode?.finish()
            }
            R.id.item_stop -> {
                fragmentViewModel.setApps(FORCE_STOP, adapter.getSelectItems())
                mode?.finish()
            }
        }
        return false
    }

    override fun onRefresh() {
//        adapter.refresh()
        refreshData()
    }

    private fun refreshData() {
        when (type) {
            0 -> {
                workInfo = fragmentViewModel.getAppsByCode(GET_USER)
            }
            1 -> {
                workInfo = fragmentViewModel.getAppsByCode(GET_SYS)
            }
            2 -> {
                workInfo = fragmentViewModel.getAppsByCode(GET_DISABLED)
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                workInfo.observe(viewLifecycleOwner, Observer {
                    log(it.state.name)
                    when (it.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            //获取数据完成
                            fragmentViewModel.loading.value = false
                            binding.tvNoData.visibility = View.INVISIBLE
                            binding.srlFragmentList.isRefreshing = false
                        }
                        WorkInfo.State.FAILED -> {
                            binding.srlFragmentList.isRefreshing = false
                            fragmentViewModel.loading.value = false
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                        WorkInfo.State.RUNNING -> {
                            //正在获取数据
                            if (firstLoad) {
                                fragmentViewModel.loading.value = true
                            }
                            binding.tvNoData.visibility = View.INVISIBLE
                        }
                        else -> {}
                    }
                })
            }
        }
    }

    private fun observeLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                fragmentViewModel.loading.collectLatest {
                    binding.pgbLoading.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            binding.btReverseSelect.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(
                    0, 0, 0, mainViewModel.offset.value
                )
            }
            binding.btSelectAll.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(
                    0, 0, 0, mainViewModel.offset.value
                )
            }
        }
    }

    private val selectAll = View.OnClickListener {
        val itemsArray = arrayListOf<AppItem>()
        adapter.snapshot().items.forEach {
            if (!selectionTracker.isSelected(it))
                itemsArray.add(it)
        }
        selectionTracker.setItemsSelected(itemsArray.asIterable(), true)
    }

    private val reverseSelect = View.OnClickListener {
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
