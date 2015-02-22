/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mac.eiscp.interfaces.impl.SingleDevice;
import com.mac.eiscp.interfaces.impl.EiscpMessage;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mac
 */
@JsonDeserialize(as=SingleDevice.class)
public interface Device {
    void setDeviceName(String name);
    String getDeviceName();    
    void setDeviceIP(InetAddress ipAddress);
    InetAddress getDeviceIP();
    void setDevicePort(int port);
    int getDevicePort();
    boolean isCompatibleCommand(Command command);
    
    /**
     * Returns a mapping of commands and their acceptable parameters
     * @return 
     */
    Map<Command, Boolean> getCommandMap();
    
    List<Command> getCompatibleCommands();
    
    Command getCommand(String command);
    
    Map<Command, Boolean> getCommandGroup(String baseCommand);
    
    void addCommand(Command command, boolean isValid);
    
    /**
     *
     * @param command
     * @return
     */
    public static Message buildMessage(Command command) {
        StringBuilder sb = new StringBuilder();
        EiscpMessage em = new EiscpMessage();
        em.setCommand(command);
        int eiscpDataSize = command.length() + 2; // this is the eISCP data size
        int eiscpMsgSize = eiscpDataSize + 1 + 16; // this is the size of the entire eISCP msg

        /* This is where I construct the entire message
         character by character. Each char is represented by a 2 disgit hex value */
        sb.append("ISCP");
    // the following are all in HEX representing one char

        // 4 char Big Endian Header
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("10", 16));

        // 4 char  Big Endian data size
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));
    // the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
        // ** BUT **
        // It only works if you send the size of the entire Message size (eiscpMsgSize)
        sb.append((char) Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

        // eiscp_version = "01";
        sb.append((char) Integer.parseInt("01", 16));

        // 3 chars reserved = "00"+"00"+"00";
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));
        sb.append((char) Integer.parseInt("00", 16));

    //  eISCP data
        // Start Character
        command.addPrefix("!1");

        // eISCP data - 3 char command and param    ie PWR01
        sb.append(command.getCommand());

    // msg end - this can be a few different cahrs depending on you receiver
        // my NR5008 works when I use  'EOF'
        sb.append((char) Integer.parseInt("0D", 16));

        System.out.println("eISCP data size: " + eiscpDataSize + "(0x" + Integer.toHexString(eiscpDataSize) + ") chars");
        System.out.println("eISCP msg size: " + sb.length() + "(0x" + Integer.toHexString(sb.length()) + ") chars");
        em.setMessage(sb.toString());
        return em;
    }
}
