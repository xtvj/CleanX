package github.xtvj.cleanx.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import github.xtvj.cleanx.R
import github.xtvj.cleanx.databinding.LicensesFragmentBinding
import github.xtvj.cleanx.ui.adapter.LicensesAdapter
import github.xtvj.cleanx.ui.viewmodel.LicensesViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LicensesFragment : Fragment() {

    private lateinit var viewModel: LicensesViewModel
    private lateinit var binding: LicensesFragmentBinding

    @Inject
    lateinit var adapter: LicensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LicensesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LicensesViewModel::class.java]

        binding.rvLicenses.adapter = adapter
        adapter.onItemClick = { license ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(license.address)
            activity?.startActivity(intent)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED){
                    activity?.title = getString(R.string.third_party_licenses)
                }
            }
            launch {
                adapter.submitData(PagingData.from(viewModel.getList()))
            }
        }
    }

}