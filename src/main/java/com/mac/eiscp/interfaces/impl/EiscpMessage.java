/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.mac.eiscp.interfaces.Command;
import com.mac.eiscp.interfaces.Message;
import java.util.Objects;

/**
 *
 * @author Mac
 */
public class EiscpMessage implements Message {

    private Command command;
    private String message;

    public EiscpMessage() {
    }
    
    public EiscpMessage(byte[] msg) {
        message = new String(msg);
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public void setCommand(Command command){
        this.command = command;
    }
    
    @JsonIgnore
    @Override
    public Command getCommand(){
        if(Objects.nonNull(command)){
            return this.command;
        }else{
            return new EiscpCommand(this);
        }
    }

    private void validateStringBuilder(StringBuilder sb) {
        Preconditions.checkNotNull(sb, sb);
    }

    @Override
    public int length() {
        return message.length();
    }

    @Override
    public byte[] toBytes() {
        return this.message.getBytes();
    }

    @Override
    public void setMessage(String msg) {
        this.message = msg;
    }
}
