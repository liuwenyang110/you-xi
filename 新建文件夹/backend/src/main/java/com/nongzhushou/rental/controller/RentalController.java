package com.nongzhushou.rental.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.rental.entity.MachineryRentalEntity;
import com.nongzhushou.rental.service.MachineryRentalService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final MachineryRentalService rentalService;

    public RentalController(MachineryRentalService rentalService) {
        this.rentalService = rentalService;
    }

    /** 发布出租（前端 rental.vue 提交到此） */
    @PostMapping
    public Result<Long> publish(@RequestBody MachineryRentalEntity entity) {
        return Result.ok(rentalService.publishRental(entity));
    }

    /** 查询可用出租列表 */
    @GetMapping
    public Result<List<MachineryRentalEntity>> list() {
        return Result.ok(rentalService.listAvailable());
    }

    /** 下架出租 */
    @PostMapping("/{id}/off")
    public Result<String> off(@PathVariable Long id, @RequestParam Long ownerId) {
        rentalService.offShelf(id, ownerId);
        return Result.ok("已下架");
    }
}
