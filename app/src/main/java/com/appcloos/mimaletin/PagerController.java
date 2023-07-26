package com.appcloos.mimaletin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerController extends FragmentPagerAdapter {
    int numOfTabs;

    public PagerController(@NonNull  FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numOfTabs = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
       switch (position){
           case 0:
                return new ReciboCobranza();
           case 1:
               return new VerRecibos();
           case 2:
               return new SubirRecibos();
           default:
               return null;
       }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
