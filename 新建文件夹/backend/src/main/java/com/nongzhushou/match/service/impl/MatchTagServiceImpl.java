package com.nongzhushou.match.service.impl;

import com.nongzhushou.match.service.MatchTagService;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MatchTagServiceImpl implements MatchTagService {

    @Override
    public boolean matchCrop(String demandCropCode, Set<String> serviceCropTags) {
        if (demandCropCode == null || serviceCropTags == null || serviceCropTags.isEmpty()) {
            return false;
        }
        return serviceCropTags.contains(demandCropCode);
    }

    @Override
    public boolean matchTerrain(Set<String> demandTerrainTags, Set<String> serviceTerrainTags) {
        if (demandTerrainTags == null || demandTerrainTags.isEmpty()) {
            return true;
        }
        if (serviceTerrainTags == null || serviceTerrainTags.isEmpty()) {
            return false;
        }
        return serviceTerrainTags.containsAll(demandTerrainTags) || demandTerrainTags.stream().anyMatch(serviceTerrainTags::contains);
    }

    @Override
    public boolean matchPlot(Set<String> demandPlotTags, Set<String> servicePlotTags) {
        if (demandPlotTags == null || demandPlotTags.isEmpty()) {
            return true;
        }
        if (servicePlotTags == null || servicePlotTags.isEmpty()) {
            return false;
        }
        return demandPlotTags.stream().anyMatch(servicePlotTags::contains);
    }

    @Override
    public boolean matchArea(BigDecimal demandArea, BigDecimal minArea, BigDecimal maxArea) {
        if (demandArea == null) {
            return false;
        }
        if (minArea != null && demandArea.compareTo(minArea) < 0) {
            return false;
        }
        if (maxArea != null && demandArea.compareTo(maxArea) > 0) {
            return false;
        }
        return true;
    }
}
