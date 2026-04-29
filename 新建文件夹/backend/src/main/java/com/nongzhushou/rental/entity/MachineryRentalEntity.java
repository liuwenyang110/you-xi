package com.nongzhushou.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("machinery_rental")
public class MachineryRentalEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private Long postId;
    private String machineCategory;
    private String machineType;
    private String brandModel;
    private String priceMode;       // PER_DAY / PER_MU / DISCUSS
    private BigDecimal priceValue;
    private String imagesJson;
    private String locationName;
    private BigDecimal lat;
    private BigDecimal lng;
    private String contactPhone;
    private String status;          // AVAILABLE / RENTED / OFF
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getMachineCategory() { return machineCategory; }
    public void setMachineCategory(String machineCategory) { this.machineCategory = machineCategory; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public String getBrandModel() { return brandModel; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }
    public String getPriceMode() { return priceMode; }
    public void setPriceMode(String priceMode) { this.priceMode = priceMode; }
    public BigDecimal getPriceValue() { return priceValue; }
    public void setPriceValue(BigDecimal priceValue) { this.priceValue = priceValue; }
    public String getImagesJson() { return imagesJson; }
    public void setImagesJson(String imagesJson) { this.imagesJson = imagesJson; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
