package com.nongzhushou.unifiedmatch.task;

import com.nongzhushou.unifiedmatch.service.UnifiedMatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UnifiedMatchTimeoutTask {

    private final UnifiedMatchService service;

    public UnifiedMatchTimeoutTask(UnifiedMatchService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 30000)
    public void handleOwnerTimeout() {
        service.handleOwnerTimeout();
    }

    @Scheduled(fixedDelay = 30000)
    public void handleFarmerTimeout() {
        service.handleFarmerTimeout();
    }
}
