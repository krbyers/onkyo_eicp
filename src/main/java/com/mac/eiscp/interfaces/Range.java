/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mac.eiscp.interfaces.impl.SimpleRange;

/**
 *
 * @author Mac
 */
@JsonDeserialize(as=SimpleRange.class)
public interface Range {
      
    public void setCurrent(int current);
    
    public Integer getMin();
    
    public Integer getMax();
    
    public Integer getCurrent();
    
    public Integer getSteps();
    
    public void setSteps(int steps);
    
    public Integer increase();
    
    public Integer decrease();
    
    public String currentHex();
    
    public String formatCurrent();
    
    public void setAsHex(boolean asHex);
    
    public boolean isHex();
}
