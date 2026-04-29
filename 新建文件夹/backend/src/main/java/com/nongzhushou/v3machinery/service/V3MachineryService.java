package com.nongzhushou.v3machinery.service;

import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.v3machinery.dto.V3MachineryRequest;
import com.nongzhushou.v3machinery.entity.V3MachineryEntity;
import com.nongzhushou.v3machinery.mapper.V3MachineryMapper;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import com.nongzhushou.zone.mapper.ZoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class V3MachineryService {

    @Autowired private V3MachineryMapper machineryMapper;
    @Autowired private V3UserService v3UserService;
    @Autowired private ZoneMapper zoneMapper;

    /**
     * 农机手登记农机
     */
    @Transactional
    public V3MachineryEntity register(V3MachineryRequest req) {
        V3UserEntity operator = v3UserService.getCurrentV3User();
        if (!"OPERATOR".equals(operator.getRole())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "仅农机手可登记农机");
        }
        if (operator.getZoneId() == null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "请先加入片区再登记农机");
        }

        V3MachineryEntity m = new V3MachineryEntity();
        m.setOperatorId(operator.getId());
        m.setZoneId(operator.getZoneId());
        m.setMachineTypeId(req.getMachineTypeId());
        m.setBrand(req.getBrand());
        m.setModelNo(req.getModelNo());
        m.setSuitableCrops(req.getSuitableCrops());
        m.setWorkTypes(req.getWorkTypes());
        m.setIsCrossRegion(req.getIsCrossRegion() != null ? req.getIsCrossRegion() : 0);
        m.setCrossRangeDesc(req.getCrossRangeDesc());
        m.setAvailDesc(req.getAvailDesc());
        m.setPhotos(req.getPhotos());
        m.setDescription(req.getDescription());
        m.setStatus("ACTIVE");
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        machineryMapper.insert(m);

        // 刷新片区统计
        zoneMapper.refreshStats(operator.getZoneId());
        return m;
    }

    /**
     * 更新农机信息
     */
    @Transactional
    public V3MachineryEntity update(Long machineryId, V3MachineryRequest req) {
        V3UserEntity operator = v3UserService.getCurrentV3User();
        V3MachineryEntity m = machineryMapper.selectById(machineryId);
        if (m == null) throw new BizException(ErrorCode.BIZ_ERROR, "农机记录不存在");
        if (!m.getOperatorId().equals(operator.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权修改他人的农机信息");
        }

        m.setMachineTypeId(req.getMachineTypeId());
        m.setBrand(req.getBrand());
        m.setModelNo(req.getModelNo());
        m.setSuitableCrops(req.getSuitableCrops());
        m.setWorkTypes(req.getWorkTypes());
        m.setIsCrossRegion(req.getIsCrossRegion() != null ? req.getIsCrossRegion() : 0);
        m.setCrossRangeDesc(req.getCrossRangeDesc());
        m.setAvailDesc(req.getAvailDesc());
        m.setPhotos(req.getPhotos());
        m.setDescription(req.getDescription());
        m.setUpdatedAt(LocalDateTime.now());
        machineryMapper.updateById(m);
        return m;
    }

    /**
     * 删除（下架）我的农机
     */
    @Transactional
    public void deactivate(Long machineryId) {
        V3UserEntity operator = v3UserService.getCurrentV3User();
        V3MachineryEntity m = machineryMapper.selectById(machineryId);
        if (m == null) throw new BizException(ErrorCode.BIZ_ERROR, "农机记录不存在");
        if (!m.getOperatorId().equals(operator.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作他人的农机信息");
        }
        m.setStatus("INACTIVE");
        m.setUpdatedAt(LocalDateTime.now());
        machineryMapper.updateById(m);
        zoneMapper.refreshStats(operator.getZoneId());
    }

    /**
     * 我的农机列表（农机手查看自己登记的）
     */
    public List<V3MachineryEntity> myMachinery() {
        V3UserEntity operator = v3UserService.getCurrentV3User();
        return machineryMapper.findByOperator(operator.getId());
    }

    /**
     * 片区农机列表（公开展示，过滤虚拟联系方式）
     */
    public List<V3MachineryEntity> getZoneMachinery(Long zoneId) {
        return machineryMapper.findByZone(zoneId);
    }

    /**
     * 片区农机分类统计（公开：展示"收割机3台、旋耕机2台"等）
     */
    public List<Map<String, Object>> getZoneStats(Long zoneId) {
        return machineryMapper.countByZoneAndCategory(zoneId);
    }
}
