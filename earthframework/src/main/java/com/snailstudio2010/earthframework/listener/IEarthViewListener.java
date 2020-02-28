/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.listener;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.snailstudio2010.earthframework.EarthView;

public interface IEarthViewListener {

    void onTouch(View view, MotionEvent motionEvent);

    boolean onSingleTapConfirmed(MotionEvent e);

    void onScale(ScaleGestureDetector scaleGestureDetector);

    void onScaleEnd(ScaleGestureDetector scaleGestureDetector);

    void onScaleBegin(ScaleGestureDetector scaleGestureDetector);

    void onMultiPointerTap(MotionEvent motionEvent);

    void onDoubleTouchDrag(MotionEvent motionEvent);

    void onSinglePointerDown(MotionEvent motionEvent);

    void onSinglePointerUp(MotionEvent motionEvent);

    void onTwoPointerPitch(MotionEvent motionEvent, double pitchDelta);

    void onTwoPointerRotate(MotionEvent motionEvent, double rotationDelta);

    void onDoubleTap(MotionEvent motionEvent);

    void onSingleTapUp(MotionEvent motionEvent);

    void onScroll(MotionEvent motionEventFrom, MotionEvent motionEventTo, float distanceX, float distanceY);

    void onLongPress(MotionEvent motionEvent);

    void onFling();

    void onStateChanged(EarthView.EarthState state);
}