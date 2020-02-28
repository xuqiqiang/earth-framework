/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.snailstudio2010.earthframework.EarthView;
import com.snailstudio2010.earthframework.MarkerLayout;
import com.snailstudio2010.earthframework.MarkerPoint;
import com.snailstudio2010.earthframework.adapter.MarkerAdapter;
import com.snailstudio2010.earthframework.entity.ArticlePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by xuqiqiang on 2019/08/12.
 */
public class EarthActivity extends BaseActivity implements MarkerLayout.OnMarkerTapListener, AMapLocationListener {

    private EarthView mEarthView;
    private MarkerAdapter mMarkerAdapter;
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earth);

        mEarthView = findViewById(R.id.earthView);
        mEarthView.init(this::onRefreshMarkers);
        mEarthView.setOnMarkerTapListener(this);

        ImageView ivRefresh = findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(v -> {
            if (mMarkerAdapter != null) mMarkerAdapter.clear();
            mEarthView.resetMap(() -> mHandler.postDelayed(this::onRefreshMarkers, 1000));
        });

        ImageView ivLocation = findViewById(R.id.iv_location);
        ivLocation.setOnClickListener(v -> {
            if (mEarthView.startLocation(true, true, true, this)) {
                mProgressDialog = ProgressDialog.show(this,
                        null, "定位中", true, true);
            }
        });
    }

    private void onRefreshMarkers() {
        List<ArticlePoint> list = new ArrayList<>();
        for (String info : Constants.mInfos) {
            list.add(new ArticlePoint(Math.random() * 180, Math.random() * 70,
                    info,
                    Constants.mPhotos[new Random().nextInt(Constants.mPhotos.length)],
                    Constants.mLocations[new Random().nextInt(Constants.mLocations.length)]));
        }

        if (mMarkerAdapter == null) {
            mMarkerAdapter = new MarkerAdapter(this, list);
            mEarthView.setAdapter(mMarkerAdapter);
        } else {
            mMarkerAdapter.setData(list);
        }
    }

    @Override
    public void onMarkerTap(MarkerPoint hashPoint, Set<MarkerPoint> set) {
        mEarthView.flyToMarker(hashPoint, set, true);
    }

    @Override
    public void onMapStop() {
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
