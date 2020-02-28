/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Earth Framework
 */
package com.snailstudio2010.earthframework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationToScreenResult;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.MarkerSymbol;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.snailstudio2010.earthframework.utils.EarthUtils;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.DisplayUtils;
import com.snailstudio2010.libutils.NotNull;
import com.snailstudio2010.libutils.ScreenUtils;
import com.snailstudio2010.libutils.SingleTaskHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.snailstudio2010.earthframework.utils.EarthUtils.logD;
import static com.snailstudio2010.earthframework.utils.EarthUtils.logE;

public class MarkerLayout implements SensorEventListener {

    @SuppressWarnings("all")
    static final int Z_INDEX_LOCATION = 1;
    @SuppressWarnings("all")
    static final int Z_INDEX_SEARCH_POINT = 2;
    static final int Z_INDEX_MARKER_POINT = 3;
    static final int Z_INDEX_MARKER_WINDOW = 4;
    private static final int DIRECT_TOP_RIGHT = 1;
    private static final int DIRECT_TOP_LEFT = 2;
    private static final int DIRECT_BOTTOM_RIGHT = 3;
    private static final int DIRECT_BOTTOM_LEFT = 4;
    private static final boolean CHANGE_DIRECT_WHEN_MOVING = false;
    private static final boolean USE_COMPASS = false;
    private static final int MESSAGE_MAP_MOVE_STOP = 1;
    private static final int MARKER_WIDTH = 138;
    private static final int MARKER_HEIGHT = 57;
    private static final Object mMarkerLock = new Object();
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Context context;
    private SceneView mSceneView;
    private Graphic mLocationPointGraphic;
    private Graphic mLocationGraphic;
    private Graphic mSearchLocationGraphic;
    private MarkerSymbol mLocationMarkerSymbol;
    private GraphicsOverlay mGraphicsOverlay;
    private boolean isMapMoving;
    private Bitmap mBitmapLocation;
    private Bitmap mBitmapLocationCompass;
    private Bitmap mBitmapSearchLocation;
    private Map<MarkerPoint, Set<MarkerPoint>> map;
    private Map<MarkerPoint, Marker> mMarkerMap = new LinkedHashMap<>();
    private OnMarkerTapListener mOnMarkerTapListener;
    private long mMapStopTime;
    private volatile boolean enabled;
    private Adapter mAdapter;
    private SensorManager mSensorManager;

    private SingleTaskHandler mHandler = new SingleTaskHandler(Looper.getMainLooper(), msg -> {
        if (msg.what == MESSAGE_MAP_MOVE_STOP) {
            onMapStop();
        }
        return false;
    });

    public MarkerLayout(Context context, SceneView sceneView) {
        this.context = context;
        this.mSceneView = sceneView;
        createGraphicsOverlay();
        initBitmap();
        if (mSceneView != null) {
            mSceneView.addViewpointChangedListener(viewpointChangedEvent -> {
                if (!isMapMoving) {
                    isMapMoving = true;
                }

                if (CHANGE_DIRECT_WHEN_MOVING && enabled) {
                    for (Map.Entry<MarkerPoint, Marker> entity : mMarkerMap.entrySet()) {
                        MarkerPoint hashPoint = entity.getKey();
                        Marker marker = entity.getValue();
                        Point point = new Point(hashPoint.x, hashPoint.y, SpatialReferences.getWgs84());
                        if (mSceneView == null) return;
                        android.graphics.Point screenPoint = mSceneView.locationToScreen(point).getScreenPoint();

                        int direct = calcDirect(screenPoint);
                        if (direct != marker.direct && !ArrayUtils.isEmpty(map)) {
                            Graphic graphic = createMarkerGraphic(
                                    hashPoint, direct, map.get(hashPoint));
                            EarthUtils.removeGraphic(mGraphicsOverlay, marker.getWindow());
                            marker.setWindow(graphic, direct);
                        }
                    }
                }

                mHandler.removeMessages(MESSAGE_MAP_MOVE_STOP);
                mHandler.sendEmptyMessageDelayed(MESSAGE_MAP_MOVE_STOP, 100);
            });
        }
    }

    private static int calcDirect(android.graphics.Point screenPoint) {
        if (screenPoint == null) return DIRECT_TOP_RIGHT;
        if (screenPoint.x > ScreenUtils.getWidthPixels() / 2) {
            if (screenPoint.y < ScreenUtils.getHeightPixels() / 2) return DIRECT_TOP_RIGHT;
            else return DIRECT_BOTTOM_RIGHT;
        } else {
            if (screenPoint.y < ScreenUtils.getHeightPixels() / 2) return DIRECT_TOP_LEFT;
            else return DIRECT_BOTTOM_LEFT;
        }
    }

    private void onMapStop() {
        long now = System.currentTimeMillis();
        if (now - mMapStopTime < 1000) {
            logE("onMapStop post delayed");
            mHandler.postDelayedTask(() -> {
                if (System.currentTimeMillis() - mMapStopTime < 1000) return;
                onMapStop();
            }, 1100);
            return;
        }
        logD("ViewpointChanged stop");
        mMapStopTime = now;
        isMapMoving = false;
        synchronized (mMarkerLock) {
            if (mExecutorService != null)
                mExecutorService.execute(this::updateMarkers);
        }
        if (mOnMarkerTapListener != null)
            mOnMarkerTapListener.onMapStop();
    }

    public void setOnMarkerTapListener(OnMarkerTapListener listener) {
        mOnMarkerTapListener = listener;
    }

    private void createGraphicsOverlay() {
        if (mSceneView == null) return;
        mGraphicsOverlay = new GraphicsOverlay();
        mSceneView.getGraphicsOverlays().add(mGraphicsOverlay);
    }

    public GraphicsOverlay getGraphicsOverlay() {
        return mGraphicsOverlay;
    }

    private void initBitmap() {
        mBitmapLocation = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_location);
        mBitmapLocationCompass = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_location_compass);
        mBitmapSearchLocation = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_location_small);
    }

    private Graphic createMarkerGraphic(MarkerPoint hashPoint, int direct, Set<MarkerPoint> set) {
        MarkerSymbol markerSymbol = createMarker(hashPoint, direct, set);
        Graphic pinStarBlueGraphic = new Graphic(new Point(hashPoint.x, hashPoint.y, SpatialReferences.getWgs84()), markerSymbol);
        pinStarBlueGraphic.setZIndex(Z_INDEX_MARKER_WINDOW);
        EarthUtils.addGraphic(mGraphicsOverlay, pinStarBlueGraphic);
        return pinStarBlueGraphic;
    }

    @SuppressWarnings("unchecked")
    private PictureMarkerSymbol createMarker(MarkerPoint hashPoint, int direct, Set<MarkerPoint> set) {
        PictureMarkerSymbol markerSymbol = mAdapter.getMarkerSymbol(hashPoint, set, direct);
        markerSymbol.loadAsync();
        return markerSymbol;
    }

    private void initMarkers() {
        synchronized (mMarkerLock) {
            clearMarkers();
            enabled = true;
            mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.execute(this::updateMarkers);
        }
    }

    public void setAdapter(@NotNull Adapter adapter) {
        this.mAdapter = adapter;
        adapter.setMarkerLayout(this);
        initMarkers();
    }

    private void clearMarkers() {
        enabled = false;
        synchronized (mMarkerLock) {
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
                mExecutorService = null;
            }

            if (!ArrayUtils.isEmpty(map)) {
                for (Map.Entry<MarkerPoint, Set<MarkerPoint>> entity : map.entrySet()) {
                    Set<MarkerPoint> set = entity.getValue();
                    if (set != null) set.clear();
                }
                map.clear();
            }
            for (Map.Entry<MarkerPoint, Marker> entity : mMarkerMap.entrySet()) {
                Marker lastMarker = entity.getValue();
                if (lastMarker != null) {
                    lastMarker.remove();
                }
            }
            mMarkerMap.clear();
        }
    }

    @SuppressWarnings("unused")
    public void createLocationPointGraphics(Location location) {
        if (mLocationPointGraphic != null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, mLocationPointGraphic);
        }
        Point point = new Point(location.getLongitude(),
                location.getLatitude(), SpatialReferences.getWgs84());
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(
                SimpleMarkerSymbol.Style.CIRCLE, 0xFFFFC209, 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 2.0f));
        mLocationPointGraphic = new Graphic(point, pointSymbol);

        EarthUtils.addGraphic(mGraphicsOverlay, mLocationPointGraphic);
    }

    public void createLocationGraphic(Location location) {
        createLocationGraphic(location.getLongitude(), location.getLatitude(), false);
    }

    public void createLocationGraphic(Location location, boolean useCompass) {
        createLocationGraphic(location.getLongitude(), location.getLatitude(), useCompass);
    }

    @SuppressWarnings("deprecation")
    private void createLocationGraphic(double longitude, double latitude, boolean useCompass) {
        if (mLocationGraphic != null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, mLocationGraphic);
        }
        if (useCompass && mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor defaultSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            mSensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        Point point = new Point(longitude, latitude, SpatialReferences.getWgs84());

        mLocationMarkerSymbol = new PictureMarkerSymbol(new BitmapDrawable(
                useCompass ? mBitmapLocationCompass : mBitmapLocation));
        mLocationGraphic = new Graphic(point, mLocationMarkerSymbol);
        mLocationGraphic.setZIndex(Z_INDEX_LOCATION);
        EarthUtils.addGraphic(mGraphicsOverlay, mLocationGraphic);
    }

    public void removeLocationGraphic() {
        if (mLocationGraphic != null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, mLocationGraphic);
            mLocationGraphic = null;
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    @SuppressWarnings("deprecation")
    public void createSearchLocationGraphic(double longitude, double latitude) {
        if (mSearchLocationGraphic != null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, mSearchLocationGraphic);
        }
        Point point = new Point(longitude, latitude, SpatialReferences.getWgs84());
        mSearchLocationGraphic = new Graphic(point, new PictureMarkerSymbol(new BitmapDrawable(mBitmapSearchLocation)));
        mSearchLocationGraphic.setZIndex(Z_INDEX_SEARCH_POINT);
        EarthUtils.addGraphic(mGraphicsOverlay, mSearchLocationGraphic);
    }

    public void removeSearchLocationGraphic() {
        if (mSearchLocationGraphic != null) {
            EarthUtils.removeGraphic(mGraphicsOverlay, mSearchLocationGraphic);
            mSearchLocationGraphic = null;
        }
    }

    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            onMapStop();
        }
    }

    public void onScaleEnd() {
//        updateMarkers();
//        logD(mSceneView.getCurrentViewpointCamera().getLocation());
    }

    private void requestUpdate() {
        synchronized (mMarkerLock) {
            if (mExecutorService != null) {
                mExecutorService.execute(this::updateMarkers);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateMarkers() {
        if (mSceneView == null || mAdapter == null || !enabled) return;
        Map<MarkerPoint, Set<MarkerPoint>> newMap = calcPoints();
        synchronized (mMarkerLock) {
            boolean checkFlag = ArrayUtils.isEmpty(map) || !map.equals(newMap);
            if (!enabled || !checkFlag) return;
        }
        if (BuildConfig.LOG_DEBUG) logD("map:" + newMap.keySet().size());

        for (Map.Entry<MarkerPoint, Set<MarkerPoint>> entity : newMap.entrySet()) {
            if (BuildConfig.LOG_DEBUG)
                logD("map entity:" + entity.getKey() + "," + entity.getValue());

            MarkerPoint hashPoint = entity.getKey();
            Set<MarkerPoint> set = entity.getValue();
            Set<MarkerPoint> lastSet = null;

            final boolean isLastMapEmpty = ArrayUtils.isEmpty(map);
            if (!isLastMapEmpty) {
                lastSet = map.get(hashPoint);
            }

            if (isLastMapEmpty || !Objects.equals(lastSet, set)) {

                Point point = new Point(hashPoint.x, hashPoint.y, SpatialReferences.getWgs84());
                android.graphics.Point screenPoint = mSceneView.locationToScreen(point).getScreenPoint();

                int direct = calcDirect(screenPoint);

                final Set<MarkerPoint> finalLastSet = lastSet;

                Runnable runnable = () -> {
                    if (!enabled) return;
                    Marker marker;
                    synchronized (mMarkerLock) {
                        marker = mMarkerMap.get(hashPoint);
                    }

                    boolean needChangeWindowSymbol = true;
                    if (marker == null) {
                        marker = new Marker(point, mGraphicsOverlay);
                    } else {
                        if (!enabled) return;
                        synchronized (mMarkerLock) {
                            if (!isLastMapEmpty) {
                                if (ArrayUtils.isEmpty(finalLastSet) && ArrayUtils.isEmpty(set) ||
                                        !ArrayUtils.isEmpty(finalLastSet) && !ArrayUtils.isEmpty(set)) {
                                    needChangeWindowSymbol = false;
                                }
                            }
                        }
                    }

                    if (needChangeWindowSymbol)
                        marker.changeWindowSymbol(createMarker(hashPoint, direct, set));

                    if (!enabled) return;
                    synchronized (mMarkerLock) {
                        mMarkerMap.put(hashPoint, marker);
                    }
                };

                if (!mAdapter.needLoadAsync(hashPoint)) {
                    runnable.run();
                } else {
                    mAdapter.onLoadAsync(hashPoint, () -> {
                        synchronized (mMarkerLock) {
                            if (mExecutorService != null) {
                                mExecutorService.execute(runnable);
                            }
                        }
                    });
                }

                if (!enabled) return;
                synchronized (mMarkerLock) {
                    if (!ArrayUtils.isEmpty(map)) {
                        map.remove(hashPoint);
                    }
                }
            }
        }

        if (!enabled) return;
        synchronized (mMarkerLock) {
            if (!ArrayUtils.isEmpty(map)) {
                for (Map.Entry<MarkerPoint, Set<MarkerPoint>> entity : map.entrySet()) {
                    if (BuildConfig.LOG_DEBUG)
                        logD("map entity:" + entity.getKey() + "," + entity.getValue());

                    MarkerPoint hashPoint = entity.getKey();
                    if (!Objects.equals(map.get(hashPoint), newMap.get(hashPoint))) {
                        Marker lastMarker = mMarkerMap.get(hashPoint);
                        if (lastMarker != null) {
//                            mHandler.post(lastMarker::remove);
                            mHandler.post(() -> {
                                try {
                                    lastMarker.remove();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            mMarkerMap.remove(hashPoint);
                        }
                    }
                }
            }
            map = newMap;
        }
    }

    private Map<MarkerPoint, Set<MarkerPoint>> calcPoints() {
        Map<MarkerPoint, Set<MarkerPoint>> markerMap = new LinkedHashMap<>();
        if (!enabled) return markerMap;
        synchronized (mMarkerLock) {
            if (mAdapter.getItemCount() <= 0) return markerMap;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                MarkerPoint point = mAdapter.getItem(i);
                markerMap.put(point, new HashSet<>());
            }
        }
        List<MarkerPoint> list = new ArrayList<>(markerMap.keySet());
        for (int i = 0; i < list.size(); i++) {
            MarkerPoint point = list.get(i);
            Set<MarkerPoint> set = markerMap.get(point);
            if (set != null) {
                for (int j = i + 1; j < list.size(); j++) {
                    MarkerPoint pointB = list.get(j);
                    if (markerMap.get(pointB) != null && isOverlap(point, pointB)) {
                        set.add(pointB);
                        if (BuildConfig.LOG_DEBUG)
                            logD("isOverlap:" + i + "," + j + ", size:" + markerMap.keySet().size());
                        markerMap.remove(pointB);
                    }
                }
            }
        }
        return markerMap;
    }

    private boolean isOverlap(MarkerPoint a, MarkerPoint b) {
        if (mSceneView == null) return false;
        LocationToScreenResult resultA = mSceneView.locationToScreen(new Point(a.x, a.y, SpatialReferences.getWgs84()));
        LocationToScreenResult resultB = mSceneView.locationToScreen(new Point(b.x, b.y, SpatialReferences.getWgs84()));
        if (resultA.getVisibility() == LocationToScreenResult.SceneLocationVisibility.VISIBLE &&
                resultB.getVisibility() == LocationToScreenResult.SceneLocationVisibility.VISIBLE) {

            android.graphics.Point pA = getPosition(resultA.getScreenPoint());
            android.graphics.Point pB = getPosition(resultB.getScreenPoint());
//            logD("isOverlap0:" + pA.toString() + pB.toString()
//            + DisplayUtils.dip2px(context, MARKER_WIDTH) + "," + DisplayUtils.dip2px(context, MARKER_HEIGHT));
            return Math.abs(pA.x - pB.x) < DisplayUtils.dip2px(context, MARKER_WIDTH) &&
                    Math.abs(pA.y - pB.y) < DisplayUtils.dip2px(context, MARKER_HEIGHT);
        }
        return false;
    }

    private android.graphics.Point getPosition(android.graphics.Point pointA) {

        if (!CHANGE_DIRECT_WHEN_MOVING) return pointA;

        int direct = calcDirect(pointA);
        if (direct == DIRECT_BOTTOM_LEFT) {
            return new android.graphics.Point(
                    (int) (pointA.x + DisplayUtils.dip2px(context, MARKER_WIDTH) / 2),
                    (int) (pointA.y - DisplayUtils.dip2px(context, MARKER_HEIGHT) / 2));
        } else if (direct == DIRECT_BOTTOM_RIGHT) {
            return new android.graphics.Point(
                    (int) (pointA.x - DisplayUtils.dip2px(context, MARKER_WIDTH) / 2),
                    (int) (pointA.y - DisplayUtils.dip2px(context, MARKER_HEIGHT) / 2));
        } else if (direct == DIRECT_TOP_LEFT) {
            return new android.graphics.Point(
                    (int) (pointA.x + DisplayUtils.dip2px(context, MARKER_WIDTH) / 2),
                    (int) (pointA.y + DisplayUtils.dip2px(context, MARKER_HEIGHT) / 2));
        } else if (direct == DIRECT_TOP_RIGHT) {
            return new android.graphics.Point(
                    (int) (pointA.x - DisplayUtils.dip2px(context, MARKER_WIDTH) / 2),
                    (int) (pointA.y + DisplayUtils.dip2px(context, MARKER_HEIGHT) / 2));
        }
        return pointA;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float now = values[0];

//        logD("now:" + now + "," + mSceneView.getCurrentViewpointCamera().getHeading());

        if (mLocationMarkerSymbol != null) {
            mLocationMarkerSymbol.setAngle((float) (now - mSceneView.getCurrentViewpointCamera().getHeading()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @SuppressWarnings("unused")
    public void createPolylineGraphics(double[][] borderData) {
        PointCollection polylinePoints = new PointCollection(SpatialReferences.getWgs84());

        for (double[] item : borderData) {
            polylinePoints.add(new Point(item[0], item[1]));
        }

        Polyline polyline = new Polyline(polylinePoints);
        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.RED, 2.0f);
        Graphic polylineGraphic = new Graphic(polyline, polylineSymbol);
        EarthUtils.addGraphic(mGraphicsOverlay, polylineGraphic);
    }

    public boolean onSingleTap(MotionEvent e) {
        if (mSceneView == null || !enabled) return true;
        android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

        // identify graphics on the graphics overlay
        final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic =
                mSceneView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false, 1);

        identifyGraphic.addDoneListener(() -> {
            try {
                IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
                List<Graphic> graphic = grOverlayResult.getGraphics();
                if (!ArrayUtils.isEmpty(graphic) && enabled) {
                    synchronized (mMarkerLock) {
                        for (Map.Entry<MarkerPoint, Marker> entry : mMarkerMap.entrySet()) {
                            if (graphic.get(0).equals(entry.getValue().getWindow()) ||
                                    graphic.get(0).equals(entry.getValue().getPoint())) {
                                if (mOnMarkerTapListener != null) {
                                    Set<MarkerPoint> set = ArrayUtils.isEmpty(map) ? null : map.get(entry.getKey());
                                    mOnMarkerTapListener.onMarkerTap(entry.getKey(), set);
                                }
                                return;
                            }
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException ie) {
                ie.printStackTrace();
            }

        });
        return true;
    }

    public interface OnMarkerTapListener {
        void onMarkerTap(MarkerPoint hashPoint, Set<MarkerPoint> set);

        void onMapStop();
    }

    public abstract static class Adapter<P extends MarkerPoint, VH extends MarkerLayout.ViewHolder> {
        boolean isClear;
        private MarkerLayout mMarkerLayout;
        private VH mHolder;

        @NonNull
        public abstract VH onCreateViewHolder();

        public abstract void onBindViewHolder(@NonNull VH holder, P point, Set<P> set);

        public abstract P getItem(int position);

        public abstract int getCount();

        public boolean needLoadAsync(P point) {
            return false;
        }

        public void onLoadAsync(P point, Runnable resolve) {
            resolve.run();
        }

        @SuppressWarnings("all")
        protected boolean reuse() {
            return false;
        }

        @SuppressWarnings("all")
        protected void onRenderFinish(P point) {
        }

        @SuppressWarnings("all")
        public final void notifyDataSetChanged() {
            isClear = false;
            mMarkerLayout.initMarkers();
        }

        public void requestUpdate() {
            mMarkerLayout.requestUpdate();
        }

        public int getItemCount() {
            if (isClear) return 0;
            return getCount();
        }

        void setMarkerLayout(MarkerLayout markerLayout) {
            mMarkerLayout = markerLayout;
        }

        public void clear() {
            isClear = true;
            mMarkerLayout.clearMarkers();
        }

        @SuppressWarnings("unused")
        Bitmap getViewBitmap(P point, Set<P> set) {
            if (mHolder == null) mHolder = onCreateViewHolder();
            onBindViewHolder(mHolder, point, set);
            return getViewBitmap(mHolder.itemView, mHolder.getWidth(), mHolder.getHeight());
        }

        @SuppressWarnings("deprecation")
        PictureMarkerSymbol getMarkerSymbol(P point, Set<P> set, int direct) {
            long start = System.currentTimeMillis();
            VH holder;
            if (reuse()) {
                if (mHolder == null)
                    mHolder = onCreateViewHolder();
                holder = mHolder;
            } else {
                holder = onCreateViewHolder();
            }

            logD("onCreateViewHolder:" + (System.currentTimeMillis() - start));
//            VH holder = onCreateViewHolder();
            onBindViewHolder(holder, point, set);
            Bitmap bitmap = getViewBitmap(holder.itemView, holder.getWidth(), holder.getHeight());

            PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(new BitmapDrawable(bitmap));
            markerSymbol.setOffsetX(holder.getOffsetX(direct));
            markerSymbol.setOffsetY(holder.getOffsetY(direct));

            onRenderFinish(point);
            logD("onCreateViewHolder1:" + (System.currentTimeMillis() - start));
            return markerSymbol;
        }

        Bitmap getViewBitmap(View view, int width, int height) {
            view.setDrawingCacheEnabled(false);
            view.setDrawingCacheEnabled(true);
            if (reuse()) {
                if (view.getTag() == null || !"hasLayout".equalsIgnoreCase((String) view.getTag())) {
                    view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                    view.setTag("hasLayout");
                }
            } else {
                view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }

            view.buildDrawingCache();
            return view.getDrawingCache();
        }
    }

    public abstract static class ViewHolder {
        protected final View itemView;

        @SuppressWarnings("all")
        public ViewHolder(@NonNull View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            } else {
                this.itemView = itemView;
            }
        }

        public ViewHolder(Context context, @LayoutRes int layoutId) {
            if (layoutId == 0) {
                throw new IllegalArgumentException("layoutId may not be 0");
            } else {
                this.itemView = LayoutInflater.from(context).inflate(layoutId, null);
            }
        }

        public abstract int getWidth();

        public abstract int getHeight();

        public abstract float getOffsetX(int direct);

        public abstract float getOffsetY(int direct);
    }
}