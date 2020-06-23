package com.example.simplesetup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

public class LangSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button next_btn = findViewById(R.id.btn_scan);
        Spinner mLanguage = findViewById(R.id.lang_spinner);
        TextView mTextView = findViewById(R.id.lang_select_title);
        ArrayAdapter<String> mAdaptor = new ArrayAdapter<String>(
                LangSelectActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.language_option));
        mLanguage.setAdapter(mAdaptor);

        mLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Context context;
                Resources resources;
                switch (i) {
                    case 0:
                        changeLanguageSettings(Locale.ENGLISH);
                        break;
                    case 1:
                        changeLanguageSettings(Locale.CHINESE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LangSelectActivity.this, BarcodeActivity.class);
                LangSelectActivity.this.startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
            }
        });
    }

    public void changeLanguageSettings(Locale language/*Locale.GERMAN*/) {
        try {

            Class<?> activityManagerNative = Class.forName("android.app.ActivityManager");
            Object am = activityManagerNative.getMethod("getService").invoke(activityManagerNative);
            Configuration config = (Configuration) am.getClass().getMethod("getConfiguration").invoke(am);

            config.setLocale(language);
            config.getClass().getDeclaredField("userSetLocale").setBoolean(config, true);
            am.getClass().getMethod("updatePersistentConfiguration", android.content.res.Configuration.class).invoke(am, config);
            BackupManager.dataChanged("com.android.providers.settings");

            Log.d("changelanguage", "success!");

        } catch (Exception e) {
            Log.d("changelanguage", "error-->", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}