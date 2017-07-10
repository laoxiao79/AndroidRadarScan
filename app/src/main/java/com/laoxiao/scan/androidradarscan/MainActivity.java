package com.laoxiao.scan.androidradarscan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.laoxiao.scan.androidradarscan.ui.RadarScanView;

public class MainActivity extends AppCompatActivity {
    private RadarScanView mRadarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRadarView = (RadarScanView) findViewById(R.id.radar_view);
        mRadarView.setSearching(true);
        mRadarView.addPoint();
    }
}
