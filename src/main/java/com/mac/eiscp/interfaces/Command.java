/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mac.eiscp.interfaces.impl.EiscpCommand;

/**
 *
 * @author Mac
 */
@JsonDeserialize(as=EiscpCommand.class)
public interface Command extends Byteable, Comparable<Command> {
    
    void setName(String name);
    
    String getName();
    
    void setCommandMainFunction(String mainFunc);
    
    String getCommandMainFunction();
    
    void setCommandFunction(String function);
    
    String getCommandFunction();
    
    void setParameter(Parameter param);
    
    Parameter getParameter();
    
    String getCommand();
    
    void addPrefix(String prefix);
    
    String getPrefix();
    
    int length();
    
    boolean hasRange();
    
    Command cloneCommand();
    
}
