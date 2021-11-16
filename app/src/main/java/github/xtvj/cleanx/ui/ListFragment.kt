package github.xtvj.cleanx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import github.xtvj.cleanx.R
import github.xtvj.cleanx.utils.log
import github.xtvj.cleanx.viewmodel.ListViewModel

class ListFragment() : Fragment() {

    companion object {
        private const val KEY_ITEM_TEXT = "github.xtvj.cleanx.KEY_ITEM_FRAGMENT"
        fun create(itemText: String) =
            ListFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_ITEM_TEXT, itemText)
                }
            }
    }

    val viewModel: ListViewModel by viewModels()
    private lateinit var type : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("onCreateView:")

        return inflater.inflate(R.layout.list_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString(KEY_ITEM_TEXT)?: throw IllegalStateException()
        log("onCreate: $type")
//        when(tag){
//            "user" ->{
//
//            }
//            "system" ->{
//
//            }
//            "all" ->{
//
//            }
//        }
    }

}