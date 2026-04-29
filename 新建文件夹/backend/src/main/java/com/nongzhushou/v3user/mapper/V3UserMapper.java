package com.nongzhushou.v3user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.v3user.entity.V3UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface V3UserMapper extends BaseMapper<V3UserEntity> {

    @Select("SELECT id, account_id, zone_id, real_name, role, status, created_at, updated_at FROM v3_user WHERE account_id = #{accountId} LIMIT 1")
    V3UserEntity findByAccountId(Long accountId);

    @Select("SELECT id, account_id, zone_id, real_name, role, status, created_at, updated_at FROM v3_user WHERE zone_id = #{zoneId} AND role = 'OPERATOR' AND status = 'NORMAL' ORDER BY id ASC")
    List<V3UserEntity> findOperatorsByZone(Long zoneId);

    @Select("SELECT COUNT(*) FROM v3_user WHERE zone_id = #{zoneId} AND role = 'OPERATOR' AND status = 'NORMAL'")
    int countOperatorsByZone(Long zoneId);

    /** 管理员按角色分页查询用户（role 为 null 时查全部） */
    @Select("<script>" +
            "SELECT id, account_id, zone_id, real_name, role, status, created_at, updated_at FROM v3_user" +
            "<where><if test='role != null'>AND role = #{role}</if></where>" +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<V3UserEntity> findByRolePaged(@Param("role") String role,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    /** 按角色统计用户总数 */
    @Select("<script>" +
            "SELECT COUNT(*) FROM v3_user" +
            "<where><if test='role != null'>AND role = #{role}</if></where>" +
            "</script>")
    long countByRole(@Param("role") String role);
}
