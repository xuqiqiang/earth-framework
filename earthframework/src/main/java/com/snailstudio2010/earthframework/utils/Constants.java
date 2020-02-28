/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.utils;

/**
 * Created by xuqiqiang on 2019/08/12.
 */
public interface Constants {

    double mLatitude = 30.436475;
    double mLongitude = 104.309885;
    double mAltitude = 24000000;
    double mHeading = 0.0;
    double mPitch = 0.0;
    double mRoll = 0.0;

    float mFlyToPeriod = 1f;

    double[] mAltitudes = {
            5000000, // 国家
            500000, // 省
            50000, // 市
            10000, // 区
            3500, // 街道
    };
}
