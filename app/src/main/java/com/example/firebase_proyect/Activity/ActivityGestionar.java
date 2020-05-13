package com.example.firebase_proyect.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.example.firebase_proyect.Adapter.PagerAdapter;
import com.example.firebase_proyect.R;
import com.google.android.material.tabs.TabLayout;

public class ActivityGestionar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar2);
//       setActionBar(myToolbar);

        getSupportActionBar().setTitle("Gestionar");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Alumnos"));
        tabLayout.addTab(tabLayout.newTab().setText("Asignaturas"));
        tabLayout.addTab(tabLayout.newTab().setText("Grupos"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(ActivityGestionar.this, "Seleccionado -> "+tab.getText(), Toast.LENGTH_SHORT).show();
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Toast.makeText(ActivityGestionar.this, "Deseleccionado -> "+tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Toast.makeText(ActivityGestionar.this, "Reseleccioando -> "+tab.getText(), Toast.LENGTH_SHORT).show();
            }
        });

    }



}
