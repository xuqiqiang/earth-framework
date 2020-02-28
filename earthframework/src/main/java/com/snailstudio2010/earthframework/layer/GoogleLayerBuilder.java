/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * LiveEarth
 */
package com.snailstudio2010.earthframework.layer;

import com.esri.arcgisruntime.arcgisservices.LevelOfDetail;
import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2019/07/16.
 */
public class GoogleLayerBuilder {

    public GoogleLayerBuilder() {
    }

    public GoogleLayerImage CreateImageLayer() {
        TileInfo inf = buildTileInfo(TileInfo.ImageFormat.JPG);

        Envelope fullextent = new Envelope(-180, -90, 180, 90, SpatialReferences.getWgs84());
        return new GoogleLayerImage(inf, fullextent);
    }

    public GoogleLayerLabel CreateLabelLayer() {
        TileInfo inf = buildTileInfo(TileInfo.ImageFormat.PNG);
        Envelope fullextent = new Envelope(-180, -90, 180, 90, SpatialReferences.getWgs84());
        return new GoogleLayerLabel(inf, fullextent);
    }

    private TileInfo buildTileInfo(TileInfo.ImageFormat formate) {

        double[] scales = new double[]{147914381.89788899, 73957190.948944002,
                36978595.474472001, 18489297.737236001, 9244648.8686180003,
                4622324.4343090001, 2311162.217155, 1155581.108577, 577790.554289,
                288895.277144, 144447.638572, 72223.819286, 36111.909643,
                18055.954822, 9027.9774109999998, 4513.9887049999998, 2256.994353,
                1128.4971760000001};

        int levels = 19;
        int dpi = 48;
        List<LevelOfDetail> details = new ArrayList<>(18);
        for (int i = 2; i <= levels; i++) {
            double resolution = 360 / Math.pow(2, i) / 256;
            details.add(new LevelOfDetail(i, resolution, scales[i - 2]));
        }
        TileInfo googletileinf = new TileInfo(
                dpi,
                formate,
                details,
                new Point(-180, 90, SpatialReferences.getWgs84()),
                SpatialReference.create(4326),
                256,
                256);
        return googletileinf;
    }
}
