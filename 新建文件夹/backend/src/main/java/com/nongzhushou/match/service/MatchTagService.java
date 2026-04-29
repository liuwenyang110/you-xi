package com.nongzhushou.match.service;

import java.math.BigDecimal;
import java.util.Set;

public interface MatchTagService {
    boolean matchCrop(String demandCropCode, Set<String> serviceCropTags);
    boolean matchTerrain(Set<String> demandTerrainTags, Set<String> serviceTerrainTags);
    boolean matchPlot(Set<String> demandPlotTags, Set<String> servicePlotTags);
    boolean matchArea(BigDecimal demandArea, BigDecimal minArea, BigDecimal maxArea);
}
