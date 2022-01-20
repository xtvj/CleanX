package github.xtvj.cleanx.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.AppItemDao
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.ui.adapter.ListItemAdapter
import github.xtvj.cleanx.ui.viewmodel.ListViewModel
import github.xtvj.cleanx.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
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

    private val fragmentViewModel: ListViewModel by viewModels() //Fragment自己的ViewModel

    //private val viewModel: MainViewModel by activityViewModels() //与Activity共用的ViewModel
    private var type = -1
    private lateinit var binding: FragmentAppListBinding
    private val lifecycleScope = lifecycle.coroutineScope

    private var actionMode: ActionMode? = null
    private lateinit var selectionTracker: SelectionTracker<AppItem>
    private lateinit var rootDialog: AlertDialog
    private var job: Job? = null
    private var needObserver = true

    @Inject
    lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var toastUtils: ToastUtils

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
            val fragment = SheetDialog.create(item)
            fragment.clickListener = {
                lifecycleScope.launch {
                    if (!rootDialog.isShowing) {
                        rootDialog.show()
                    }
                }
            }
            fragment.show(childFragmentManager, "bottom_dialog")

//            lifecycleScope.launch{
//                appItemDao.updateName(item.id,item.name + "嗨")
//            }

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
        binding.srlFragmentList.setOnRefreshListener(this)

        adapter.addLoadStateListener {
            if (it.refresh is LoadState.Loading) {
                if (adapter.itemCount == 0) {
                    //可以显示加载progress或者显示retryButton
                    binding.pgbLoading.visibility = View.VISIBLE
                }
                binding.tvNoData.visibility = View.INVISIBLE
            } else {
                binding.tvNoData.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
                binding.pgbLoading.visibility = View.INVISIBLE
                binding.srlFragmentList.isRefreshing = false
//                if (it.refresh is LoadState.Error){
//                    binding.tvNoData.setOnClickListener {
//                        adapter.retry()
//                    }
//                }else{
//                    binding.tvNoData.setOnClickListener {
//                        adapter.refresh()
//                    }
//                }
            }
        }
        //查询不到数据时，点击刷新
        binding.tvNoData.setOnClickListener {
            adapter.refresh()
        }

        initDialog()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        if (needObserver) {
            lifecycleScope.launch {
                //viewModel是懒加载，必需在主线程中创建
                fragmentViewModel.run {
                    launch {
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
                }
                needObserver = false
            }
        }
    }

    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        log("observer Apps type =$type")
        job?.cancel()
        job = lifecycleScope.launch {
            apps.collectLatest {
                adapter.submitData(lifecycle, it)
            }
        }
    }


    private fun initDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_request_root, null)
        val textView = dialogView.findViewById<MaterialTextView>(R.id.tv_quest_root)
        textView.text = HtmlCompat.fromHtml(
            getString(R.string.request_root_message),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        textView.movementMethod = LinkMovementMethod.getInstance()

        rootDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.request_root_ok)) { dialog, _ ->
                lifecycleScope.launch {
                    val isRoot = withContext(Dispatchers.Default) {
                        log("isRootGiven")
                        RunnerUtils.isRootGiven()
                    }
                    if (isRoot) {
                        toastUtils.toastLong(R.string.got_root)
                    } else {
                        toastUtils.toastLong(R.string.need_to_open_root)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.request_root_cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()
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
        adapter.refresh()
    }

}