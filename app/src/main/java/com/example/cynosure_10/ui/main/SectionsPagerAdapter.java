package com.example.cynosure_10.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.cynosure_10.R;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;
    private ArrayList<String> groupInvites;
    private ArrayList<String> groupList;

    public SectionsPagerAdapter(Context context, FragmentManager fm, ArrayList<String> groupInvites, ArrayList<String> groupList) {
        super(fm);
        mContext = context;
        this.groupInvites = groupInvites;
        this.groupList = groupList;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Log.d("POSITION", position + "");
        switch (position) {
            case 0:
                return GroupsFragment.newInstance(position + 1, mContext, groupList);
            case 1:
                return InvitesFragment.newInstance(position + 1, mContext, groupInvites);
        }
        return GroupsFragment.newInstance(position + 1,  mContext, groupList);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}