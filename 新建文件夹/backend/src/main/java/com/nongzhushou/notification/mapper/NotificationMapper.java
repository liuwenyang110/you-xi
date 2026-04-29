package com.nongzhushou.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.notification.entity.NotificationEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationEntity> {

    @Select("SELECT * FROM v3_notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<NotificationEntity> findByUser(@Param("userId") Long userId,
                                        @Param("limit") int limit,
                                        @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM v3_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUser(@Param("userId") Long userId);

    @Update("UPDATE v3_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllReadByUser(@Param("userId") Long userId);
}
