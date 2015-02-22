/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.services.protocols;

import com.mac.eiscp.services.controllers.status.Status;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mac
 */
@Component
public class FailedProtocol extends Protocol {

    public FailedProtocol() {
        super.setIsModified(true);
        super.setResponse(Status.FAILED.name());
        super.setRoutedTo(ROUTED_TO);
        super.setParameters(null);
    }

    public FailedProtocol(Protocol protocol) {
        super.setIsModified(true);
        super.setResponse(Status.FAILED.name());
        super.setRoutedTo(ROUTED_TO);
        if (Objects.nonNull(protocol)) {            
            super.setParameters(protocol.getParameters());            
        }else{
            super.setParameters(null);
        }
    }
    
    public void setValues(Protocol protocol){
        if (Objects.nonNull(protocol)) {            
            super.setParameters(protocol.getParameters());            
        }else{
            super.setParameters(null);
        }
    }
}
