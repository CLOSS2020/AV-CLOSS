/* ******************************************************************************************************
 * 4 DE SEPTIEMBRE, 2020                                                                              *
 * AUTOR: PCV                                                                                         *
 * APP: MI MALETIN V 1.0.0                                                                            *
 * ESTA ES LA ACTIVITY QUE VALIDA SI HAY O NO UNA SESION ACTIVA                                       *
 *                                                                                                    *
 * -------------------------------------------------------------------------------------------------- *
 * 25 DE SEPTIEMBRE, 2020                                                                             *
 * AUTOR: PCV                                                                                         *
 * ACTUALIZACION: DOCUMENTANCION DE LAS CLASES                                                        *
 ******************************************************************************************************

 * */




package com.appcloos.mimaletin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class PresentacionActivity extends AppCompatActivity {

    ProgressBar progressBar; //DECLARAMOS EL OBJETO progressbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //metodo para mantener la vista vertical
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentacion);
        getSupportActionBar().hide(); //metodo para esconder la actionbar

        progressBar = findViewById(R.id.progressBar);  //enlazamos objeto con id
        progressBar.setVisibility(View.VISIBLE); // hacemos que sea visible

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {  //creamos un handler que se encargue de verificar a traves de las preferencias guardadas, si hay una sesion o no en curso.
                SharedPreferences preferences = getSharedPreferences("preferenciaslogin", Context.MODE_PRIVATE);
                boolean sesion = preferences.getBoolean("sesion", false);

                 if(sesion){
                     Intent intent = new Intent(getApplicationContext(),PrincipalActivity.class); // si hay sesion activa, llevame al principal
                     startActivity(intent);
                     finish();
                 } else {
                     Intent intent = new Intent(getApplicationContext(), MainActivity.class); //si no hay activa, llevame al login.
                     startActivity(intent);
                     finish();
                 }
            }
        }, 2000);

    }
}