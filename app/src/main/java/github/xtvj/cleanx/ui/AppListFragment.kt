package github.xtvj.cleanx.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.whenResumed
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.data.DataStoreManager
import github.xtvj.cleanx.data.SortOrder
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.shell.RunnerUtils
import github.xtvj.cleanx.ui.adapter.ListItemAdapter
import github.xtvj.cleanx.ui.viewmodel.ListViewModel
import github.xtvj.cleanx.utils.APPS_BY_ID
import github.xtvj.cleanx.utils.APPS_BY_LAST_UPDATE_TIME
import github.xtvj.cleanx.utils.APPS_BY_NAME
import github.xtvj.cleanx.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var rootDialog: AlertDialog

    @Inject
    lateinit var adapter: ListItemAdapter

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreateView: $type")

        binding = FragmentAppListBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setAdapterType(type)
        adapter.itemClickListener = { item ->
            val fragment = SheetDialog.create(item)
            fragment.clickListener = {
                lifecycleScope.launch {
                    if (!rootDialog.isShowing){
                        rootDialog.show()
                    }
                }
            }
            fragment.show(childFragmentManager,"bottom_dialog")
        }
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
        lifecycleScope.launch {
            lifecycle.whenResumed {

                //viewModel是懒加载，必需在主线程中创建
                fragmentViewModel.run {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (needLoadData) {
                            when (type) {
                                0 -> {
                                    getUserApps()
                                    observeApps(userList)
                                    observeReload(userReload)
                                }
                                1 -> {
                                    getSystemApps()
                                    observeApps(systemList)
                                    observeReload(systemReload)
                                }
                                else -> {
                                    getDisabledApps()
                                    observeApps(disableList)
                                    observeReload(disableReload)
                                }
                            }
                        }
                        needLoadData = false

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
        }
        binding.srlFragmentList.setOnRefreshListener(this)

        initDialog()
    }

    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        lifecycleScope.launch(Dispatchers.Main) {
            apps.collectLatest {
                adapter.submitData(lifecycle, it)
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
                        Toast.makeText(
                            context,
                            getString(R.string.got_root),
                            Toast.LENGTH_LONG
                        ).show()
                    }else{
                        Toast.makeText(
                            context,
                            getString(R.string.need_to_open_root),
                            Toast.LENGTH_LONG
                        ).show()
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