package com.anirudh.EasyLocationFetch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.anirudh.locationfetch.EasyLocationFetch;
import com.anirudh.locationfetch.GeoLocationModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(v -> {
            try {
                GeoLocationModel geoLocationModel = new EasyLocationFetch(this).getLocationData();
                Toast.makeText(this, "" + geoLocationModel.getAddress(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
}