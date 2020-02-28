/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.listener;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.snailstudio2010.earthframework.EarthView;

public class EarthViewListener implements IEarthViewListener {

    @Override
    public void onTouch(View view, MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public void onScale(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public void onScaleBegin(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public void onMultiPointerTap(MotionEvent motionEvent) {

    }

    @Override
    public void onDoubleTouchDrag(MotionEvent motionEvent) {

    }

    @Override
    public void onSinglePointerDown(MotionEvent motionEvent) {

    }

    @Override
    public void onSinglePointerUp(MotionEvent motionEvent) {

    }

    @Override
    public void onTwoPointerPitch(MotionEvent motionEvent, double pitchDelta) {

    }

    @Override
    public void onTwoPointerRotate(MotionEvent motionEvent, double rotationDelta) {

    }

    @Override
    public void onDoubleTap(MotionEvent motionEvent) {

    }

    @Override
    public void onSingleTapUp(MotionEvent motionEvent) {

    }

    @Override
    public void onScroll(MotionEvent motionEventFrom, MotionEvent motionEventTo, float distanceX, float distanceY) {

    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public void onFling() {

    }

    @Override
    public void onStateChanged(EarthView.EarthState state) {

    }
}