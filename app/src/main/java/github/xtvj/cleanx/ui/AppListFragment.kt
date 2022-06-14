package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
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
import github.xtvj.cleanx.utils.*
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
                        binding.srlFragmentList.isEnabled = false
                        if (actionMode == null) {
                            actionMode =
                                (activity as AppCompatActivity).startSupportActionMode(this@AppListFragment)
                        }
                        actionMode?.title = selectionTracker.selection.size().toString()
                    } else {
                        actionMode?.finish()
                        binding.srlFragmentList.isEnabled = true
                    }
                }
            })
        binding.rvApp.layoutManager = LinearLayoutManager(context)
        binding.rvApp.setHasFixedSize(true)
        binding.srlFragmentList.setOnRefreshListener(this)
        fragmentViewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "UnsafeRepeatOnLifecycleDetector")
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
            lifecycleScope.launch {
                fragmentViewModel.run {
                    if (type == 0) {
                        launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                                }
                            }
                        }
                    }
                    launch {
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
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "UnsafeRepeatOnLifecycleDetector")
    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        log("observer Apps type =$type")
        job?.cancel()
        job = lifecycleScope.launch {
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
        workInfo.observe(viewLifecycleOwner, Observer {
            when (it.state) {
                WorkInfo.State.SUCCEEDED -> {
                    //获取数据完成
                    binding.pgbLoading.visibility = View.INVISIBLE
                    binding.tvNoData.visibility = View.INVISIBLE
                    binding.srlFragmentList.isRefreshing = false
                }
                WorkInfo.State.FAILED -> {
                    binding.srlFragmentList.isRefreshing = false
                    binding.pgbLoading.visibility = View.INVISIBLE
                    binding.tvNoData.visibility = View.VISIBLE
                }
                WorkInfo.State.RUNNING -> {
                    //正在获取数据
                    if (firstLoad){
                        binding.pgbLoading.visibility = View.VISIBLE
                    }
                    binding.tvNoData.visibility = View.INVISIBLE
                }
                else -> {}
            }
        })
    }

}