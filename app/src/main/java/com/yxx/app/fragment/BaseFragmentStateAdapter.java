package com.yxx.app.fragment;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class BaseFragmentStateAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList;

    public BaseFragmentStateAdapter(@NonNull FragmentManager fragmentManager,
                                    @NonNull Lifecycle lifecycle,
                                    @NonNull List<Fragment> fragmentList) {
        super(fragmentManager, lifecycle);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList != null ? (fragmentList.get(position) != null
                ? fragmentList.get(position) : new Fragment())
                : new Fragment();
    }

    @Override
    public int getItemCount() {
        return fragmentList != null ? fragmentList.size() : 0;
    }
}
