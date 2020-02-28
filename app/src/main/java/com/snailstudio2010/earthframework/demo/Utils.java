/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.demo;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.snailstudio2010.earthframework.utils.EarthUtils;
import com.snailstudio2010.libutils.NotNull;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class Utils {

    private Utils() {
    }

    public static float calcDuration(@NotNull SceneView sceneView) {
        double altitude = sceneView.getCurrentViewpointCamera().getLocation().getZ();
        return calcDuration(altitude);
    }

    public static float calcDuration(double targetAltitude) {
        float animationDuration = (float) (Math.abs((Constants.mAltitude - targetAltitude)
                / (Constants.mAltitude - Constants.mAltitudes[Constants.mAltitudes.length - 1])) * 2f);
        animationDuration = Math.max(Math.min(animationDuration, 2f), 0.5f);
        return animationDuration;
    }

    public static void resetMap(SceneView sceneView, Runnable runnable) {
        Camera camera = new Camera(Constants.mLatitude, Constants.mLongitude, Constants.mAltitude,
                Constants.mHeading, Constants.mPitch, Constants.mRoll);
        EarthUtils.moveMap(sceneView, camera, Utils.calcDuration(sceneView), runnable, false);
    }

    public static double getTargetAltitude(Point point) {
        for (double item : Constants.mAltitudes) {
            if (point.getZ() > item + 100) return item;
        }
        return Constants.mAltitudes[Constants.mAltitudes.length - 1];
    }
}
