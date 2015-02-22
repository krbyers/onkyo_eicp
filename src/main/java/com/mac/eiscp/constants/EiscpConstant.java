/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.constants;

import com.mac.eiscp.messaging.tcp.EiscpSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mac
 */
public class EiscpConstant {

    public static final int SOCKET_TIMEOUT = 500;
    public static final int DEFAULT_EISCP_PORT = 60128;
    public static final byte EOF = 0x1A;
    public static final byte CR = 0x0D;
    public static final byte LF = 0x0A;

    public static final String AUTODISCOVER_QSTN = "!xECNQSTN";
    
    public static final InetAddress DEFAULT_EISCP_IP;
    static {
        InetAddress defaultAddr = null;
        try {
            defaultAddr = InetAddress.getByName("192.168.11.120");
        } catch (UnknownHostException ex) {
            Logger.getLogger(EiscpSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        DEFAULT_EISCP_IP = defaultAddr;
    }
}
