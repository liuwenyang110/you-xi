package com.nongzhushou.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.community.entity.CommunityPostEntity;
import com.nongzhushou.community.mapper.CommunityPostMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommunityPostService {

    private final CommunityPostMapper postMapper;

    public CommunityPostService(CommunityPostMapper postMapper) {
        this.postMapper = postMapper;
    }

    /** 发布社区帖子 (急单/出租/闲聊) */
    public Long createPost(CommunityPostEntity entity) {
        entity.setStatus("ACTIVE");
        entity.setViewCount(0);
        entity.setReplyCount(0);
        postMapper.insert(entity);
        return entity.getId();
    }

    /** 按类型分页查询帖子列表 */
    public List<CommunityPostEntity> listByType(String postType, int page, int size) {
        LambdaQueryWrapper<CommunityPostEntity> qw = new LambdaQueryWrapper<>();
        if (postType != null && !postType.isEmpty()) {
            qw.eq(CommunityPostEntity::getPostType, postType);
        }
        qw.eq(CommunityPostEntity::getStatus, "ACTIVE");
        qw.orderByDesc(CommunityPostEntity::getIsUrgent);   // 急单置顶
        qw.orderByDesc(CommunityPostEntity::getCreatedAt);
        qw.last("LIMIT " + (page - 1) * size + "," + size);
        return postMapper.selectList(qw);
    }

    /** 查看帖子详情，浏览量+1 */
    public CommunityPostEntity detail(Long id) {
        CommunityPostEntity post = postMapper.selectById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postMapper.updateById(post);
        }
        return post;
    }

    /** 关闭帖子 */
    public void closePost(Long id, Long authorId) {
        CommunityPostEntity post = postMapper.selectById(id);
        if (post != null && post.getAuthorId().equals(authorId)) {
            post.setStatus("CLOSED");
            postMapper.updateById(post);
        }
    }
}
