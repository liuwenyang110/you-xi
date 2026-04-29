package com.nongzhushou.v3demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.v3demand.entity.V3GroupMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface V3GroupMemberMapper extends BaseMapper<V3GroupMemberEntity> {

    @Select("SELECT * FROM v3_group_member WHERE group_id = #{groupId} AND join_status = 'APPROVED' ORDER BY id ASC")
    List<V3GroupMemberEntity> findApprovedByGroup(Long groupId);

    @Select("SELECT * FROM v3_group_member WHERE group_id = #{groupId} AND join_status = 'PENDING' ORDER BY id ASC")
    List<V3GroupMemberEntity> findPendingByGroup(Long groupId);

    @Select("SELECT * FROM v3_group_member WHERE group_id = #{groupId} AND farmer_id = #{farmerId} LIMIT 1")
    V3GroupMemberEntity findByGroupAndFarmer(Long groupId, Long farmerId);
}
