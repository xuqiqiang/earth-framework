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
public class GoogleLayerLabel extends ImageTiledLayer {

    private String baseurl = "https://t0.tianditu.gov.cn/DataServer?T=cia_c&x=%d&y=%d&l=%d&tk=22596bb83f470235c2eda254e6b4c2de";

    public GoogleLayerLabel(TileInfo tileinf, Envelope fillextent) {
        super(tileinf, fillextent);
    }

    @Override
    protected byte[] getTile(TileKey tagtile) {
        return NetService.GetByteFromUrl(this.getMapUrl(tagtile.getLevel(), tagtile.getColumn(), tagtile.getRow()));
    }

    private String getMapUrl(int level, int col, int row) {
        level -= 1;
        String url = String.format(baseurl, col, row, level);
        return url;
    }
}
