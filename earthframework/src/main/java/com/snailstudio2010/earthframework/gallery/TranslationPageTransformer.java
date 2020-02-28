package com.snailstudio2010.earthframework.gallery;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.snailstudio2010.libutils.DisplayUtils;

/**
 * Created by xuqiqiang on 2019/08/29.
 */
public class TranslationPageTransformer implements ViewPager.PageTransformer {
    public static final float MIN_SCALE = 0.8f;

    public void transformPage(View page, float position) {
        float offset = Math.abs(position);
        if (offset > 1) {
            page.setTranslationY(DisplayUtils.dip2px(page.getContext(), 29));
        } else {
            if (offset > 0.29f) {
                offset = 0.29f;
            }
            page.setTranslationY(DisplayUtils.dip2px(page.getContext(), offset * 100));
        }
    }
}
