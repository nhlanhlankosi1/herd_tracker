package com.nhlanhlankosi.tablayoutdemo.viewPagers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nhlanhlankosi.tablayoutdemo.fragments.MyFarmFragment;
import com.nhlanhlankosi.tablayoutdemo.fragments.MyHerdFragment;
import com.nhlanhlankosi.tablayoutdemo.fragments.NotificationsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new MyHerdFragment();
            case 1:
                return new MyFarmFragment();
            default:
                return new NotificationsFragment();

        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
