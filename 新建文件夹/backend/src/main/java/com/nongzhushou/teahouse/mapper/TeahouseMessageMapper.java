package com.nongzhushou.teahouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.teahouse.entity.TeahouseMessageEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TeahouseMessageMapper extends BaseMapper<TeahouseMessageEntity> {

    @Select("SELECT * FROM v3_teahouse_message WHERE teahouse_id = #{teahouseId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<TeahouseMessageEntity> findByTeahouse(@Param("teahouseId") Long teahouseId,
                                                @Param("limit") int limit,
                                                @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM v3_teahouse_message WHERE teahouse_id = #{teahouseId}")
    int countByTeahouse(@Param("teahouseId") Long teahouseId);

    /** 获取茶馆内所有媒体文件的 OSS Key（用于关闭时批量清理） */
    @Select("SELECT media_key FROM v3_teahouse_message WHERE teahouse_id = #{teahouseId} AND media_key IS NOT NULL")
    List<String> findMediaKeysByTeahouse(@Param("teahouseId") Long teahouseId);

    /** 物理删除茶馆全部消息（阅后即焚） */
    @Delete("DELETE FROM v3_teahouse_message WHERE teahouse_id = #{teahouseId}")
    int deleteByTeahouse(@Param("teahouseId") Long teahouseId);
}
