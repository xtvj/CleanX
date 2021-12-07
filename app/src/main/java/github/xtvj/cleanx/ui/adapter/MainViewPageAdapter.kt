package github.xtvj.cleanx.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import github.xtvj.cleanx.ui.AppListFragment

class MainViewPageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AppListFragment.create(0)
            }
            1 -> {
                AppListFragment.create(1)
            }
            else -> {
                AppListFragment.create(2)
            }
        }
    }
}