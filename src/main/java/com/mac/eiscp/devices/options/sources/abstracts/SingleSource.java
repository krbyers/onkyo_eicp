/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mac.eiscp.devices.options.sources.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mac.eiscp.devices.interfaces.Selection;

/**
 *
 * @author Mac
 */
public abstract class SingleSource implements Selection {

    protected boolean isSelected;
    protected final String displayName;
    protected final String[] sourceCommand;

    public SingleSource(String displayName, String... sourceCommand) {
        isSelected = false;
        this.displayName = displayName;
        this.sourceCommand = sourceCommand;
    }

    @JsonIgnore
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setIsSelected(boolean selection) {
        this.isSelected = selection;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public String[] getSourceCommand() {
        return sourceCommand;
    }
}
