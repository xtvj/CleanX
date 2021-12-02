package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.whenResumed
import androidx.paging.PagingData
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.adapter.ListItemAdapter
import github.xtvj.cleanx.data.AppItem
import github.xtvj.cleanx.databinding.FragmentAppListBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class AppListFragment : Fragment(), ActionMode.Callback {

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

    //    private val viewModel: MainViewModel by activityViewModels() //与Activity共用的ViewModel
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreateView: $type")

        binding = FragmentAppListBinding.inflate(layoutInflater, container, false)

//        adapter = ListItemAdapter(imageLoaderX)
        //set our adapters to the RecyclerView
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
                                }
                                1 -> {
                                    getSystemApps()
                                    observeApps(systemList)
                                }
                                else -> {
                                    getUserApps()
                                    observeApps(disableList)
                                }
                            }
                        }
                        needLoadData = false
                    }
                }
            }
        }

        return binding.root
    }

    private fun observeApps(apps: Flow<PagingData<AppItem>>) {
        lifecycleScope.launch(Dispatchers.Main) {
            apps.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyActionMode(actionMode: ActionMode?) {
        selectionTracker.clearSelection()
        this.actionMode = null
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.cab,menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.item_disable ->{
                //todo
                Toast.makeText(context,"禁用：" + selectionTracker.selection.size() + "个",Toast.LENGTH_SHORT).show()
                mode?.finish()
            }
            R.id.item_enable ->{
                Toast.makeText(context,"启用：" + selectionTracker.selection.size() + "个",Toast.LENGTH_SHORT).show()
                mode?.finish()
            }
        }
        return false
    }
}