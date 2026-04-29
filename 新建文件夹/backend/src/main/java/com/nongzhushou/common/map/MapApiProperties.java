package com.nongzhushou.common.map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.map")
public class MapApiProperties {

    private boolean gaodeEnabled = true;
    private String gaodeApiKey;
    private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";
    private String drivingUrl = "https://restapi.amap.com/v3/direction/driving";
    private String inputTipsUrl = "https://restapi.amap.com/v3/assistant/inputtips";

    public boolean isGaodeEnabled() {
        return gaodeEnabled;
    }

    public void setGaodeEnabled(boolean gaodeEnabled) {
        this.gaodeEnabled = gaodeEnabled;
    }

    public String getGaodeApiKey() {
        return gaodeApiKey;
    }

    public void setGaodeApiKey(String gaodeApiKey) {
        this.gaodeApiKey = gaodeApiKey;
    }

    public String getGeocodeUrl() {
        return geocodeUrl;
    }

    public void setGeocodeUrl(String geocodeUrl) {
        this.geocodeUrl = geocodeUrl;
    }

    public String getDrivingUrl() {
        return drivingUrl;
    }

    public void setDrivingUrl(String drivingUrl) {
        this.drivingUrl = drivingUrl;
    }

    public String getInputTipsUrl() {
        return inputTipsUrl;
    }

    public void setInputTipsUrl(String inputTipsUrl) {
        this.inputTipsUrl = inputTipsUrl;
    }
}
