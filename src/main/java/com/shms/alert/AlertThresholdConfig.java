package com.shms.alert;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurable thresholds for demonstration purposes only.
 * NOT for clinical diagnostic use.
 */
@Configuration
@ConfigurationProperties(prefix = "shms.thresholds")
public class AlertThresholdConfig {

    private int minHeartRate = 60;
    private int maxHeartRate = 100;
    private double minOxygenSaturation = 95.0;
    private double minTemperature = 36.0;
    private double maxTemperature = 37.5;
    private int maxSystolicBp = 120;
    private int minSystolicBp = 90;
    private int maxDiastolicBp = 80;
    private int minDiastolicBp = 60;

    public int getMinHeartRate() {
        return minHeartRate;
    }

    public void setMinHeartRate(int minHeartRate) {
        this.minHeartRate = minHeartRate;
    }

    public int getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public double getMinOxygenSaturation() {
        return minOxygenSaturation;
    }

    public void setMinOxygenSaturation(double minOxygenSaturation) {
        this.minOxygenSaturation = minOxygenSaturation;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMaxSystolicBp() {
        return maxSystolicBp;
    }

    public void setMaxSystolicBp(int maxSystolicBp) {
        this.maxSystolicBp = maxSystolicBp;
    }

    public int getMinSystolicBp() {
        return minSystolicBp;
    }

    public void setMinSystolicBp(int minSystolicBp) {
        this.minSystolicBp = minSystolicBp;
    }

    public int getMaxDiastolicBp() {
        return maxDiastolicBp;
    }

    public void setMaxDiastolicBp(int maxDiastolicBp) {
        this.maxDiastolicBp = maxDiastolicBp;
    }

    public int getMinDiastolicBp() {
        return minDiastolicBp;
    }

    public void setMinDiastolicBp(int minDiastolicBp) {
        this.minDiastolicBp = minDiastolicBp;
    }
}
