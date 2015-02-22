/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mac.eiscp.interfaces.impl.EiscpMessage;

/**
 *
 * @author Mac
 */
@JsonDeserialize(as=EiscpMessage.class)
public interface Message extends Byteable{
    
    void setMessage(String msg);
    
    String getMessage();
    
    void setCommand(Command command);
    
    Command getCommand();
    
    int length();
}
