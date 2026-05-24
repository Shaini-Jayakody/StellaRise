package com.example.stellarise.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.stellarise.fragments.HabitsFragment
import com.example.stellarise.fragments.MoodFragment
import com.example.stellarise.fragments.HydrationFragment
import com.example.stellarise.fragments.SettingsFragment

/**
 * ViewPager adapter for main navigation
 * Handles switching between different app sections
 */
class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val HABITS_PAGE = 0
        const val MOOD_PAGE = 1
        const val HYDRATION_PAGE = 2
        const val SETTINGS_PAGE = 3
    }

    // Store fragment references for easy access
    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            HABITS_PAGE -> HabitsFragment()
            MOOD_PAGE -> MoodFragment()
            HYDRATION_PAGE -> HydrationFragment()
            SETTINGS_PAGE -> SettingsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
        fragments[position] = fragment
        return fragment
    }

    /**
     * Safely get fragment instance by position
     */
    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }
}
