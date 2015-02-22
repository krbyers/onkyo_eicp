/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.services.protocols;

import java.util.Objects;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mac
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Protocol {

    protected static final String ROUTED_TO = "response";
    private String routedTo;
    private String respondTo;
    private String[] parameters;
    private String response;
    private boolean isModified;

    public String getRoutedTo() {
        return routedTo;
    }

    public void setRoutedTo(String routedTo) {
        this.routedTo = routedTo;
    }

    public String getRespondTo() {
        return respondTo;
    }

    public void setRespondTo(String respondTo) {
        this.respondTo = respondTo;
    }

    public String[] getParameters() {
        return parameters;
    }
    
    public void setParameters(String... parameters) {
        this.parameters = parameters;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isIsModified() {
        return isModified;
    }

    public void setIsModified(boolean isModified) {
        this.isModified = isModified;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.routedTo);
        hash = 67 * hash + Objects.hashCode(this.respondTo);
        hash = 67 * hash + Objects.hashCode(this.parameters);
        hash = 67 * hash + Objects.hashCode(this.response);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Protocol other = (Protocol) obj;
        return Objects.equals(this.routedTo, other.routedTo)
                && Objects.equals(this.respondTo, other.respondTo)
                && Objects.equals(this.parameters, other.parameters)
                && Objects.equals(this.response, other.response);
    }
    
}
