package com.snailstudio2010.earthframework.gallery;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xuqiqiang on 2019/08/29.
 */
public class ViewPagerGallery extends ViewPager {

    private final static float DISTANCE = 10;
    private float downX;
    private float downY;
    private float upX;
    private float upY;

    public ViewPagerGallery(Context context) {
        this(context, null);
    }

    public ViewPagerGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downX = ev.getX();
            downY = ev.getY();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            upX = ev.getX();
            upY = ev.getY();
            if (Math.abs(upX - downX) > DISTANCE || Math.abs(upY - downY) > DISTANCE) {
                return super.dispatchTouchEvent(ev);
            }
            View view = clickPageOnScreen(ev);
            if (view != null) {
                int index = (Integer) view.getTag();
                if (getCurrentItem() != index) {
                    setCurrentItem(index);
                    return true;
                }
            } else {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private View clickPageOnScreen(MotionEvent ev) {
        int childCount = getChildCount();
        int currentIndex = getCurrentItem();
        int[] location = new int[2];
        float x = ev.getRawX();

        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            int position = (Integer) v.getTag();
            v.getLocationOnScreen(location);
            int minX = location[0];
            int maxX = location[0] + v.getWidth();

            if (position < currentIndex) {
                maxX -= v.getWidth() * (1 - TranslationPageTransformer.MIN_SCALE) * 0.5;
                minX -= v.getWidth() * (1 - TranslationPageTransformer.MIN_SCALE) * 0.5;
            }

            if ((x > minX && x < maxX)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                upY = ev.getY();
                if (Math.abs(upY - downY) > DISTANCE) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                upY = ev.getY();
                if (Math.abs(upY - downY) > DISTANCE) {
                    return super.onTouchEvent(ev);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }
}
