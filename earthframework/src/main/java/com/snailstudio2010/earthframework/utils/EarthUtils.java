/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework.utils;

import android.content.Context;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snailstudio2010.earthframework.BuildConfig;
import com.snailstudio2010.libutils.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class EarthUtils {
    public static final String TAG = "earth-framework";
    public static ImageLoader mImageLoader;

    static {
        System.loadLibrary("parabola");
    }

    private EarthUtils() {
    }

    public static native String parabola(double[] startPoint, double[] endPoint);

    public static void flyTo(SceneView sceneView, Point target, float animationDuration) {
        flyTo(sceneView, target, animationDuration, null, false);
    }

    public static void flyTo(SceneView sceneView, Point target, float animationDuration, Runnable runnable, boolean strict) {
        flyTo(sceneView, new Camera(target, 0, 0, 0), animationDuration, runnable, strict);
    }

    public static void flyTo(SceneView sceneView, Camera camera, float animationDuration, Runnable runnable, boolean strict) {

        Camera currentCamera = sceneView.getCurrentViewpointCamera();
        Point resp = currentCamera.getLocation();
        Point desp = camera.getLocation();
        Log.d("EarthView", "flyTo: " + resp.getY() + "," + resp.getX() + "," + resp.getZ() + "," +
                currentCamera.getHeading() + "," +  currentCamera.getPitch() + "," + currentCamera.getRoll());
        String parabola = parabola(
                new double[]{resp.getY(), resp.getX(), resp.getZ(),
                        currentCamera.getHeading(), currentCamera.getPitch(), currentCamera.getRoll()},
//                    new double[]{mLatitude, mLongitude, mAltitude, mHeading, mPitch, mRoll},
//                new double[]{42.644483, -109.084758, 200000, 0, 0, 0}
                new double[]{desp.getY(), desp.getX(), desp.getZ(),
                        camera.getHeading(), camera.getPitch(), camera.getRoll()}
        );
        Log.d("EarthView", "parabola: " + parabola);

        double[][] data = new Gson().fromJson(parabola, new TypeToken<double[][]>() {
        }.getType());

        Log.d("EarthView", "parabola length: " + data.length);

//            SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(
//                    SimpleMarkerSymbol.Style.CIRCLE, 0xFFFFC209, 10.0f);
//            Graphic mPlane3D = new Graphic(new Point(0, 0, 0, SpatialReferences.getWgs84()), pointSymbol);
//            mMarkerLayout.getGraphicsOverlay().getGraphics().add(mPlane3D);
//
//
//            OrbitGeoElementCameraController mOrbitCameraController
//                    = new OrbitGeoElementCameraController(mPlane3D, mAltitude);
////                    mOrbitCameraController.setCameraPitchOffset(75.0);
//            mSceneView.setCameraController(mOrbitCameraController);


        Timer mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i >= data.length - 1) {
                    mTimer.cancel();
                    if (runnable != null) runnable.run();
                    return;
                }
                double[] item = data[i++];

                Log.d("EarthView", "setViewpointCamera: "
                        + i + "," + item[1] + "," + item[0] + "," + item[2]
                        + "," + item[3] + "," + item[4] + "," + item[5]);

//                    mPlane3D.setGeometry(new Point(item[1], item[0], item[2], SpatialReferences.getWgs84()));
//                    mPlane3D.getAttributes().put("HEADING", item[3]);
//                    mPlane3D.getAttributes().put("PITCH", item[4]);
//                    mPlane3D.getAttributes().put("ROLL", item[5]);
//
//                    mOrbitCameraController.setCameraDistance(item[2]);

                sceneView.setViewpointCameraAsync(
                        new Camera(item[0], item[1], item[2], item[3], item[4], item[5]),
                        0);

            }
        }, 0, 5);
    }

    public static ImageLoader getImageLoader(Context context) {
        if (mImageLoader == null) mImageLoader = new ImageLoader(context);
        return mImageLoader;
    }

    public static void addGraphic(GraphicsOverlay graphicsOverlay, Graphic graphic) {
        if (graphicsOverlay != null
                && graphicsOverlay.getGraphics() != null
                && graphic != null) {
            try {
                graphicsOverlay.getGraphics().add(graphic);
            } catch (Exception e) {
                e.printStackTrace();
                logE("ArcGISRuntimeException: Out of range");
            }
        }
    }

    public static void removeGraphic(GraphicsOverlay graphicsOverlay, Graphic graphic) {
        if (graphicsOverlay != null
                && graphicsOverlay.getGraphics() != null
                && graphic != null) {
            try {
                graphicsOverlay.getGraphics().remove(graphic);
            } catch (Exception e) {
                e.printStackTrace();
                logE("java.util.NoSuchElementException");
            }
        }
    }

    public static void logD(String msg) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, msg);
    }

    public static void logE(String msg) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, msg);
    }

    public static void moveMap(SceneView sceneView, Camera camera, float animationDuration) {
        moveMap(sceneView, camera, animationDuration, null, false);
    }

    public static void moveMap(SceneView sceneView, Camera camera, float animationDuration, Runnable runnable, boolean strict) {

        ListenableFuture<Boolean> listenableFuture = sceneView.setViewpointCameraAsync(
                camera, animationDuration);
        if (runnable != null) {
            listenableFuture.addDoneListener(() -> {
                if (!strict) runnable.run();
                else {
                    Point point = sceneView.getCurrentViewpointCamera().getLocation();
                    if (point.getZ() < camera.getLocation().getZ() + 100 && point.getZ() > camera.getLocation().getZ() - 100) {
                        runnable.run();
                    }
                }
            });
        }
    }

    public static void moveMap(SceneView sceneView, Point target, float animationDuration) {
        moveMap(sceneView, target, animationDuration, null, false);
    }

    public static void moveMap(SceneView sceneView, Point target, float animationDuration, Runnable runnable, boolean strict) {
        Camera camera = sceneView.getCurrentViewpointCamera();
        moveMap(sceneView, camera.moveTo(target), animationDuration, runnable, strict);
    }

    public static double getTargetAltitude(Point point) {
        for (double item : Constants.mAltitudes) {
            if (point.getZ() > item + 100) return item;
        }
        return Constants.mAltitudes[Constants.mAltitudes.length - 1];
    }
}
