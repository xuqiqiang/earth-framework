package com.snailstudio2010.earthframework.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.snailstudio2010.earthframework.adapter.GalleryAdapter;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2019/08/29.
 */
public class GalleryView extends RelativeLayout {

    public ViewPagerGallery vpGallery;
    private GalleryAdapter mAdapter;
    private Context mContext;
    private OnGalleryListener mOnGalleryListener;
    private ObjectAnimator mObjectAnimator;
    private List<ArticlePoint> mList = new ArrayList<>();
    private boolean isShow;
    private int height;

    public GalleryView(Context context) {
        this(context, null);
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;

        vpGallery = new ViewPagerGallery(context);
        height = (int) DisplayUtils.dip2px(context, 268);
        this.addView(vpGallery, new RelativeLayout.LayoutParams(
                (int) DisplayUtils.dip2px(context, 278),
                height));

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setGravity(Gravity.CENTER);
        this.setClipChildren(false);
        initView();
    }

    public void setOnGalleryListener(OnGalleryListener listener) {
        mOnGalleryListener = listener;
        mAdapter.setOnItemSelectListener(listener);
    }

    private void initView() {
        this.setOnTouchListener((v, event) -> vpGallery.dispatchTouchEvent(event));
        mAdapter = new GalleryAdapter(mContext, vpGallery, mList);
        vpGallery.setAdapter(mAdapter);
        vpGallery.setOffscreenPageLimit(5);
        vpGallery.setPageTransformer(true, new TranslationPageTransformer());
        vpGallery.setPageMargin(0);

        vpGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mAdapter.onPageSelected(position);
                if (mOnGalleryListener != null)
                    mOnGalleryListener.onGalleryItemSelect(position, mList.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void show(List<ArticlePoint> list) {
        height = (int) DisplayUtils.dip2px(mContext, !ArrayUtils.isEmpty(list) && list.size() > 1 ? 220 : 268);
        this.setTranslationY(height);
        RelativeLayout.LayoutParams rll = (RelativeLayout.LayoutParams) vpGallery.getLayoutParams();
        rll.width = (int) DisplayUtils.dip2px(mContext, !ArrayUtils.isEmpty(list) && list.size() > 1 ? 278 : 361);
        rll.height = height;
        vpGallery.setLayoutParams(rll);

        mList.clear();
        mList.addAll(list);

        mAdapter = new GalleryAdapter(mContext, vpGallery, mList);
        vpGallery.setAdapter(mAdapter);
        mAdapter.setOnItemSelectListener(mOnGalleryListener);

        isShow = true;
        this.setVisibility(View.VISIBLE);

        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", height, 0);

        if (mObjectAnimator != null && mObjectAnimator.isRunning()) {
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhY);
        mObjectAnimator.setDuration(300);
        mObjectAnimator.start();
        mObjectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                GalleryView.this.requestLayout();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                GalleryView.this.requestLayout();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        new Handler().postDelayed(this::requestLayout, 100);
        new Handler().postDelayed(this::requestLayout, 200);
    }

    public boolean isShowing() {
        return isShow;
    }

    public void hide() {
        if (!isShow) return;
        isShow = false;
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", height);

        if (mObjectAnimator != null && mObjectAnimator.isRunning()) {
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhY);
        mObjectAnimator.setDuration(300);
        mObjectAnimator.start();
        mObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GalleryView.this.setVisibility(View.INVISIBLE);
            }
        });
    }

    public int getCurrentItem() {
        return vpGallery.getCurrentItem();
    }

    public interface OnGalleryListener {
        void onGalleryItemSelect(int position, ArticlePoint articleItem);

        void onGalleryItemClick(int position, ArticlePoint articleItem);

        void onGalleryClose();
    }
}