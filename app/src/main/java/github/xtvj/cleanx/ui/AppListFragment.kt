package github.xtvj.cleanx.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.helpers.ActionModeHelper
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.adapter.SimpleItem
import github.xtvj.cleanx.databinding.AppListFragmentBinding
import github.xtvj.cleanx.utils.ImageLoader.ImageLoaderX
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel
import github.xtvj.cleanx.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
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
    private lateinit var mActionModeHelper: ActionModeHelper<SimpleItem>
    private val lifecycleScope = lifecycle.coroutineScope

    @Inject
    lateinit var imageLoaderX : ImageLoaderX


    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        log("onCreateView:")
        binding = AppListFragmentBinding.inflate(layoutInflater, container, false)

        fastAdapter.setHasStableIds(true)
        selectExtension = fastAdapter.getSelectExtension()
        selectExtension.apply {
            isSelectable = true
            multiSelect = true
            selectOnLongClick = true
            selectionListener = object : ISelectionListener<SimpleItem> {
                override fun onSelectionChanged(item: SimpleItem, selected: Boolean) {
                    log(
                        "FastAdapter",
                        "SelectedCount: " + selectExtension.selections.size + " ItemsCount: " + selectExtension.selectedItems.size
                    )
                }
            }
        }


        fastAdapter.onPreClickListener =
            { _: View?, _: IAdapter<SimpleItem>, item: SimpleItem, postion: Int ->
                //we handle the default onClick behavior for the actionMode. This will return null if it didn't do anything and you can handle a normal onClick
                val res = mActionModeHelper.onClick(item)
                res ?: false
            }

        fastAdapter.onClickListener =
            { v: View?, _: IAdapter<SimpleItem>, item: SimpleItem, pos: Int ->
                if (v != null) {
                    Toast.makeText(
                        v.context,
                        "item: " + item.name + " itemID: " + item.id,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }

        fastAdapter.onPreLongClickListener =
            { view: View, _: IAdapter<SimpleItem>, simpleItem: SimpleItem, position: Int ->
                val actionMode =
                    mActionModeHelper.onLongClick(activity as AppCompatActivity, position)
                if (actionMode != null) {
                    //we want color our CAB
                    view.findViewById<ConstraintLayout>(R.id.cl_item_app)
                        .setBackgroundColor(R.color.purple_200)
                }
                //if we have no actionMode we do not consume the event
                actionMode != null
            }

        //we init our ActionModeHelper
        mActionModeHelper = ActionModeHelper(fastAdapter, R.menu.cab, ActionBarCallBack())

        binding.rvApp.layoutManager = LinearLayoutManager(context)
//        binding.rvApp.itemAnimator = DefaultAnimator()

        //set our adapters to the RecyclerView
        binding.rvApp.adapter = fastAdapter
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(KEY_ITEM_TEXT) ?: throw IllegalStateException()
        log("onCreate: $type")
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
        }
    }

    private fun observeApps(apps: MutableLiveData<List<String>>, savedInstanceState: Bundle?) {
        apps.observe(this, {
            fragmentViewModel.upData(it)

        })

        fragmentViewModel.list.observe(this, {
            itemAdapter.add(it)
            //restore selections (this has to be done after the items were added
            fastAdapter.withSavedInstanceState(savedInstanceState)
        })
    }

    override fun onPause() {
        super.onPause()
        log("fragment is on pause , type: $type")
    }

    /**
     * Our ActionBarCallBack to showcase the CAB
     */
    internal inner class ActionBarCallBack : ActionMode.Callback {

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

//            find out the current visible position
            var firstVisiblePosition = 0
            if (binding.rvApp.layoutManager is LinearLayoutManager) {
                firstVisiblePosition =
                    (binding.rvApp.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            } else if (binding.rvApp.layoutManager is GridLayoutManager) {
                firstVisiblePosition =
                    (binding.rvApp.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            }

            when (item.itemId) {
                android.R.id.home -> {
                    requireActivity().onBackPressed()
                    mode.finish()
                    return true
                }
                R.id.item_delete -> {
                    log("delete")
                    selectExtension.deleteAllSelectedItems()
                    mode.finish()
                    return true
                }
                R.id.item_add -> {
                    val simpleItem = SimpleItem(imageLoaderX)
                    simpleItem.withID("android","安卓系统","11",false)
                    itemAdapter.add(firstVisiblePosition + 1, simpleItem)
                    mode.finish()
                    return true
                }
                else ->{
                    //as we no longer have a selection so the actionMode can be finished
                    //action bar 消失
                    mode.finish()
                    //we consume the event
                    return true
                }
            }
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {}

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }
    }

}