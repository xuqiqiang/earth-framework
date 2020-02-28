/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * LiveEarth
 */
package com.snailstudio2010.earthframework.layer;

import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.data.TileKey;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.ImageTiledLayer;

/**
 * Created by xuqiqiang on 2019/07/16.
 */
public class GoogleLayerImage extends ImageTiledLayer {

    private String baseurl = "%sgetTileImage?x=%d&y=%d&z=%d";

    public GoogleLayerImage(TileInfo tileinf, Envelope fillextent) {
        super(tileinf, fillextent);
    }

    @Override
    protected byte[] getTile(TileKey tagtile) {
        return NetService.GetByteFromUrl(this.getMapUrl(tagtile.getLevel(), tagtile.getColumn(), tagtile.getRow()));
    }

    private String getMapUrl(int level, int col, int row) {
        level -= 1;
        row = (int) (3 * Math.pow(2, level) / 4) - row - 1;
        String url = String.format(baseurl, NetService.GetServerHeader(), col, row, level);
        return url;
    }
}
