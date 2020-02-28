/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.snailstudio2010.earthframework.utils.EarthUtils;

import static com.snailstudio2010.earthframework.MarkerLayout.Z_INDEX_MARKER_POINT;
import static com.snailstudio2010.earthframework.MarkerLayout.Z_INDEX_MARKER_WINDOW;

class Marker {

    int direct;
    private Point mPoint;
    private GraphicsOverlay mGraphicsOverlay;
    private Graphic mGraphicPoint;
    private Graphic mGraphicWindow;
    private ValueAnimator mAnimator;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    Marker(Point p, GraphicsOverlay graphicsOverlay) {

        this.mPoint = p;
        this.mGraphicsOverlay = graphicsOverlay;
        mGraphicPoint = new Graphic(p);
        mGraphicPoint.setZIndex(Z_INDEX_MARKER_POINT);
        mGraphicWindow = new Graphic(p);
        mGraphicWindow.setZIndex(Z_INDEX_MARKER_WINDOW);

        EarthUtils.addGraphic(mGraphicsOverlay, mGraphicPoint);
        EarthUtils.addGraphic(mGraphicsOverlay, mGraphicWindow);

        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(
                SimpleMarkerSymbol.Style.CIRCLE, 0xFF779BF1, 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 2.0f));
        mGraphicPoint.setSymbol(pointSymbol);
    }

    public Graphic getPoint() {
        return mGraphicPoint;
    }

    public void setPoint(Graphic point) {
        this.mGraphicPoint = point;
    }

    public Graphic getWindow() {
        return mGraphicWindow;
    }

    public void setWindow(Graphic window, int direct) {
        this.mGraphicWindow = window;
        this.direct = direct;
    }

    public void changeWindowSymbol(PictureMarkerSymbol markerSymbol) {
        Graphic lastWindow = mGraphicWindow;

        mGraphicWindow = new Graphic(mPoint, markerSymbol);
        mGraphicWindow.setZIndex(Z_INDEX_MARKER_WINDOW);
        EarthUtils.addGraphic(mGraphicsOverlay, mGraphicWindow);

        if (lastWindow.getSymbol() == null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, lastWindow);
            markerSymbol.setOpacity(0);
            mHandler.post(() -> {

                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
                mAnimator = ValueAnimator.ofFloat(0, 1);
                mAnimator.setDuration(300);
                mAnimator.addUpdateListener(animation -> {
                    float currentValue = (float) animation.getAnimatedValue();
//                    logD("onAnimationUpdate: " + currentValue);
                    PictureMarkerSymbol markerSymbol1 = (PictureMarkerSymbol) mGraphicWindow.getSymbol();
                    markerSymbol1.setOpacity(currentValue);
                });
                mAnimator.start();
            });
        } else {
            mHandler.postDelayed(() -> EarthUtils.removeGraphic(mGraphicsOverlay, lastWindow), 70);
        }
    }

    public void remove() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(1, 0);
        mAnimator.setDuration(300);
        mAnimator.addUpdateListener(animation -> {
            float currentValue = (float) animation.getAnimatedValue();
//            logD("onAnimationUpdate: " + currentValue);
            PictureMarkerSymbol markerSymbol = (PictureMarkerSymbol) mGraphicWindow.getSymbol();
            markerSymbol.setOpacity(currentValue);

            if (currentValue <= 0) {
//                logD("onAnimationUpdate: finish");
                EarthUtils.removeGraphic(mGraphicsOverlay, mGraphicPoint);
                EarthUtils.removeGraphic(mGraphicsOverlay, mGraphicWindow);
            }
        });
        mAnimator.start();

//        mGraphicsOverlay.getGraphics().remove(mGraphicPoint);
//        mGraphicsOverlay.getGraphics().remove(mGraphicWindow);
    }
}