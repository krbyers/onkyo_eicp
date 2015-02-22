/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mac.eiscp.interfaces.impl.EiscpParameter;

/**
 *
 * @author Mac
 */
@JsonDeserialize(as=EiscpParameter.class)
public interface Parameter {
    
    void setParamName(String paramName);
    
    String getParamName();
    
    void setRange(Range range);
    
    Range getRange();
    
    String getParameter();
    
    int length();
}
