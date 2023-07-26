package com.appcloos.mimaletin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class PromocionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promociones);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//mantener la activity en vertical
    }
}