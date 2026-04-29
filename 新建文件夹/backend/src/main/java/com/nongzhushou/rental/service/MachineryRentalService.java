package com.nongzhushou.rental.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.community.entity.CommunityPostEntity;
import com.nongzhushou.community.mapper.CommunityPostMapper;
import com.nongzhushou.rental.entity.MachineryRentalEntity;
import com.nongzhushou.rental.mapper.MachineryRentalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MachineryRentalService {

    private final MachineryRentalMapper rentalMapper;
    private final CommunityPostMapper postMapper;

    public MachineryRentalService(MachineryRentalMapper rentalMapper, CommunityPostMapper postMapper) {
        this.rentalMapper = rentalMapper;
        this.postMapper = postMapper;
    }

    /** 发布出租信息 (同时自动生成社区帖子) */
    @Transactional
    public Long publishRental(MachineryRentalEntity rental) {
        rental.setStatus("AVAILABLE");
        rentalMapper.insert(rental);

        // 同步发布一条社区帖子
        CommunityPostEntity post = new CommunityPostEntity();
        post.setAuthorId(rental.getOwnerId());
        post.setPostType("RENTAL");
        post.setTitle(rental.getBrandModel() + " 闲置出租");
        post.setContent("机械类型: " + rental.getMachineType()
                + ", 计费: " + formatPrice(rental)
                + ", 位置: " + rental.getLocationName());
        post.setImagesJson(rental.getImagesJson());
        post.setMachineCategory(rental.getMachineCategory());
        post.setMachineType(rental.getMachineType());
        post.setLat(rental.getLat());
        post.setLng(rental.getLng());
        post.setLocationName(rental.getLocationName());
        post.setIsUrgent(0);
        post.setStatus("ACTIVE");
        post.setViewCount(0);
        post.setReplyCount(0);
        postMapper.insert(post);

        // 回写 post_id
        rental.setPostId(post.getId());
        rentalMapper.updateById(rental);

        return rental.getId();
    }

    /** 获取可用出租列表 */
    public List<MachineryRentalEntity> listAvailable() {
        LambdaQueryWrapper<MachineryRentalEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(MachineryRentalEntity::getStatus, "AVAILABLE");
        qw.orderByDesc(MachineryRentalEntity::getCreatedAt);
        return rentalMapper.selectList(qw);
    }

    /** 下架出租 */
    public void offShelf(Long id, Long ownerId) {
        MachineryRentalEntity rental = rentalMapper.selectById(id);
        if (rental != null && rental.getOwnerId().equals(ownerId)) {
            rental.setStatus("OFF");
            rentalMapper.updateById(rental);
        }
    }

    private String formatPrice(MachineryRentalEntity r) {
        if ("DISCUSS".equals(r.getPriceMode())) return "面议";
        String unit = "PER_DAY".equals(r.getPriceMode()) ? "元/天" : "元/亩";
        return r.getPriceValue() + unit;
    }
}
