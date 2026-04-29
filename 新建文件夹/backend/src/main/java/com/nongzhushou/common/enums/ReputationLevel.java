package com.nongzhushou.common.enums;

/**
 * 老把式信誉等级枚举
 * 中国农村版征信体系，农民一听就懂
 */
public enum ReputationLevel {
    NEWBIE("新手机手", "⭐", 0, 0),
    RELIABLE("靠谱机手", "⭐⭐", 5, 80),
    VETERAN("老把式", "⭐⭐⭐", 20, 95),
    GOLD("金牌机手", "⭐⭐⭐⭐", 50, 98),
    MODEL("村级模范", "⭐⭐⭐⭐⭐", 100, 99);

    private final String label;
    private final String stars;
    private final int minServices;     // 最低服务次数
    private final int minGoodRate;     // 最低好评率

    ReputationLevel(String label, String stars, int minServices, int minGoodRate) {
        this.label = label;
        this.stars = stars;
        this.minServices = minServices;
        this.minGoodRate = minGoodRate;
    }

    public String getLabel() { return label; }
    public String getStars() { return stars; }
    public int getMinServices() { return minServices; }
    public int getMinGoodRate() { return minGoodRate; }

    /**
     * 根据服务次数和好评率自动计算当前应有的等级
     */
    public static ReputationLevel calculate(int totalServices, double goodRate, boolean hasDisasterRecord) {
        // 村级模范需要救灾记录
        if (hasDisasterRecord && totalServices >= MODEL.minServices && goodRate >= MODEL.minGoodRate) {
            return MODEL;
        }
        if (totalServices >= GOLD.minServices && goodRate >= GOLD.minGoodRate) {
            return GOLD;
        }
        if (totalServices >= VETERAN.minServices && goodRate >= VETERAN.minGoodRate) {
            return VETERAN;
        }
        if (totalServices >= RELIABLE.minServices && goodRate >= RELIABLE.minGoodRate) {
            return RELIABLE;
        }
        return NEWBIE;
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "未知等级";
        }
        try {
            return valueOf(code).getLabel();
        } catch (IllegalArgumentException ignored) {
            return code;
        }
    }
}
