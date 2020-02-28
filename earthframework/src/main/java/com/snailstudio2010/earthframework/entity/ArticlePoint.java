package com.snailstudio2010.earthframework.entity;

import android.graphics.Bitmap;

import com.snailstudio2010.earthframework.MarkerPoint;

/**
 * Created by xuqiqiang on 2019/08/20.
 */
public class ArticlePoint extends MarkerPoint {

    public String info;
    public String photo;
    public String location;
    public Bitmap bitmap;

    public ArticlePoint(double x, double y, String info, String photo, String location) {
        super(x, y);
        this.info = info;
        this.photo = photo;
        this.location = location;
    }

    @Override
    public String toString() {
        return "ArticlePoint{" +
                "info='" + info + '\'' +
                ", photo='" + photo + '\'' +
                ", location='" + location + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}