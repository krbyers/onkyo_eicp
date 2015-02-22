/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces.impl;

import com.mac.eiscp.interfaces.Parameter;
import com.mac.eiscp.interfaces.Range;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author Mac
 */
public class InputParameter {
    
    private String paramName;
    private Range range;

    public InputParameter() {
        this.paramName = "";
        this.range = new SimpleRange(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    
    public InputParameter(Parameter param){
        this();
        if(Objects.nonNull(param)){
            if(Objects.nonNull(param.getParamName())){
                this.paramName = param.getParamName();
            }
            if(Objects.nonNull(param.getRange())){
                this.range = new SimpleRange(param.getRange());
            }
        }
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }
}
