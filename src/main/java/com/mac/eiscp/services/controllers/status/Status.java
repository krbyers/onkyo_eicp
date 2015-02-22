/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.services.controllers.status;

/**
 *
 * @author Mac
 */
public enum Status {
    SUCCESS, FAILED, NOT_FOUND(404), OK(200), NOT_MODIFIED(304);
    
    private final int code;
    
    Status(){
        code = Integer.MIN_VALUE;
    }
    
    Status(int code){
        this.code = code;
    }
    
    public int code(){
        return code;
    }
}
