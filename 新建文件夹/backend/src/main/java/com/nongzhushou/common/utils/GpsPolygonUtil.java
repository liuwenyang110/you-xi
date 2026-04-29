package com.nongzhushou.common.utils;

import java.util.List;

/**
 * GPS 实用地块面积测算工具
 * 核心干货功能：用于解决农户与机手关于“亩数”的扯皮问题
 * 依据机手绕田打卡的经纬度多边形，计算出地表实际面积 (单位: 亩)
 *
 * 算法：球面多边形面积计算 (基于地球半径与经纬度夹角积分实现近似换算)
 */
public class GpsPolygonUtil {

    private static final double EARTH_RADIUS = 6378137.0; // 地球赤道半径(米)

    public static class Point {
        public double lng;
        public double lat;
        public Point(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }
    }

    /**
     * 计算地块实际测量面积并转化为“亩”
     * @param points 绕田打卡的按顺/逆时针排列的经纬度点集
     * @return 实际亩数 (保留两位小数)
     */
    public static double calculateAreaToMu(List<Point> points) {
        if (points == null || points.size() < 3) {
            return 0.0; // 形不成多边形
        }

        double area = 0.0;
        int p1 = 0, p2 = 1;
        
        // 使用测地线流形积分多边形面积近似法
        for (int i = 0; i < points.size(); i++) {
            p1 = i;
            p2 = (i + 1) % points.size();

            // 弧度转换
            double lng1 = Math.toRadians(points.get(p1).lng);
            double lat1 = Math.toRadians(points.get(p1).lat);
            double lng2 = Math.toRadians(points.get(p2).lng);
            double lat2 = Math.toRadians(points.get(p2).lat);

            // 梯形法则积分
            area += (lng2 - lng1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }

        area = area * EARTH_RADIUS * EARTH_RADIUS / 2.0;

        // 取绝对值 (因为顺逆时针积分方向问题可能出现负数)
        double squareMeters = Math.abs(area);
        
        // 1 亩 ≈ 666.667 平方米
        double mu = squareMeters / 666.6666666666667;
        
        return Math.round(mu * 100.0) / 100.0;
    }
}
