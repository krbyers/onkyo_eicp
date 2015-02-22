/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices;

import com.mac.eiscp.interfaces.impl.SingleDevice;

/**
 *
 * @author Mac
 */
public class TX_NR626 extends SingleDevice {
    
    public static final String REAL_NAME = TX_NR626.class.getSimpleName().replace("_", "-");
    
    public TX_NR626(){
        super();
        this.setDeviceName(REAL_NAME);
    }
    
    
}
