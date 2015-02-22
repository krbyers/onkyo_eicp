/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.options.sources;

import com.mac.eiscp.devices.options.sources.abstracts.SingleSource;
import com.mac.eiscp.devices.options.sources.suboptions.Preset;

/**
 *
 * @author Mac
 */
public class Tuner extends SingleSource {

    private float minFmFrequency;
    private float maxFmFrequency;
    private int minAmFrequency;
    private int maxAmFrequency;
    private float frequency;
    private String band;
    private Preset[] presets;

    public Tuner() {
        super("Tuner", "SLI24", "SLI25", "SLI26");
        minFmFrequency = 88.1f;
        maxFmFrequency = 107.9f;
        minAmFrequency = 531;
        maxAmFrequency = 1611;
        this.band = "FM";
        this.frequency = 102.1f;
        presets = new Preset[40];
        int index;
        float range;
        int amRange;
        for (index = 0, range = 88.1f; index < presets.length && range < maxFmFrequency; index++) {
            presets[index] = new Preset((index + 1), range, "FM");
            range += 0.7f;
        }
        for (amRange = 531; index < presets.length && amRange < maxAmFrequency; index++) {
            presets[index] = new Preset((index + 1), amRange, "AM");
            amRange += (9 * 6);
        }
    }

    public float getFrequency() {
        return frequency;
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

    public Preset[] getPresets() {
        return presets;
    }

    public void setPresets(Preset[] presets) {
        this.presets = presets;
    }

    public float getMinFmFrequency() {
        return minFmFrequency;
    }

    public void setMinFmFrequency(float minFmFrequency) {
        this.minFmFrequency = minFmFrequency;
    }

    public float getMaxFmFrequency() {
        return maxFmFrequency;
    }

    public void setMaxFmFrequency(float maxFmFrequency) {
        this.maxFmFrequency = maxFmFrequency;
    }

    public int getMinAmFrequency() {
        return minAmFrequency;
    }

    public void setMinAmFrequency(int minAmFrequency) {
        this.minAmFrequency = minAmFrequency;
    }

    public int getMaxAmFrequency() {
        return maxAmFrequency;
    }

    public void setMaxAmFrequency(int maxAmFrequency) {
        this.maxAmFrequency = maxAmFrequency;
    }
}
