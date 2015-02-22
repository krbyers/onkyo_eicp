/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mac.eiscp.devices.interfaces.CommandStateUpdatable;
import com.mac.eiscp.devices.interfaces.InitializableState;
import com.mac.eiscp.interfaces.Command;

/**
 *
 * @author Mac
 */
public class Mute implements CommandStateUpdatable, InitializableState {

    private static final String QSTN = "AMTQSTN";
    private boolean isMuted;
    private final String muteOn;
    private final String muteOff;
    private final String muteWrapAround;

    public Mute() {
        isMuted = false;
        
        muteOn = "AMT01";
        muteOff = "AMT00";
        muteWrapAround = "AMTTG";
    }

    public boolean isIsMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    public String getMuteOn() {
        return muteOn;
    }

    public String getMuteOff() {
        return muteOff;
    }

    public String getMuteWrapAround() {
        return muteWrapAround;
    }

    @Override
    public void updateState(Command cmd) {
        if (isValid(cmd, muteWrapAround)) {
            if (cmd.getCommand().equalsIgnoreCase(muteOn)) {
                isMuted = true;
            } else if (cmd.getCommand().equalsIgnoreCase(muteOff)) {
                isMuted = false;
            }
        }
    }

    @JsonIgnore
    @Override
    public String getStateQstn() {
        return QSTN;
    }
}
