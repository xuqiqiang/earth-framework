package com.snailstudio2010.earthframework;

/**
 * Created by xuqiqiang on 2019/08/20.
 */
public class MarkerPoint {

    public double x;
    public double y;

    public MarkerPoint() {
    }

    public MarkerPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return (int) (x * 1000000 + y * 100000);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MarkerPoint && (this.hashCode() == obj.hashCode());
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}