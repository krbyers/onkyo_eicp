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
public class Volume implements CommandStateUpdatable, InitializableState {

    private static final String QSTN = "MVLQSTN";
    
    private int current;
    private final int min;
    private final int max;
    private final String up;
    private final String down;

    public Volume() {
        this.current = 10;
        this.down = "MVLDOWN";
        this.up = "MVLUP";
        this.max = 50;
        this.min = 0;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getUp() {
        return up;
    }

    public String getDown() {
        return down;
    }

    @Override
    public void updateState(Command cmd) {
        if (isValid(cmd, up)) {
            String name = cmd.getName();

            if (name.equalsIgnoreCase(up.substring(0, 3))
                    || name.equalsIgnoreCase(down.substring(0, 3))) {
                String param = cmd.getParameter().getParamName();
                try {
                    current = Integer.parseInt(param, 16);
                } catch (NumberFormatException ex) {
                }
            }
        }
    }

    @JsonIgnore
    @Override
    public String getStateQstn() {
        return QSTN;
    }

}
