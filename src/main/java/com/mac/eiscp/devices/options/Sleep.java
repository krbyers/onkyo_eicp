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
public class Sleep implements CommandStateUpdatable, InitializableState{
    @JsonIgnore
    private static final int MAX_THRESHOLD = 90;
    
    private static final String QSTN = "SLPQSTN";
    private String time = "OFF";
    private final String off = "SLPOFF";
    private final String up = "SLPUP";  //sleep wrap around up

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOff() {
        return off;
    }

    public String getUp() {
        return up;
    }

    @Override
    public void updateState(Command cmd) {
        if(isValid(cmd, up)){
            if(cmd.getCommand().equalsIgnoreCase(off)){
                time = "OFF";
            }else {
                String param = cmd.getParameter().getParamName();
                try{
                    time = String.valueOf(Integer.parseInt(param, 16));
                    if(time.equals("0")){
                        time = "OFF";
                    }
                }catch(NumberFormatException ex){}
            }
        }
    }

    @JsonIgnore
    @Override
    public String getStateQstn() {
        return QSTN;
    }
    
    
}
