/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.messaging;

import com.mac.eiscp.interfaces.Message;
import com.mac.eiscp.interfaces.MessageListener;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mac
 */
public class ConsoleMessageListener implements MessageListener{

    private static final Logger log = LoggerFactory.getLogger(ConsoleMessageListener.class);
    
    @Override
    public void messageReceived(Message message) {
        if(Objects.nonNull(message)){
            log.info(message.getMessage());
        }
    }
    
}
