package com.nongzhushou.zone.service;

import com.nongzhushou.zone.entity.*;
import com.nongzhushou.zone.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ZoneService {

    @Autowired private RegionMapper regionMapper;
    @Autowired private ZoneMapper zoneMapper;
    @Autowired private MachineCategoryMapper machineCategoryMapper;
    @Autowired private MachineTypeMapper machineTypeMapper;
    @Autowired private CropMapper cropMapper;
    @Autowired private WorkTypeMapper workTypeMapper;

    // ---- 区划相关 ----

    /** 获取省级列表 */
    public List<RegionEntity> getProvinces() {
        return regionMapper.findByLevel(1);
    }

    /** 获取下级区划 */
    public List<RegionEntity> getChildren(String parentCode) {
        return regionMapper.findByParentCode(parentCode);
    }

    /** 通过区划码查找 */
    public RegionEntity getByCode(String code) {
        return regionMapper.findByCode(code);
    }

    // ---- 片区相关 ----

    /** 查询乡镇下所有片区 */
    public List<ZoneEntity> getZonesByTownship(String townshipCode) {
        return zoneMapper.findByTownship(townshipCode);
    }

    /** 查询县区下所有片区 */
    public List<ZoneEntity> getZonesByCounty(String countyCode) {
        return zoneMapper.findByCounty(countyCode);
    }

    /** 片区详情（含农机统计） */
    public ZoneEntity getZoneDetail(Long zoneId) {
        return zoneMapper.findById(zoneId);
    }

    /** 刷新片区统计数字 */
    public void refreshZoneStats(Long zoneId) {
        zoneMapper.refreshStats(zoneId);
    }

    // ---- 字典相关 ----

    /** 获取所有农机大类 */
    public List<MachineCategoryEntity> getMachineCategories() {
        return machineCategoryMapper.findAllEnabled();
    }

    /** 按大类获取小类 */
    public List<MachineTypeEntity> getMachineTypes(Integer categoryId) {
        if (categoryId != null) {
            return machineTypeMapper.findByCategory(categoryId);
        }
        return machineTypeMapper.findAllEnabled();
    }

    /** 获取所有作物品种 */
    public List<CropEntity> getCrops() {
        return cropMapper.findAllEnabled();
    }

    /** 获取所有作业类型 */
    public List<WorkTypeEntity> getWorkTypes(Integer categoryId) {
        if (categoryId != null) {
            return workTypeMapper.findByCategory(categoryId);
        }
        return workTypeMapper.findAllEnabled();
    }

    /**
     * 获取聚合的字典数据（供前端下拉框一次性加载）
     */
    public Map<String, Object> getAllDicts() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("machineCategories", getMachineCategories());
        result.put("machineTypes", getMachineTypes(null));
        result.put("crops", getCrops());
        result.put("workTypes", getWorkTypes(null));
        return result;
    }

    // ===== 管理员功能 =====

    /**
     * 管理员创建片区
     */
    public ZoneEntity createZone(Map<String, Object> params) {
        ZoneEntity zone = new ZoneEntity();
        zone.setName((String) params.get("name"));
        zone.setZoneType((String) params.getOrDefault("zoneType", "VILLAGE"));
        zone.setTownshipCode((String) params.get("townshipCode"));
        zone.setCountyCode((String) params.get("countyCode"));
        zone.setLocationDesc((String) params.get("locationDesc"));
        zone.setStatus("ACTIVE");
        zone.setOperatorCount(0);
        zone.setMachineryCount(0);
        zoneMapper.insert(zone);
        return zone;
    }

    /**
     * 管理员切换片区状态
     */
    public void toggleZoneStatus(Long zoneId, String status) {
        ZoneEntity zone = zoneMapper.findById(zoneId);
        if (zone == null) throw new RuntimeException("片区不存在");
        zone.setStatus(status);
        zoneMapper.updateById(zone);
    }

    /**
     * 查询县区下所有片区（含停用，管理员用）
     */
    public List<ZoneEntity> getAllZonesByCounty(String countyCode) {
        return zoneMapper.findAllByCounty(countyCode);
    }
}
