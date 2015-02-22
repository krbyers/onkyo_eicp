/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.options.sources.suboptions;

/**
 *
 * @author Mac
 */
public class Preset {

    private String presetHexValue;
    private int presetNum;
    private float frequency;
    private String band;

    public Preset() {
    }

    public Preset(int presetNum, float frequency, String band) {
        this.presetNum = presetNum >= 1 && presetNum <= 40 ? presetNum : 0;
        
        String pre = this.presetNum > 0 ? this.presetNum < 10 ? ("0" + this.presetNum) : "" : null;        
        presetHexValue = (pre == null ? "" : "PRS" + (pre.equals("") ? Integer.toHexString(presetNum) : pre)).toUpperCase();
        this.frequency = frequency;
        this.band = band;
    }

    public String getFrequency() {
        return String.format("%.1f", frequency);
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public int getPresetNum() {
        return presetNum;
    }

    public void setPresetNum(int presetNum) {
        this.presetNum = presetNum;
    }

    public String getPresetHexValue() {
        return presetHexValue;
    }

    public void setPresetHexValue(String presetHexValue) {
        this.presetHexValue = presetHexValue;
    }
}
