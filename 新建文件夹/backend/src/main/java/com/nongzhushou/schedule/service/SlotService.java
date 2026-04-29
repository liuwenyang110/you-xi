package com.nongzhushou.schedule.service;

public interface SlotService {

    boolean lockSlotWithLua(Long equipmentId, String date, Long slotMask, Long userId);
}
