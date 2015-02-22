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
public class Power implements CommandStateUpdatable, InitializableState {

    private static final String QSTN = "PWRQSTN";
    private final String on = "PWR01";
    private final String off = "PWR00";
    private boolean state = false;

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getOn() {
        return on;
    }

    public String getOff() {
        return off;
    }

    @Override
    public void updateState(Command cmd) {
        if (isValid(cmd, on)) {
            if (cmd.getCommand().equalsIgnoreCase(on)) {
                state = true;
            } else if (cmd.getCommand().equalsIgnoreCase(off)) {
                state = false;
            }
        }
    }

    @JsonIgnore
    @Override
    public String getStateQstn() {
        return QSTN;
    }
}
