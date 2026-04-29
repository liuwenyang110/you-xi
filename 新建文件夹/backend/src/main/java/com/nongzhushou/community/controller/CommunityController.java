package com.nongzhushou.community.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.community.entity.CommunityPostEntity;
import com.nongzhushou.community.service.CommunityPostService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v3/community")
public class CommunityController {

    private final CommunityPostService postService;

    public CommunityController(CommunityPostService postService) {
        this.postService = postService;
    }

    /** 发布帖子 */
    @PostMapping("/posts")
    public Result<Long> createPost(@RequestBody CommunityPostEntity entity) {
        return Result.ok(postService.createPost(entity));
    }

    /** 帖子列表 (可选按类型过滤: DEMAND_URGENT / RENTAL / CHAT) */
    @GetMapping("/posts")
    public Result<List<CommunityPostEntity>> listPosts(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.listByType(type, page, size));
    }

    /** 帖子详情 */
    @GetMapping("/posts/{id}")
    public Result<CommunityPostEntity> detail(@PathVariable Long id) {
        return Result.ok(postService.detail(id));
    }

    /** 关闭帖子 (作者操作) */
    @PostMapping("/posts/{id}/close")
    public Result<String> closePost(@PathVariable Long id, @RequestParam Long authorId) {
        postService.closePost(id, authorId);
        return Result.ok("已关闭");
    }
}
