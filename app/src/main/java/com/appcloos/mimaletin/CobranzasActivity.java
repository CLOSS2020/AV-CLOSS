package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class CobranzasActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    PagerController pagerAdapter;

    ReciboCobranza reciboCobranza = new ReciboCobranza();
    VerRecibos verRecibos         = new VerRecibos();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranzas);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //mantener la orientacion vertical

        tabLayout = findViewById(R.id.TabLayout);
        viewPager = findViewById(R.id.ViewPager);

        pagerAdapter = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if(tab.getPosition() == 0){
                    pagerAdapter.notifyDataSetChanged();

                }

                if(tab.getPosition() == 1){
                    pagerAdapter.notifyDataSetChanged();

                }

                if(tab.getPosition() == 2){
                    pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }
}